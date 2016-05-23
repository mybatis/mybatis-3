/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.submitted.array_result_type;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class ArrayResultTypeTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/array_result_type/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/array_result_type/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void shouldGetUserArray() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User[] users = mapper.getUsers();
      assertEquals("User1", users[0].getName());
      assertEquals("User2", users[1].getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetUserArrayXml() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User[] users = mapper.getUsersXml();
      assertEquals("User1", users[0].getName());
      assertEquals("User2", users[1].getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetSimpleTypeArray() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Integer[] ids = mapper.getUserIds();
      assertEquals(Integer.valueOf(1), ids[0]);
    } finally {
      sqlSession.close();
    }
  }

  @Test(expected = ClassCastException.class)
  public void shouldGetPrimitiveArray() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      // Throwing an exception is the expected behavior
      // until #555 is fixed
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      int[] ids = mapper.getUserIdsPrimitive();
      assertEquals(1, ids[0]);
    } finally {
      sqlSession.close();
    }
  }
}
