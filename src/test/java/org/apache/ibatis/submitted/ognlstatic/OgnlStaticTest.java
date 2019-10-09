/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.ognlstatic;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OgnlStaticTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/ognlstatic/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/ognlstatic/CreateDB.sql");
  }

  /**
   * This is the log output.
   * DEBUG [main] - ooo Using Connection [org.hsqldb.jdbc.JDBCConnection@5ae1a5c7]
   * DEBUG [main] - ==>  Preparing: SELECT * FROM users WHERE name IN (?) AND id = ?
   * DEBUG [main] - ==> Parameters: 1(Integer), 1(Integer)
   * There are two parameter mappings but DefaulParameterHandler maps them both to input paremeter (integer)
   */
  @Test // see issue #448
  void shouldGetAUserStatic() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserStatic(1);
      Assertions.assertNotNull(user);
      Assertions.assertEquals("User1", user.getName());
    }
  }

  @Test // see issue #61 (gh)
  void shouldGetAUserWithIfNode() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserIfNode("User1");
      Assertions.assertEquals("User1", user.getName());
    }
  }

}
