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
package org.apache.ibatis.datasource.unpooled.usesjava8;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.test.EmbeddedMysqlTests;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.sql.Connection;

import static org.apache.ibatis.datasource.unpooled.UnpooledDataSourceTestSupport.countRegisteredDrivers;
import static org.junit.Assert.assertEquals;

@Category(EmbeddedMysqlTests.class)
public class UnpooledDataSourceMysqlTest {

  private static EmbeddedMysql mysql;

  @BeforeClass
  public static void startMySql() throws IOException {
    MysqldConfig config = MysqldConfig.aMysqldConfig(Version.v5_7_latest)
      .withFreePort()
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
  public void shouldRegisterDynamicallyLoadedDriver() throws Exception {
    final String url = String.format("jdbc:mysql://localhost:%d/test", mysql.getConfig().getPort());
    final String username = mysql.getConfig().getUsername();
    final String password = mysql.getConfig().getPassword();

    int before = countRegisteredDrivers();
    ClassLoader driverClassLoader = getClass().getClassLoader();

    UnpooledDataSource dataSource = new UnpooledDataSource(driverClassLoader, "com.mysql.jdbc.Driver", url, username, password);
    try (Connection con = dataSource.getConnection()) {
      assertEquals(before + 1, countRegisteredDrivers());
    }

    dataSource = new UnpooledDataSource(driverClassLoader, "com.mysql.jdbc.Driver", url, username, password);
    try (Connection con = dataSource.getConnection()) {
      assertEquals(before + 1, countRegisteredDrivers());
    }
  }

}
