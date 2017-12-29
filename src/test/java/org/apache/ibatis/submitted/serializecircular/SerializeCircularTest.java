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
package org.apache.ibatis.submitted.serializecircular;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

//@Ignore("see issue #614")
public class SerializeCircularTest {

  @Test
  public void serializeAndDeserializeObjectsWithAggressiveLazyLoadingWithoutPreloadingAttribute() 
  throws Exception {
    SqlSession sqlSession = createSessionWithAggressiveLazyLoading();
    try {
      testSerializeWithoutPreloadingAttribute(sqlSession);
      } finally {
        sqlSession.close();
      }
  }
  
  @Test
  public void serializeAndDeserializeObjectsWithAggressiveLazyLoadingWithPreloadingAttribute() 
  throws Exception {
    SqlSession sqlSession = createSessionWithAggressiveLazyLoading();
    try {
      testSerializeWithPreloadingAttribute(sqlSession);
      } finally {
        sqlSession.close();
      }
  }

//  @Ignore("See http://code.google.com/p/mybatis/issues/detail?id=614")
  @Test
  public void serializeAndDeserializeObjectsWithoutAggressiveLazyLoadingWithoutPreloadingAttribute() 
  throws Exception {
    SqlSession sqlSession = createSessionWithoutAggressiveLazyLoading();
    try {
        //expected problem with deserializing
      testSerializeWithoutPreloadingAttribute(sqlSession);
      } finally {
        sqlSession.close();
      }
  }

  @Test
  public void serializeAndDeserializeObjectsWithoutAggressiveLazyLoadingWithPreloadingAttribute() 
  throws Exception {
    SqlSession sqlSession = createSessionWithoutAggressiveLazyLoading();
    try {
      testSerializeWithPreloadingAttribute(sqlSession);
      } finally {
        sqlSession.close();
      }
  }

  private SqlSession createSessionWithoutAggressiveLazyLoading() throws Exception {
    return createSession(false);
  }

  private SqlSession createSessionWithAggressiveLazyLoading() throws Exception {
    return createSession(true);
  }

  private SqlSession createSession(boolean anAggressiveLazyLoading) throws Exception {
    String xmlConfig = anAggressiveLazyLoading ?
        "org/apache/ibatis/submitted/serializecircular/MapperConfigWithAggressiveLazyLoading.xml":
        "org/apache/ibatis/submitted/serializecircular/MapperConfigWithoutAggressiveLazyLoading.xml";
      SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
      SqlSession sqlSession = sqlSessionFactory.openSession();
    return sqlSession;
  }

  private void testSerializeWithPreloadingAttribute(SqlSession sqlSession) {
    testSerialize(sqlSession, true);
  }

  private void testSerializeWithoutPreloadingAttribute(SqlSession sqlSession) {
    testSerialize(sqlSession, false);
  }

  private void testSerialize(SqlSession sqlSession, boolean aPreloadAttribute) {
    DepartmentMapper departmentMapper = sqlSession.getMapper(DepartmentMapper.class);
    Department department = departmentMapper.getById(1);
    if (aPreloadAttribute) {
      department.getAttribute();
    }

    serializeAndDeserializeObject(department);

    // This call results in problems when deserializing department
    department.getPerson();
    serializeAndDeserializeObject(department);
  }

  protected void serializeAndDeserializeObject(Object anObject) {
    UtilityTester.serializeAndDeserializeObject(anObject);
  }

  private SqlSessionFactory getSqlSessionFactoryXmlConfig(String resource) throws Exception {
    Reader configReader = Resources.getResourceAsReader(resource);
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
    configReader.close();

    Connection conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
    initDb(conn);
    conn.close();

    return sqlSessionFactory;
  }

  private static void initDb(Connection conn) throws IOException, SQLException {
    try {
      Reader scriptReader = Resources
          .getResourceAsReader("org/apache/ibatis/submitted/serializecircular/CreateDB.sql");
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(scriptReader);
      conn.commit();
      scriptReader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

}
