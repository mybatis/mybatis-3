/**
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.annotion_many_one_add_columnprefix;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OneManyColumnPrefixTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/annotion_many_one_add_columnprefix/SqlMapConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/annotion_many_one_add_columnprefix/CreateDB.sql");
  }

  @Test
  void shouldUseColumnPrefixWithMany() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll();
      assertNotNull(users);
      assertEquals(4, users.size());
      assertEquals(2, users.get(0).getRoles().size());
    }
  }

  @Test
  void shouldUseColumnPrefixInXmlWithMany() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll2();
      assertNotNull(users);
      assertEquals(4, users.size());
      assertEquals(2, users.get(0).getRoles().size());
    }
  }

  @Test
  void shouldUseColumnPrefixWithOne() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll3();
      assertNotNull(users);
      assertEquals(2, users.size());
      assertNotNull(users.get(0).getRole());
      assertEquals("teacher", users.get(0).getRole().getName());
    }
  }

  @Test
  void shouldResolveNestedColumnPrefix() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      User  user = mapper.findUserWithFriend(4);
      assertEquals(4, user.getId());
      assertEquals(2, user.getRoles().size());
      assertEquals("student", user.getRoles().get(0).getName());
      assertEquals("Learning-commissary", user.getRoles().get(1).getName());
      assertEquals(1, user.getFriend().getId());
      assertEquals(2, user.getFriend().getRoles().size());
      assertEquals("teacher", user.getFriend().getRoles().get(0).getName());
      assertEquals("Headmaster", user.getFriend().getRoles().get(1).getName());
    }
  }
}
