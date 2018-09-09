/**
 *    Copyright 2009-2018 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.cursor.defaults.DefaultCursor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultResultSetHandlerTest {

  @Mock
  private Statement stmt;
  @Mock
  private ResultSet rs;
  @Mock
  private ResultSetMetaData rsmd;
  @Mock
  private Connection conn;
  @Mock
  private DatabaseMetaData dbmd;
  
  /**
   * Test the behavior of calling {@link DefaultResultSetHandler#handleRowValues}
   * multiple times with a nested mapping that has ordered results.
   * This test replicates the behavior of the {@link DefaultCursor}.
   * 
   * Some database drivers (like DB2) do not allow any calls to the {@link ResultSet}
   * after the {@link ResultSet#next()} method has returned false. The Javadoc
   * mentions that implementations are allowed to throw {@link SQLException} in
   * this case. In this test we verify that we do not perform any more calls on
   * the {@link ResultSet} instance after the result set is closed when the end
   * was reached.
   */
  @Test
  public void shouldNotCallNextWhenResultSetIsClosedForNestedMapping() throws Exception {

    final MappedStatement ms = getNestedAndOrderedMappedStatement();
    final ResultMap rm = ms.getResultMaps().get(0);

    final Executor executor = null;
    final ParameterHandler parameterHandler = null;
    final TestResultHandler resultHandler = new TestResultHandler();
    final BoundSql boundSql = null;
    final RowBounds rowBounds = RowBounds.DEFAULT;
    
    final DefaultResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, ms, parameterHandler, resultHandler, boundSql, rowBounds);

    when(stmt.getResultSet()).thenReturn(rs);
    when(rs.getMetaData()).thenReturn(rsmd);
    when(rs.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
    //Simulate a JDBC driver that throws an SQL exception when the end of the result set was reached
    when(rs.next()).thenReturn(true).thenReturn(false).thenThrow(new SQLException("Not allowed to call next() after false was returned from previous invocation!"));
    when(rs.getInt("CoLuMn1")).thenReturn(100);
    when(rs.getInt("CoLuMn2")).thenReturn(200);
    when(rs.wasNull()).thenReturn(false);
    when(rsmd.getColumnCount()).thenReturn(2);
    when(rsmd.getColumnLabel(1)).thenReturn("CoLuMn1");
    when(rsmd.getColumnType(1)).thenReturn(Types.INTEGER);
    when(rsmd.getColumnClassName(1)).thenReturn(Integer.class.getCanonicalName());
    when(rsmd.getColumnLabel(2)).thenReturn("CoLuMn2");
    when(rsmd.getColumnType(2)).thenReturn(Types.INTEGER);
    when(rsmd.getColumnClassName(2)).thenReturn(Integer.class.getCanonicalName());
    when(stmt.getConnection()).thenReturn(conn);
    when(conn.getMetaData()).thenReturn(dbmd);
    when(dbmd.supportsMultipleResultSets()).thenReturn(false); // for simplicity.
    
    final ResultSetWrapper rsw = new ResultSetWrapper(rs, ms.getConfiguration());

    //Read the first and only record
    resultSetHandler.handleRowValues(rsw, rm, resultHandler, rowBounds, null);
    assertEquals(1, resultHandler.getResults().size());
    
    when(rs.isClosed()).thenReturn(true);
    when(rs.getType()).thenThrow(new SQLException("No interaction allowed when the result set is closed!"));
    
    //Call handleRowValues a second time like the DefaultCursor does to make sure no more results are present
    resultSetHandler.handleRowValues(rsw, rm, resultHandler, rowBounds, null);
    assertEquals(1, resultHandler.getResults().size());
    
    assertEquals(100, ((HashMap) resultHandler.getResults().get(0)).get("cOlUmN1"));
    assertEquals(200, ((HashMap) ((HashMap) resultHandler.getResults().get(0)).get("nestedData")).get("cOlUmN2"));
  }
  
  private static class TestResultHandler implements ResultHandler<Object> {
      private final List<Object> results = new ArrayList<>();
      
        @Override
        public void handleResult(ResultContext<? extends Object> resultContext) {
            results.add(resultContext.getResultObject());
            resultContext.stop();
        }
        
        public List<Object> getResults() {
            return results;
        }
  }

  /**
   * Contrary to the spec, some drivers require case-sensitive column names when getting result.
   * 
   * @see <a href="http://code.google.com/p/mybatis/issues/detail?id=557">Issue 557</a>
   */
  @Test
  public void shouldRetainColumnNameCase() throws Exception {

    final MappedStatement ms = getMappedStatement();

    final Executor executor = null;
    final ParameterHandler parameterHandler = null;
    final ResultHandler resultHandler = null;
    final BoundSql boundSql = null;
    final RowBounds rowBounds = new RowBounds(0, 100);
    final DefaultResultSetHandler fastResultSetHandler = new DefaultResultSetHandler(executor, ms, parameterHandler, resultHandler, boundSql, rowBounds);

    when(stmt.getResultSet()).thenReturn(rs);
    when(rs.getMetaData()).thenReturn(rsmd);
    when(rs.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
    when(rs.next()).thenReturn(true).thenReturn(false);
    when(rs.getInt("CoLuMn1")).thenReturn(100);
    when(rsmd.getColumnCount()).thenReturn(1);
    when(rsmd.getColumnLabel(1)).thenReturn("CoLuMn1");
    when(rsmd.getColumnType(1)).thenReturn(Types.INTEGER);
    when(rsmd.getColumnClassName(1)).thenReturn(Integer.class.getCanonicalName());
    when(stmt.getConnection()).thenReturn(conn);
    when(conn.getMetaData()).thenReturn(dbmd);
    when(dbmd.supportsMultipleResultSets()).thenReturn(false); // for simplicity.

    final List<Object> results = fastResultSetHandler.handleResultSets(stmt);
    assertEquals(1, results.size());
    assertEquals(Integer.valueOf(100), ((HashMap) results.get(0)).get("cOlUmN1"));
  }

  @Test
  public void shouldThrowExceptionWithColumnName() throws Exception {
    final MappedStatement ms = getMappedStatement();
    final RowBounds rowBounds = new RowBounds(0, 100);

    final DefaultResultSetHandler defaultResultSetHandler = new DefaultResultSetHandler(null/*executor*/, ms,
            null/*parameterHandler*/, null/*resultHandler*/, null/*boundSql*/, rowBounds);

    final ResultSetWrapper rsw = mock(ResultSetWrapper.class);
    when(rsw.getResultSet()).thenReturn(mock(ResultSet.class));

    final ResultMapping resultMapping = mock(ResultMapping.class);
    final TypeHandler typeHandler = mock(TypeHandler.class);
    when(resultMapping.getColumn()).thenReturn("column");
    when(resultMapping.getTypeHandler()).thenReturn(typeHandler);
    when(typeHandler.getResult(any(ResultSet.class), any(String.class))).thenThrow(new SQLException("exception"));
    List<ResultMapping> constructorMappings = Collections.singletonList(resultMapping);

    try {
      defaultResultSetHandler.createParameterizedResultObject(rsw, null/*resultType*/, constructorMappings,
              null/*constructorArgTypes*/, null/*constructorArgs*/, null/*columnPrefix*/);
      Assert.fail("Should have thrown ExecutorException");
    } catch (Exception e) {
      Assert.assertTrue("Expected ExecutorException", e instanceof ExecutorException);
      Assert.assertTrue("", e.getMessage().contains("mapping: " + resultMapping.toString()));
    }
  }

  MappedStatement getMappedStatement() {
    final Configuration config = new Configuration();
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    return new MappedStatement.Builder(config, "testSelect", new StaticSqlSource(config, "some select statement"), SqlCommandType.SELECT).resultMaps(
        new ArrayList<ResultMap>() {
          {
            add(new ResultMap.Builder(config, "testMap", HashMap.class, new ArrayList<ResultMapping>() {
              {
                add(new ResultMapping.Builder(config, "cOlUmN1", "CoLuMn1", registry.getTypeHandler(Integer.class)).build());
              }
            }).build());
          }
        }).build();
  }
  
  MappedStatement getNestedAndOrderedMappedStatement() {
    final Configuration config = new Configuration();
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    
    ResultMap nestedResultMap = new ResultMap.Builder(config, "nestedTestMap", HashMap.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "cOlUmN2", "CoLuMn2", registry.getTypeHandler(Integer.class)).build());
      }
    }).build();
    config.addResultMap(nestedResultMap);
      
    return new MappedStatement.Builder(config, "testSelect", new StaticSqlSource(config, "some select statement"), SqlCommandType.SELECT).resultMaps(
        new ArrayList<ResultMap>() {
          {
            add(new ResultMap.Builder(config, "testMap", HashMap.class, new ArrayList<ResultMapping>() {
              {
                add(new ResultMapping.Builder(config, "cOlUmN1", "CoLuMn1", registry.getTypeHandler(Integer.class)).build());
                add(new ResultMapping.Builder(config, "nestedData").nestedResultMapId("nestedTestMap").build());
              }
            }).build());
          }
        })
        .resultOrdered(true)
        .build();
  }

}
