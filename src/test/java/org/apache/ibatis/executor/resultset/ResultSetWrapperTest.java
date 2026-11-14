/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.executor.resultset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.apache.ibatis.type.StringTypeHandler;
import org.junit.jupiter.api.Test;

class ResultSetWrapperTest {

  @Test
  void shouldResolveTypeHandlerForObjectPropertyWhenColumnClassNameIsNull() throws Exception {
    // e.g. auto-mapping to resultType="map" with a driver that returns null from getColumnClassName() (gh-699)
    ResultSetWrapper rsw = createResultSetWrapper(Types.VARCHAR, null);
    assertThat(rsw.getTypeHandler(Object.class, "col")).isInstanceOf(StringTypeHandler.class);
  }

  @Test
  void shouldFallBackToObjectTypeHandlerWhenPropertyTypeIsUnknown() throws Exception {
    // e.g. the key column of a nested select is a PostgreSQL uuid
    ResultSetWrapper rsw = createResultSetWrapper(Types.OTHER, "java.util.UUID");
    assertThat(rsw.getTypeHandler(null, "col")).isInstanceOf(ObjectTypeHandler.class);
  }

  private ResultSetWrapper createResultSetWrapper(int columnType, String columnClassName) throws SQLException {
    ResultSet rs = mock(ResultSet.class);
    ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
    when(rs.getMetaData()).thenReturn(rsmd);
    when(rsmd.getColumnCount()).thenReturn(1);
    when(rsmd.getColumnLabel(1)).thenReturn("col");
    when(rsmd.getColumnType(1)).thenReturn(columnType);
    when(rsmd.getColumnClassName(1)).thenReturn(columnClassName);
    return new ResultSetWrapper(rs, new Configuration());
  }

}
