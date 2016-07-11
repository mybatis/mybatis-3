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
package org.apache.ibatis.submitted.usesjava8.default_method;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.usesjava8.default_method.Mapper.SubMapper;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultMethodTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader(
        "org/apache/ibatis/submitted/usesjava8/default_method/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader(
        "org/apache/ibatis/submitted/usesjava8/default_method/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void shouldInvokeDefaultMethod() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.defaultGetUser(1);
      assertEquals("User1", user.getName());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldInvokeDefaultMethodOfSubclass() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SubMapper mapper = sqlSession.getMapper(SubMapper.class);
      User user = mapper.defaultGetUser("User1", 1);
      assertEquals("User1", user.getName());
    } finally {
      sqlSession.close();
    }
  }
}
