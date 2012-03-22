/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.executor.resultset;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;

public class FastResultSetHandlerTest {
  private Mockery context = new JUnit4Mockery();

  /**
   * Contrary to the spec, some drivers require case-sensitive column names when getting result.
   * 
   * @see <a href="http://code.google.com/p/mybatis/issues/detail?id=557">Issue 557</a>
   */
  @Test
  public void shouldRetainColumnNameCase() throws Exception {
    final Configuration config = new Configuration();
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    final MappedStatement ms = new MappedStatement.Builder(config, "testSelect", new StaticSqlSource(config, "some select statement"), SqlCommandType.SELECT).resultMaps(new ArrayList<ResultMap>() {
      {
        add(new ResultMap.Builder(config, "testMap", HashMap.class, new ArrayList() {
          {
            add(new ResultMapping.Builder(config, "cOlUmN1", "CoLuMn1", registry.getTypeHandler(Integer.class)).build());
          }
        }).build());
      }
    }).build();

    final Executor executor = null;
    final ParameterHandler parameterHandler = null;
    final ResultHandler resultHandler = null;
    final BoundSql boundSql = null;
    final RowBounds rowBounds = new RowBounds(0, 100);
    final FastResultSetHandler fastResultSetHandler = new FastResultSetHandler(executor, ms, parameterHandler, resultHandler, boundSql, rowBounds);

    final Statement stmt = context.mock(Statement.class);
    final ResultSet rs = context.mock(ResultSet.class);
    final ResultSetMetaData rsmd = context.mock(ResultSetMetaData.class);
    final Connection conn = context.mock(Connection.class);
    final DatabaseMetaData dbmd = context.mock(DatabaseMetaData.class);

    context.checking(new Expectations() {
      {
        allowing(stmt).getResultSet();
        will(returnValue(rs));

        allowing(rs).getMetaData();
        will(returnValue(rsmd));
        allowing(rs).getType();
        will(returnValue(ResultSet.TYPE_FORWARD_ONLY));
        allowing(rs).close();
        oneOf(rs).next();
        will(returnValue(true));
        oneOf(rs).next();
        will(returnValue(false)); // just one row.
        allowing(rs).getInt("CoLuMn1");
        will(returnValue(100));
        allowing(rs).wasNull();
        will(returnValue(false));

        allowing(rsmd).getColumnCount();
        will(returnValue(1));
        allowing(rsmd).getColumnLabel(1);
        will(returnValue("CoLuMn1"));
        allowing(rsmd).getColumnType(1);
        will(returnValue(Types.INTEGER));
        allowing(rsmd).getColumnClassName(1);
        will(returnValue(Integer.class.getCanonicalName()));
        allowing(stmt).getConnection();
        will(returnValue(conn));

        allowing(conn).getMetaData();
        will(returnValue(dbmd));

        allowing(dbmd).supportsMultipleResultSets();
        will(returnValue(false)); // for simplicity.
      }
    });

    final List<Object> results = fastResultSetHandler.handleResultSets(stmt);
    assertEquals(1, results.size());
    assertEquals(Integer.valueOf(100), ((HashMap) results.get(0)).get("cOlUmN1"));
  }

}
