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
package org.apache.ibatis.submitted.results_id;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResultsIdTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/results_id/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/results_id/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Test
  public void testNamingResults() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserByName("User2");
      assertEquals(Integer.valueOf(2), user.getId());
      assertEquals("User2", user.getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testResultsOnlyForNaming() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserByNameConstructor("User2");
      assertEquals(Integer.valueOf(2), user.getId());
      assertEquals("User2", user.getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testReuseNamedResultsFromAnotherMapper() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      AnotherMapper mapper = sqlSession.getMapper(AnotherMapper.class);
      List<User> users = mapper.getUsers();
      assertEquals(2, users.size());
      assertEquals(Integer.valueOf(1), users.get(0).getId());
      assertEquals("User1", users.get(0).getName());
      assertEquals(Integer.valueOf(2), users.get(1).getId());
      assertEquals("User2", users.get(1).getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testReuseNamedResultsFromXmlMapper() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      AnotherMapper mapper = sqlSession.getMapper(AnotherMapper.class);
      User user = mapper.getUser(1);
      assertEquals(Integer.valueOf(1), user.getId());
      assertEquals("User1", user.getName());
    } finally {
      sqlSession.close();
    }
  }
}
