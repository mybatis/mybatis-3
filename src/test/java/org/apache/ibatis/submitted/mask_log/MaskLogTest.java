/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.mask_log;

import static org.apache.ibatis.submitted.mask_log.StringBuilderLogImpl.LOG_CONTENT;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MaskLogTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/mask_log/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/mask_log/CreateDB.sql");
  }

  @Test
  void shouldGetAUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1, "Number1", "Password1");
      Assertions.assertEquals("User1", user.getName());
      String logContent = LOG_CONTENT.toString();
      Assertions.assertTrue(logContent.contains("1(Integer), Nu***r1, Pas****d1"));
      Assertions.assertTrue(logContent.contains("1, User1, Nu***r1, Pas****d1"));
    }
  }

  @Test
  void shouldGetUsers() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsers(Lists.newArrayList("Number1", "Number2", "Number3"),
          new String[] { "Password1", "Password2", "Password3" });
      Assertions.assertEquals(3, users.size());
      String logContent = LOG_CONTENT.toString();
      Assertions.assertTrue(logContent.contains("Nu***r1, Nu***r2, Nu***r3, Pas****d1, Pas****d2, Pas****d3"));
      Assertions.assertTrue(logContent.contains("1, User1, Nu***r1, Pas****d1"));
      Assertions.assertTrue(logContent.contains("2, User2, Nu***r2, Pas****d2"));
      Assertions.assertTrue(logContent.contains("3, User3, Nu***r3, Pas****d3"));
    }
  }

  @Test
  void shouldInsertAUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(4);
      user.setName("User4");
      user.setNumber("Number4");
      user.setPassword("Password4");
      Assertions.assertEquals(1, mapper.insertUser(user));
      String logContent = LOG_CONTENT.toString();
      Assertions.assertTrue(logContent.contains("4(Integer), User4(String), Nu***r4, Pas****d4"));
    }
  }

  @Test
  void shouldInsertUsers() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = new ArrayList<>();
      for (int i = 4; i < 7; i++) {
        User user = new User();
        user.setId(i);
        user.setName("User" + i);
        user.setNumber("Number" + i);
        user.setPassword("Password" + i);
        users.add(user);
      }
      Assertions.assertEquals(3, mapper.insertUsers(users));
      String logContent = LOG_CONTENT.toString();
      Assertions.assertTrue(logContent.contains("4(Integer), User4(String), Nu***r4, Pas****d4, "
          + "5(Integer), User5(String), Nu***r5, Pas****d5, 6(Integer), User6(String), Nu***r6, Pas****d6"));
    }
  }

}
