/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.jdbc.usesjava8;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.test.EmbeddedMysqlTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

@Category(EmbeddedMysqlTests.class)
public class PooledDataSourceMysqlTest extends BaseDataTest {

  private static EmbeddedMysql mysql;

  @BeforeClass
  public static void startMySql() throws IOException {
    MysqldConfig config = MysqldConfig.aMysqldConfig(Version.v5_7_latest)
      .withFreePort()
      .withServerVariable("wait_timeout", 3)
      .build();
    mysql = EmbeddedMysql.anEmbeddedMysql(config)
      .addSchema("test")
      .start();
  }

  @AfterClass
  public static void stopMySql() {
    if (mysql != null) {
      mysql.stop();
    }
  }

  @Test
  public void shouldReconnectWhenServerKilledLeakedConnection() throws Exception {
    final String URL = String.format("jdbc:mysql://localhost:%d/test", mysql.getConfig().getPort());
    final String USERNAME = mysql.getConfig().getUsername();
    final String PASSWORD = mysql.getConfig().getPassword();

    PooledDataSource ds = new PooledDataSource();
    ds.setDriver("com.mysql.cj.jdbc.Driver");
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

    try (Connection con = ds.getConnection()) {
      executeQueryAndAssertion(con);
      // Simulate connection leak by not closing.
      // con.close();

      // Wait for disconnected from mysql...
      TimeUnit.SECONDS.sleep(3);
    }

    // Should return usable connection.
    try(Connection con = ds.getConnection()) {
      executeQueryAndAssertion(con);
    }
  }

  private void executeQueryAndAssertion(Connection con) throws SQLException {
    try(PreparedStatement st = con.prepareStatement("select 1");
        ResultSet rs = st.executeQuery()) {
      while (rs.next()) {
        assertEquals(1, rs.getInt(1));
      }
    }
  }
}
