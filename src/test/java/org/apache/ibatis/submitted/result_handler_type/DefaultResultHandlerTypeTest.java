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
package org.apache.ibatis.submitted.result_handler_type;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

public class DefaultResultHandlerTypeTest {

  @Test
  public void testSelectList() throws Exception {
    String xmlConfig = "org/apache/ibatis/submitted/result_handler_type/MapperConfig.xml";
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Person> list = sqlSession
          .selectList("org.apache.ibatis.submitted.result_handler_type.PersonMapper.doSelect");
      assertEquals(list.size(), 2);
      assertEquals("java.util.LinkedList", list.getClass().getCanonicalName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSelectMap() throws Exception {
    String xmlConfig = "org/apache/ibatis/submitted/result_handler_type/MapperConfig.xml";
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Map<Integer, Person> map = sqlSession.selectMap(
          "org.apache.ibatis.submitted.result_handler_type.PersonMapper.doSelect", "id");
      assertEquals(map.size(), 2);
      assertEquals("java.util.LinkedHashMap", map.getClass().getCanonicalName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSelectMapAnnotation() throws Exception {
    String xmlConfig = "org/apache/ibatis/submitted/result_handler_type/MapperConfig.xml";
    SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      PersonMapper mapper = sqlSession.getMapper(PersonMapper.class);
      Map<Integer, Person> map = mapper.selectAsMap();
      assertEquals(map.size(), 2);
      assertEquals("java.util.LinkedHashMap", map.getClass().getCanonicalName());
    } finally {
      sqlSession.close();
    }
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
          .getResourceAsReader("org/apache/ibatis/submitted/result_handler_type/CreateDB.sql");
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
