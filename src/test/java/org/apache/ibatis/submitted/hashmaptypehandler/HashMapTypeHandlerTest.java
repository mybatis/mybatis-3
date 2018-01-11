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
package org.apache.ibatis.submitted.hashmaptypehandler;

import java.io.Reader;
import java.sql.Connection;
import java.util.HashMap;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HashMapTypeHandlerTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/hashmaptypehandler/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/hashmaptypehandler/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Test
  public void shouldNotApplyTypeHandlerToParamMap() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1, "User1");
      Assert.assertEquals("User1", user.getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldNotApplyTypeHandlerToParamMapXml() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserXml(1, "User1");
      Assert.assertEquals("User1", user.getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldApplyHashMapTypeHandler() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      HashMap<String, String> map = new HashMap<String, String>();
      map.put("name", "User1");
      User user = mapper.getUserWithTypeHandler(map);
      Assert.assertNotNull(user);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldApplyHashMapTypeHandlerXml() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      HashMap<String, String> map = new HashMap<String, String>();
      map.put("name", "User1");
      User user = mapper.getUserWithTypeHandlerXml(map);
      Assert.assertNotNull(user);
    } finally {
      sqlSession.close();
    }
  }
}
