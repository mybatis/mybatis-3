/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.jdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.hsqldb.jdbc.JDBCConnection;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class PooledDataSourceTest extends BaseDataTest {

  @Test
  void shouldProperlyMaintainPoolOf3ActiveAnd2IdleConnections() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    try {
      runScript(ds, JPETSTORE_DDL);
      ds.setDefaultAutoCommit(false);
      ds.setDriverProperties(new Properties() {
        {
          setProperty("username", "sa");
          setProperty("password", "");
        }
      });
      ds.setPoolMaximumActiveConnections(3);
      ds.setPoolMaximumIdleConnections(2);
      ds.setPoolMaximumCheckoutTime(10000);
      ds.setPoolPingConnectionsNotUsedFor(1);
      ds.setPoolPingEnabled(true);
      ds.setPoolPingQuery("SELECT * FROM PRODUCT");
      ds.setPoolTimeToWait(10000);
      ds.setLogWriter(null);
      List<Connection> connections = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        connections.add(ds.getConnection());
      }
      assertEquals(3, ds.getPoolState().getActiveConnectionCount());
      for (Connection c : connections) {
        c.close();
      }
      assertEquals(2, ds.getPoolState().getIdleConnectionCount());
      assertEquals(4, ds.getPoolState().getRequestCount());
      assertEquals(0, ds.getPoolState().getBadConnectionCount());
      assertEquals(0, ds.getPoolState().getHadToWaitCount());
      assertEquals(0, ds.getPoolState().getAverageOverdueCheckoutTime());
      assertEquals(0, ds.getPoolState().getClaimedOverdueConnectionCount());
      assertEquals(0, ds.getPoolState().getAverageWaitTime());
      assertNotNull(ds.getPoolState().toString());
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  void shouldNotFailCallingToStringOverAnInvalidConnection() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    Connection c = ds.getConnection();
    c.close();
    c.toString();
  }

  @Test
  void ShouldReturnRealConnection() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    Connection c = ds.getConnection();
    JDBCConnection realConnection = (JDBCConnection) PooledDataSource.unwrapConnection(c);
    c.close();
  }

  @Disabled("See the comments")
  @Test
  void shouldReconnectWhenServerKilledLeakedConnection() throws Exception {
    // See #748
    // Requirements:
    // 1. MySQL JDBC driver dependency.
    // 2. MySQL server instance with the following.
    //  - CREATE DATABASE `test`;
    //  - SET GLOBAL wait_timeout=3;
    // 3. Tweak the connection info below.
    final String URL = "jdbc:mysql://localhost:3306/test";
    final String USERNAME = "admin";
    final String PASSWORD = "";

    PooledDataSource ds = new PooledDataSource();
    ds.setDriver("com.mysql.jdbc.Driver");
    ds.setUrl(URL);
    ds.setUsername(USERNAME);
    ds.setPassword(PASSWORD);
    ds.setPoolMaximumActiveConnections(1);
    ds.setPoolMaximumIdleConnections(1);
    ds.setPoolTimeToWait(1000);
    ds.setPoolMaximumCheckoutTime(2000);
    ds.setPoolPingEnabled(true);
    ds.setPoolPingQuery("select 1");
    ds.setDefaultAutoCommit(true);
    // MySQL wait_timeout * 1000 or less. (unit:ms)
    ds.setPoolPingConnectionsNotUsedFor(1000);

    Connection con = ds.getConnection();
    executeQuery(con);
    // Simulate connection leak by not closing.
    // con.close();

    // Wait for disconnected from mysql...
    Thread.sleep(TimeUnit.SECONDS.toMillis(3));

    con.close();

    // Should return usable connection.
    con = ds.getConnection();
    executeQuery(con);
    con.close();
  }

  private void executeQuery(Connection con) throws SQLException {
    try (PreparedStatement st = con.prepareStatement("select 1");
         ResultSet rs = st.executeQuery()) {
      while (rs.next()) {
        assertEquals(1, rs.getInt(1));
      }
    }
  }
}
