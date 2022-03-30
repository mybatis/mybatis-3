/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.datasource.unpooled;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.testcontainers.PgContainer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("TestcontainersTests")
class NetworkTimeoutTest {

  @Test
  void testNetworkTimeout_UnpooledDataSource() throws Exception {
    UnpooledDataSource dataSource = (UnpooledDataSource) PgContainer.getUnpooledDataSource();
    dataSource.setDefaultNetworkTimeout(5000);
    try (Connection connection = dataSource.getConnection()) {
      assertEquals(5000, connection.getNetworkTimeout());
    }
  }

  @Test
  void testNetworkTimeout_PooledDataSource() throws Exception {
    UnpooledDataSource unpooledDataSource = (UnpooledDataSource) PgContainer.getUnpooledDataSource();
    PooledDataSource dataSource = new PooledDataSource(unpooledDataSource);
    dataSource.setDefaultNetworkTimeout(5000);
    try (Connection connection = dataSource.getConnection()) {
      assertEquals(5000, connection.getNetworkTimeout());
    }
  }

}
