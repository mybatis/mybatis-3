/**
 *    Copyright 2009-2019 the original author or authors.
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

package org.apache.ibatis.cursor.defaults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetWrapper;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultCursorTest {
  @Spy
  private ImpatientResultSet rs;
  @Mock
  protected ResultSetMetaData rsmd;

  @SuppressWarnings("unchecked")
  @Test
  void shouldCloseImmediatelyIfResultSetIsClosed() throws Exception {
    final MappedStatement ms = getNestedAndOrderedMappedStatement();
    final ResultMap rm = ms.getResultMaps().get(0);

    final Executor executor = null;
    final ParameterHandler parameterHandler = null;
    final ResultHandler<?> resultHandler = null;
    final BoundSql boundSql = null;
    final RowBounds rowBounds = RowBounds.DEFAULT;

    final DefaultResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, ms, parameterHandler,
      resultHandler, boundSql, rowBounds);


    when(rsmd.getColumnCount()).thenReturn(2);
    doReturn("id").when(rsmd).getColumnLabel(1);
    doReturn(Types.INTEGER).when(rsmd).getColumnType(1);
    doReturn(Integer.class.getCanonicalName()).when(rsmd).getColumnClassName(1);
    doReturn("role").when(rsmd).getColumnLabel(2);
    doReturn(Types.VARCHAR).when(rsmd).getColumnType(2);
    doReturn(String.class.getCanonicalName()).when(rsmd).getColumnClassName(2);

    final ResultSetWrapper rsw = new ResultSetWrapper(rs, ms.getConfiguration());

    try (DefaultCursor<?> cursor = new DefaultCursor<>(resultSetHandler, rm, rsw, RowBounds.DEFAULT)) {
      Iterator<?> iter = cursor.iterator();
      assertTrue(iter.hasNext());
      Map<String, Object> map = (Map<String, Object>) iter.next();
      assertEquals(1, map.get("id"));
      assertEquals("CEO", ((Map<String, Object>) map.get("roles")).get("role"));

      assertFalse(cursor.isConsumed());
      assertTrue(cursor.isOpen());

      assertFalse(iter.hasNext());
      assertTrue(cursor.isConsumed());
      assertFalse(cursor.isOpen());
    }
  }

  @SuppressWarnings("serial")
  private MappedStatement getNestedAndOrderedMappedStatement() {
    final Configuration config = new Configuration();
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

    ResultMap nestedResultMap = new ResultMap.Builder(config, "roleMap", HashMap.class,
      new ArrayList<ResultMapping>() {
        {
          add(new ResultMapping.Builder(config, "role", "role", registry.getTypeHandler(String.class))
            .build());
        }
      }).build();
    config.addResultMap(nestedResultMap);

    return new MappedStatement.Builder(config, "selectPerson", new StaticSqlSource(config, "select person..."),
      SqlCommandType.SELECT).resultMaps(
        new ArrayList<ResultMap>() {
          {
            add(new ResultMap.Builder(config, "personMap", HashMap.class, new ArrayList<ResultMapping>() {
              {
                add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(Integer.class))
                  .build());
                add(new ResultMapping.Builder(config, "roles").nestedResultMapId("roleMap").build());
              }
            }).build());
          }
        })
        .resultOrdered(true)
        .build();
  }

  /*
   * Simulate a driver that closes ResultSet automatically when next() returns false (e.g. DB2).
   */
  protected abstract class ImpatientResultSet implements ResultSet {
    private int rowIndex = -1;
    private List<Map<String, Object>> rows = new ArrayList<>();

    protected ImpatientResultSet() {
      Map<String, Object> row = new HashMap<>();
      row.put("id", 1);
      row.put("role", "CEO");
      rows.add(row);
    }

    @Override
    public boolean next() throws SQLException {
      throwIfClosed();
      return ++rowIndex < rows.size();
    }

    @Override
    public boolean isClosed() {
      return rowIndex >= rows.size();
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
      throwIfClosed();
      return (String) rows.get(rowIndex).get(columnLabel);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
      throwIfClosed();
      return (Integer) rows.get(rowIndex).get(columnLabel);
    }

    @Override
    public boolean wasNull() throws SQLException {
      throwIfClosed();
      return false;
    }

    @Override
    public ResultSetMetaData getMetaData() {
      return rsmd;
    }

    @Override
    public int getType() throws SQLException {
      throwIfClosed();
      return ResultSet.TYPE_FORWARD_ONLY;
    }

    private void throwIfClosed() throws SQLException {
      if (rowIndex >= rows.size()) {
        throw new SQLException("Invalid operation: result set is closed.");
      }
    }
  }
}
