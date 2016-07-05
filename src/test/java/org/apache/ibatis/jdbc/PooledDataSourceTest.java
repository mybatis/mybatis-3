/**
 *    Copyright 2009-2015 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.hsqldb.jdbc.JDBCConnection;
import org.junit.Test;

public class PooledDataSourceTest extends BaseDataTest {

  @Test
  public void shouldProperlyMaintainPoolOf3ActiveAnd2IdleConnections() throws Exception {
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
      List<Connection> connections = new ArrayList<Connection>();
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
  public void shouldNotFailCallingToStringOverAnInvalidConnection() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    Connection c = ds.getConnection();
    c.close();
    c.toString();
  }
  
  @Test
  public void ShouldReturnRealConnection() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    Connection c = ds.getConnection();
    JDBCConnection realConnection = (JDBCConnection) PooledDataSource.unwrapConnection(c);
  }
  
  @Test
  public void pingConnection() throws Exception {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    ds.setPoolPingConnectionsNotUsedFor(0);
    ds.setPoolPingEnabled(true);
    ds.setPoolPingQuery("SELECT * FROM PRODUCT");
    Connection c = ds.getConnection();
    c.close();
  }
  
}
