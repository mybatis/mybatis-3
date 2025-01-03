/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.builder.BuilderException;
import org.junit.jupiter.api.Test;

class VendorDatabaseIdProviderTest {

  private static final String PRODUCT_NAME = "Chewbacca DB";

  @Test
  void shouldNpeBeThrownIfDataSourceIsNull() {
    VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
    try {
      provider.getDatabaseId(null);
      fail("Should NullPointerException be thrown.");
    } catch (NullPointerException e) {
      // pass
    }
  }

  @Test
  void shouldProductNameBeReturnedIfPropertiesIsNull() throws Exception {
    VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
    assertEquals(PRODUCT_NAME, provider.getDatabaseId(mockDataSource()));
  }

  @Test
  void shouldProductNameBeReturnedIfPropertiesIsEmpty() throws Exception {
    VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
    provider.setProperties(new Properties());
    assertEquals(PRODUCT_NAME, provider.getDatabaseId(mockDataSource()));
  }

  @Test
  void shouldProductNameBeTranslated() throws Exception {
    VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
    Properties properties = new Properties();
    String partialProductName = "Chewbacca";
    String id = "chewie";
    properties.put(partialProductName, id);
    provider.setProperties(properties);
    assertEquals(id, provider.getDatabaseId(mockDataSource()));
  }

  @Test
  void shouldNullBeReturnedIfNoMatch() throws Exception {
    VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
    Properties properties = new Properties();
    properties.put("Ewok DB", "ewok");
    provider.setProperties(properties);
    assertNull(provider.getDatabaseId(mockDataSource()));
  }

  @Test
  void shouldNullBeReturnedOnDbError() throws Exception {
    DataSource dataSource = mock(DataSource.class);
    when(dataSource.getConnection()).thenThrow(SQLException.class);

    VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
    Properties properties = new Properties();
    properties.put("Ewok DB", "ewok");
    try {
      provider.getDatabaseId(dataSource);
      fail("Should BuilderException be thrown.");
    } catch (BuilderException e) {
      // pass
    }
  }

  private DataSource mockDataSource() throws SQLException {
    DatabaseMetaData metaData = mock(DatabaseMetaData.class);
    when(metaData.getDatabaseProductName()).thenReturn(PRODUCT_NAME);
    Connection connection = mock(Connection.class);
    when(connection.getMetaData()).thenReturn(metaData);
    DataSource dataSource = mock(DataSource.class);
    when(dataSource.getConnection()).thenReturn(connection);
    return dataSource;
  }

}
