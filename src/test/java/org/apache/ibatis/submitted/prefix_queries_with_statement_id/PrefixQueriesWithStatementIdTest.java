/*
 *    Copyright 2014 the original author or authors.
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
package org.apache.ibatis.submitted.prefix_queries_with_statement_id;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class PrefixQueriesWithStatementIdTest {
  private static final String resourcesPath = "org/apache/ibatis/submitted/prefix_queries_with_statement_id/";
  private static SqlSessionFactory sqlSessionFactory;
  private static Map<String, String> expectationMap;
  private static Map<String, Object> parameterMap;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader(resourcesPath + "mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader(resourcesPath + "CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();

    // prepare expectation map
    expectationMap = new HashMap<String, String>();
    expectationMap.put("getUserUsingParameterMarker", "select * from users where id = ?");
    expectationMap.put("getUserUsingStringSubstitution", "select * from users where id = 1");
    expectationMap.put("getUserUsingDynamicSQL", "select * from users where id = ?");

    // prepare parameter map
    parameterMap = new HashMap<String, Object>();
    parameterMap.put("id", 1);
  }

  @Test
  public void shouldPrefixXmlBasedMappers() {
    testPrefix("org.apache.ibatis.submitted.prefix_queries_with_statement_id.XmlBasedMapper");
  }

  @Test
  public void shouldPrefixAnnotatedMappers() {
    testPrefix("org.apache.ibatis.submitted.prefix_queries_with_statement_id.AnnotatedMapper");
  }

  private void testPrefix(String mapperNamespace) {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      for (Map.Entry<String, String> e : expectationMap.entrySet()) {
        String statementId = mapperNamespace + "." + e.getKey();
        String expected = "/* " + statementId + " */\n" + e.getValue();
        String actual = sqlSession.getConfiguration().getMappedStatement(statementId).getBoundSql(parameterMap).getSql();
        Assert.assertEquals(expected, actual.trim());
      }
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetAUserThroughXmlBasedMapper() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      XmlBasedMapper mapper = sqlSession.getMapper(XmlBasedMapper.class);
      Assert.assertEquals("User1", mapper.getUserUsingParameterMarker(parameterMap).getName());
      Assert.assertEquals("User1", mapper.getUserUsingStringSubstitution(parameterMap).getName());
      Assert.assertEquals("User1", mapper.getUserUsingDynamicSQL(parameterMap).getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetAUserThroughAnnotatedMapper() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      AnnotatedMapper mapper = sqlSession.getMapper(AnnotatedMapper.class);
      Assert.assertEquals("User1", mapper.getUserUsingParameterMarker(parameterMap).getName());
      Assert.assertEquals("User1", mapper.getUserUsingStringSubstitution(parameterMap).getName());
      Assert.assertEquals("User1", mapper.getUserUsingDynamicSQL(parameterMap).getName());
    } finally {
      sqlSession.close();
    }
  }
}
