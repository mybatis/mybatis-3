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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NestedResultSetHandlerTest {

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
     * Contrary to the spec, some drivers require case-sensitive column names
     * when getting result.
     *
     * @see <a
     *      href="http://code.google.com/p/mybatis/issues/detail?id=557">Issue
     *      557</a>
     */
  @Test
    public void shouldReturnAllObjects() throws Exception {

    final Configuration config = new Configuration();
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

    final ResultMap post = new ResultMap.Builder(config, "post", HashMap.class, new ArrayList<ResultMapping>() {
        {
            add(new ResultMapping.Builder(config, "id", "post_id", registry.getTypeHandler(Integer.class)).flags(Arrays.asList(ResultFlag.ID)).build());
            add(new ResultMapping.Builder(config, "subject", "post_subject", registry.getTypeHandler(String.class)).build());
        }
      }).build();
    final ResultMap blog = new ResultMap.Builder(config, "blog", HashMap.class, new ArrayList<ResultMapping>() {
        {
            add(new ResultMapping.Builder(config, "id", "blog_id", registry.getTypeHandler(Integer.class)).flags(Arrays.asList(ResultFlag.ID)).build());
            add(new ResultMapping.Builder(config, "title", "blog_title", registry.getTypeHandler(String.class)).build());
            add(new ResultMapping.Builder(config, "posts").javaType(List.class).nestedResultMapId("post").build());
        }
      }).build();
    config.addResultMap(post);
    config.addResultMap(blog);

    final MappedStatement ms = new MappedStatement.Builder(config, "testSelect", new StaticSqlSource(config, "some select statement"), SqlCommandType.SELECT).resultMaps(
        new ArrayList<ResultMap>() {
          {
            add(blog);
          }
        }).build();

    final Executor executor = null;
    final ParameterHandler parameterHandler = null;
    final ResultHandler resultHandler = null;
    final BoundSql boundSql = null;
    final RowBounds rowBounds = new RowBounds(0, 100);

    final NestedResultSetHandler nestedResultSetHandler = new NestedResultSetHandler(executor, ms, parameterHandler, resultHandler, boundSql, rowBounds);

    final int[] pointer = new int[] { -1 };
    final Object[][] values = new Object[][] {
        { new Integer(1), "main1",  new Integer(1), "a" },
        { new Integer(2), "main2",  new Integer(2), "b" },
        { new Integer(1), "main1",  new Integer(4), "d" },
    };

    when(stmt.getResultSet()).thenReturn(rs);
    when(rs.getMetaData()).thenReturn(rsmd);
    when(rs.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
    when(rs.next()).thenAnswer(new Answer<Boolean>() {
        @Override
        public Boolean answer(InvocationOnMock invocation) throws Throwable {
            pointer[0]++;
            return pointer[0] < values.length;
        }
    });
    when(rs.getInt("blog_id")).thenAnswer(new Answer<Integer>() {
        @Override
        public Integer answer(InvocationOnMock invocation) throws Throwable {
            return (Integer) values[pointer[0]][0];
        }
    });
    when(rs.getString("blog_title")).thenAnswer(new Answer<String>() {
        @Override
        public String answer(InvocationOnMock invocation) throws Throwable {
            return (String) values[pointer[0]][1];
        }
    });
    when(rs.getInt("post_id")).thenAnswer(new Answer<Integer>() {
        @Override
        public Integer answer(InvocationOnMock invocation) throws Throwable {
            return (Integer) values[pointer[0]][2];
        }
    });
    when(rs.getString("post_subject")).thenAnswer(new Answer<String>() {
        @Override
        public String answer(InvocationOnMock invocation) throws Throwable {
            return (String) values[pointer[0]][3];
        }
    });
    when(rs.wasNull()).thenReturn(false);
    when(rsmd.getColumnCount()).thenReturn(4);
    when(rsmd.getColumnLabel(1)).thenReturn("blog_id");
    when(rsmd.getColumnLabel(2)).thenReturn("blog_title");
    when(rsmd.getColumnLabel(3)).thenReturn("post_id");
    when(rsmd.getColumnLabel(4)).thenReturn("post_subject");
    when(rsmd.getColumnType(1)).thenReturn(Types.INTEGER);
    when(rsmd.getColumnType(2)).thenReturn(Types.VARCHAR);
    when(rsmd.getColumnType(3)).thenReturn(Types.INTEGER);
    when(rsmd.getColumnType(4)).thenReturn(Types.VARCHAR);
    when(rsmd.getColumnClassName(1)).thenReturn(Integer.class.getCanonicalName());
    when(rsmd.getColumnClassName(2)).thenReturn(String.class.getCanonicalName());
    when(rsmd.getColumnClassName(3)).thenReturn(Integer.class.getCanonicalName());
    when(rsmd.getColumnClassName(4)).thenReturn(String.class.getCanonicalName());
    when(stmt.getConnection()).thenReturn(conn);
    when(conn.getMetaData()).thenReturn(dbmd);
    when(dbmd.supportsMultipleResultSets()).thenReturn(false); // for simplicity.

    final List<Object> results = nestedResultSetHandler.handleResultSets(stmt);
    assertEquals(2, results.size());
    assertNotSame("Results contain duplicates", results.get(1), results.get(0));
  }
}
