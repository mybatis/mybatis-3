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
package org.apache.ibatis.submitted.cursor_nested;

import java.io.Reader;
import java.util.Iterator;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CursorNestedTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cursor_nested/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/cursor_nested/CreateDB.sql");
  }

  @Test
  void shouldGetAllUser() {
    Cursor<User> usersCursor;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      usersCursor = mapper.getAllUsers();

      Assertions.assertFalse(usersCursor.isOpen());
      // Retrieving iterator, fetching is not started
      Iterator<User> iterator = usersCursor.iterator();

      // Check if hasNext, fetching is started
      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(usersCursor.isOpen());
      Assertions.assertFalse(usersCursor.isConsumed());

      User user = iterator.next();
      Assertions.assertEquals(2, user.getGroups().size());
      Assertions.assertEquals(3, user.getRoles().size());

      user = iterator.next();
      Assertions.assertEquals(1, user.getGroups().size());
      Assertions.assertEquals(3, user.getRoles().size());

      user = iterator.next();
      Assertions.assertEquals(3, user.getGroups().size());
      Assertions.assertEquals(1, user.getRoles().size());

      user = iterator.next();
      Assertions.assertEquals(2, user.getGroups().size());
      Assertions.assertEquals(2, user.getRoles().size());

      Assertions.assertTrue(usersCursor.isOpen());
      Assertions.assertFalse(usersCursor.isConsumed());

      // Check no more elements
      Assertions.assertFalse(iterator.hasNext());
      Assertions.assertFalse(usersCursor.isOpen());
      Assertions.assertTrue(usersCursor.isConsumed());
    }
    Assertions.assertFalse(usersCursor.isOpen());
  }

  @Test
  void testCursorWithRowBound() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Cursor<User> usersCursor = sqlSession.selectCursor("getAllUsers", null, new RowBounds(2, 1));

      Iterator<User> iterator = usersCursor.iterator();

      Assertions.assertTrue(iterator.hasNext());
      User user = iterator.next();
      Assertions.assertEquals("User3", user.getName());
      Assertions.assertEquals(2, usersCursor.getCurrentIndex());

      Assertions.assertFalse(iterator.hasNext());
      Assertions.assertFalse(usersCursor.isOpen());
      Assertions.assertTrue(usersCursor.isConsumed());
    }
  }
}
