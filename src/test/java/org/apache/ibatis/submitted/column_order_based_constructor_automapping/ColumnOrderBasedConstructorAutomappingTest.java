/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.submitted.column_order_based_constructor_automapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.text.MessageFormat;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ColumnOrderBasedConstructorAutomappingTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader(
        "org/apache/ibatis/submitted/column_order_based_constructor_automapping/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().setArgNameBasedConstructorAutoMapping(false);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/column_order_based_constructor_automapping/CreateDB.sql");
  }

  @Test
  void shouldHandleNoArgsConstructor() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<UserNoArgsConstructor> userList = mapper.finaAllByNoArgsConstructor();

      assertEquals(2, userList.size());
      UserNoArgsConstructor user1 = userList.get(0);
      UserNoArgsConstructor user2 = userList.get(1);

      assertEquals(1, user1.getId());
      assertEquals("Tom", user1.getName());
      assertEquals(7, user1.getAge());
      assertNull(user1.getEmail());

      assertEquals(2, user2.getId());
      assertEquals("Cat", user2.getName());
      assertEquals(3, user2.getAge());
      assertNull(user2.getEmail());
    }
  }

  @Test
  void shouldHandleConstructorEqualsResultSet() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<UserConstructorEqualsResultSet> userList = mapper.finaAllByConstructorEqualsResultSet();

      assertEquals(2, userList.size());
      UserConstructorEqualsResultSet user1 = userList.get(0);
      UserConstructorEqualsResultSet user2 = userList.get(1);

      assertEquals(1, user1.getId());
      assertEquals("Tom", user1.getName());
      assertEquals(7, user1.getAge());
      assertNull(user1.getEmail());

      assertEquals(2, user2.getId());
      assertEquals("Cat", user2.getName());
      assertEquals(3, user2.getAge());
      assertNull(user2.getEmail());
    }
  }

  @Test
  void shouldHandleConstructorLessThanResultSet() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<UserConstructorLessThanResultSet> userList = mapper.finaAllByConstructorLessThanResultSet();

      assertEquals(2, userList.size());
      UserConstructorLessThanResultSet user1 = userList.get(0);
      UserConstructorLessThanResultSet user2 = userList.get(1);

      assertEquals(1, user1.getId());
      assertEquals("Tom", user1.getName());
      assertEquals(7, user1.getAge());
      assertNull(user1.getEmail());

      assertEquals(2, user2.getId());
      assertEquals("Cat", user2.getName());
      assertEquals(3, user2.getAge());
      assertNull(user2.getEmail());
    }
  }

  @Test
  void shouldNotHandleConstructorGreaterThanResultSet() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      PersistenceException persistenceException = assertThrows(PersistenceException.class,
          mapper::finaAllByConstructorGreaterThanResultSet);
      assertNotNull(persistenceException);
      String message = persistenceException.getMessage();
      assertNotNull(message);
      assertTrue(message.contains(MessageFormat.format(
          "Constructor auto-mapping of ''{0}'' failed. The constructor takes ''{1}'' arguments, but there are only ''{2}'' columns in the result set.",
          UserConstructorGreaterThanResultSet.class.getConstructors()[0], 4, 3)));
    }
  }
}
