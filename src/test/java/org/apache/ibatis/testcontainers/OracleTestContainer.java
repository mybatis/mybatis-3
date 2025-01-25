/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.testcontainers;

import javax.sql.DataSource;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

@Testcontainers
public final class OracleTestContainer {

  private static final String DB_NAME = "mybatis_test";
  private static final String USERNAME = "u";
  private static final String PASSWORD = "p";
  private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

  @Container
  private static final OracleContainer INSTANCE = initContainer();

  private static OracleContainer initContainer() {
    @SuppressWarnings("resource")
    var container = new OracleContainer("gvenzl/oracle-free:slim-faststart").withDatabaseName(DB_NAME)
        .withUsername(USERNAME).withPassword(PASSWORD);
    container.start();
    return container;
  }

  public static DataSource getUnpooledDataSource() {
    return new UnpooledDataSource(OracleTestContainer.DRIVER, INSTANCE.getJdbcUrl(), OracleTestContainer.USERNAME,
        OracleTestContainer.PASSWORD);
  }

  public static PooledDataSource getPooledDataSource() {
    return new PooledDataSource(OracleTestContainer.DRIVER, INSTANCE.getJdbcUrl(), OracleTestContainer.USERNAME,
        OracleTestContainer.PASSWORD);
  }

  private OracleTestContainer() {
  }
}
