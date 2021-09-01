/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.annotion_many_one_add_resultmapid;

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

class OneManyResultMapTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/annotion_many_one_add_resultmapid/SqlMapConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/annotion_many_one_add_resultmapid/CreateDB.sql");
  }

  @Test
  void shouldUseResultMapWithMany() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll();
      assertNotNull(users);
      assertEquals(4, users.size());
      assertEquals(2, users.get(0).getRoles().size());
    }
  }

  @Test
  void shouldUseResultMapInXmlWithMany() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll2();
      assertNotNull(users);
      assertEquals(4, users.size());
      assertEquals(2, users.get(0).getRoles().size());
    }
  }

  @Test
  void shouldUseResultMapWithOne() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      List<User> users = mapper.findAll3();
      assertNotNull(users);
      assertEquals(2, users.size());
      assertNotNull(users.get(0).getRole());
      assertEquals("teacher", users.get(0).getRole().getRoleName());
    }
  }

  @Test
  void shouldResolveResultMapInTheSameNamespace() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserDao mapper = sqlSession.getMapper(UserDao.class);
      User headmaster = mapper.findHeadmaster();
      assertNotNull(headmaster);
      assertEquals(3, headmaster.getTeachers().size());
      assertEquals("Doug Lea", headmaster.getTeachers().get(0).getUsername());
    }
  }

}
