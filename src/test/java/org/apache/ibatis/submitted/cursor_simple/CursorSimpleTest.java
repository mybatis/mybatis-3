/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.cursor_simple;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

class CursorSimpleTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cursor_simple/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/cursor_simple/CreateDB.sql");
  }

  @Test
  void shouldGetAllUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Cursor<User> usersCursor = mapper.getAllUsers();

      Assertions.assertFalse(usersCursor.isOpen());

      // Cursor is just created, current index is -1
      Assertions.assertEquals(-1, usersCursor.getCurrentIndex());

      Iterator<User> iterator = usersCursor.iterator();

      // Check if hasNext, fetching is started
      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(usersCursor.isOpen());
      Assertions.assertFalse(usersCursor.isConsumed());

      // next() has not been called, index is still -1
      Assertions.assertEquals(-1, usersCursor.getCurrentIndex());

      User user = iterator.next();
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals(0, usersCursor.getCurrentIndex());

      user = iterator.next();
      Assertions.assertEquals("User2", user.getName());
      Assertions.assertEquals(1, usersCursor.getCurrentIndex());

      user = iterator.next();
      Assertions.assertEquals("User3", user.getName());
      Assertions.assertEquals(2, usersCursor.getCurrentIndex());

      user = iterator.next();
      Assertions.assertEquals("User4", user.getName());
      Assertions.assertEquals(3, usersCursor.getCurrentIndex());

      user = iterator.next();
      Assertions.assertEquals("User5", user.getName());
      Assertions.assertEquals(4, usersCursor.getCurrentIndex());

      // Check no more elements
      Assertions.assertFalse(iterator.hasNext());
      Assertions.assertFalse(usersCursor.isOpen());
      Assertions.assertTrue(usersCursor.isConsumed());
    }
  }

  @Test
  void testCursorClosedOnSessionClose() {
    Cursor<User> usersCursor;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      usersCursor = mapper.getAllUsers();

      Assertions.assertFalse(usersCursor.isOpen());

      Iterator<User> iterator = usersCursor.iterator();

      // Check if hasNext, fetching is started
      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(usersCursor.isOpen());
      Assertions.assertFalse(usersCursor.isConsumed());

      // Consume only the first result
      User user = iterator.next();
      Assertions.assertEquals("User1", user.getName());

      // Check there is still remaining elements
      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(usersCursor.isOpen());
      Assertions.assertFalse(usersCursor.isConsumed());
    }

    // The cursor was not fully consumed, but it should be close since we closed the session
    Assertions.assertFalse(usersCursor.isOpen());
    Assertions.assertFalse(usersCursor.isConsumed());
  }

  @Test
  void testCursorWithRowBound() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      // RowBound starting at offset 1 and limiting to 2 items
      Cursor<User> usersCursor = sqlSession.selectCursor("getAllUsers", null, new RowBounds(1, 3));

      Iterator<User> iterator = usersCursor.iterator();

      User user = iterator.next();
      Assertions.assertEquals("User2", user.getName());
      Assertions.assertEquals(1, usersCursor.getCurrentIndex());

      // Calling hasNext() before next()
      Assertions.assertTrue(iterator.hasNext());
      user = iterator.next();
      Assertions.assertEquals("User3", user.getName());
      Assertions.assertEquals(2, usersCursor.getCurrentIndex());

      // Calling next() without a previous hasNext() call
      user = iterator.next();
      Assertions.assertEquals("User4", user.getName());
      Assertions.assertEquals(3, usersCursor.getCurrentIndex());

      Assertions.assertFalse(iterator.hasNext());
      Assertions.assertFalse(usersCursor.isOpen());
      Assertions.assertTrue(usersCursor.isConsumed());
    }
  }

  @Test
  void testCursorIteratorNoSuchElementExceptionWithHasNext() throws IOException {

    try (SqlSession sqlSession = sqlSessionFactory.openSession();
        Cursor<User> usersCursor = sqlSession.selectCursor("getAllUsers", null, new RowBounds(1, 1))) {
      try {
        Iterator<User> iterator = usersCursor.iterator();

        User user = iterator.next();
        Assertions.assertEquals("User2", user.getName());
        Assertions.assertEquals(1, usersCursor.getCurrentIndex());

        Assertions.assertFalse(iterator.hasNext());
        iterator.next();
        Assertions.fail("We should have failed since we call next() when hasNext() returned false");
      } catch (NoSuchElementException e) {
        Assertions.assertFalse(usersCursor.isOpen());
        Assertions.assertTrue(usersCursor.isConsumed());
      }
    }
  }

  @Test
  void testCursorIteratorNoSuchElementExceptionNoHasNext() throws IOException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession();
        Cursor<User> usersCursor = sqlSession.selectCursor("getAllUsers", null, new RowBounds(1, 1))) {
      try {
        Iterator<User> iterator = usersCursor.iterator();
        User user = iterator.next();
        Assertions.assertEquals("User2", user.getName());
        Assertions.assertEquals(1, usersCursor.getCurrentIndex());

        // Trying next() without hasNext()
        iterator.next();
        Assertions.fail("We should have failed since we call next() when is no more items");
      } catch (NoSuchElementException e) {
        Assertions.assertFalse(usersCursor.isOpen());
        Assertions.assertTrue(usersCursor.isConsumed());
      }
    }
  }

  @Test
  void testCursorWithBadRowBound() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      // Trying to start at offset 10 (which does not exist, since there is only 4 items)
      Cursor<User> usersCursor = sqlSession.selectCursor("getAllUsers", null, new RowBounds(10, 2));
      Iterator<User> iterator = usersCursor.iterator();

      Assertions.assertFalse(iterator.hasNext());
      Assertions.assertFalse(usersCursor.isOpen());
      Assertions.assertTrue(usersCursor.isConsumed());
    }
  }

  @Test
  void testCursorMultipleHasNextCall() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Cursor<User> usersCursor = mapper.getAllUsers();

      Iterator<User> iterator = usersCursor.iterator();

      Assertions.assertEquals(-1, usersCursor.getCurrentIndex());

      User user = iterator.next();
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals(0, usersCursor.getCurrentIndex());

      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(iterator.hasNext());
      // assert that index has not changed after hasNext() call
      Assertions.assertEquals(0, usersCursor.getCurrentIndex());
    }
  }

  @Test
  void testCursorMultipleIteratorCall() {
    Iterator<User> iterator2 = null;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Cursor<User> usersCursor = mapper.getAllUsers();

      Iterator<User> iterator = usersCursor.iterator();
      User user = iterator.next();
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals(0, usersCursor.getCurrentIndex());

      iterator2 = usersCursor.iterator();
      iterator2.hasNext();
      Assertions.fail("We should have failed since calling iterator several times is not allowed");
    } catch (IllegalStateException e) {
      Assertions.assertNull(iterator2, "iterator2 should be null");
      return;
    }
    Assertions.fail("Should have returned earlier");
  }

  @Test
  void testCursorMultipleCloseCall() throws IOException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Cursor<User> usersCursor = mapper.getAllUsers();

      Assertions.assertFalse(usersCursor.isOpen());

      Iterator<User> iterator = usersCursor.iterator();

      // Check if hasNext, fetching is started
      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(usersCursor.isOpen());
      Assertions.assertFalse(usersCursor.isConsumed());

      // Consume only the first result
      User user = iterator.next();
      Assertions.assertEquals("User1", user.getName());

      usersCursor.close();
      // Check multiple close are no-op
      usersCursor.close();

      // hasNext now return false, since the cursor is closed
      Assertions.assertFalse(iterator.hasNext());
      Assertions.assertFalse(usersCursor.isOpen());
      Assertions.assertFalse(usersCursor.isConsumed());
    }
  }

  @Test
  void testCursorUsageAfterClose() throws IOException {

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      Cursor<User> usersCursor = mapper.getAllUsers();
      try {
        Iterator<User> iterator = usersCursor.iterator();
        User user = iterator.next();
        Assertions.assertEquals("User1", user.getName());
        Assertions.assertEquals(0, usersCursor.getCurrentIndex());

        user = iterator.next();
        Assertions.assertEquals("User2", user.getName());
        Assertions.assertEquals(1, usersCursor.getCurrentIndex());

        usersCursor.close();

        // hasNext now return false, since the cursor is closed
        Assertions.assertFalse(iterator.hasNext());
        Assertions.assertFalse(usersCursor.isOpen());
        Assertions.assertFalse(usersCursor.isConsumed());

        // trying next() will fail
        iterator.next();

        Assertions.fail("We should have failed with NoSuchElementException since Cursor is closed");
      } catch (NoSuchElementException e) {
        // We had an exception and current index has not changed
        Assertions.assertEquals(1, usersCursor.getCurrentIndex());
        usersCursor.close();
        return;
      }
    }

    Assertions.fail("Should have returned earlier");
  }

  @Test
  void shouldGetAllUserUsingAnnotationBasedMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      sqlSession.getConfiguration().getMapperRegistry().addMapper(AnnotationMapper.class);
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      Cursor<User> usersCursor = mapper.getAllUsers();

      Assertions.assertFalse(usersCursor.isOpen());
      Assertions.assertFalse(usersCursor.isConsumed());
      Assertions.assertEquals(-1, usersCursor.getCurrentIndex());

      List<User> userList = new ArrayList<>();
      for (User user : usersCursor) {
        userList.add(user);
        Assertions.assertEquals(userList.size() - 1, usersCursor.getCurrentIndex());
      }

      Assertions.assertFalse(usersCursor.isOpen());
      Assertions.assertTrue(usersCursor.isConsumed());
      Assertions.assertEquals(4, usersCursor.getCurrentIndex());

      Assertions.assertEquals(5, userList.size());
      User user = userList.get(0);
      Assertions.assertEquals("User1", user.getName());
      user = userList.get(1);
      Assertions.assertEquals("User2", user.getName());
      user = userList.get(2);
      Assertions.assertEquals("User3", user.getName());
      user = userList.get(3);
      Assertions.assertEquals("User4", user.getName());
      user = userList.get(4);
      Assertions.assertEquals("User5", user.getName());
    }
  }

  @Test
  void shouldThrowIllegalStateExceptionUsingIteratorOnSessionClosed() {
    Cursor<User> usersCursor;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      usersCursor = sqlSession.getMapper(Mapper.class).getAllUsers();
    }
    try {
      usersCursor.iterator();
      Assertions.fail("Should throws the IllegalStateException when call the iterator method after session is closed.");
    } catch (IllegalStateException e) {
      Assertions.assertEquals("A Cursor is already closed.", e.getMessage());
    }

    // verify for checking order
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      usersCursor = sqlSession.getMapper(Mapper.class).getAllUsers();
      usersCursor.iterator();
    }
    try {
      usersCursor.iterator();
      Assertions.fail("Should throws the IllegalStateException when call the iterator already.");
    } catch (IllegalStateException e) {
      Assertions.assertEquals("Cannot open more than one iterator on a Cursor", e.getMessage());
    }

  }

  @Test
  void shouldNullItemNotStopIteration() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Cursor<User> cursor = mapper.getNullUsers(new RowBounds());
      Iterator<User> iterator = cursor.iterator();

      Assertions.assertFalse(cursor.isOpen());

      // Cursor is just created, current index is -1
      Assertions.assertEquals(-1, cursor.getCurrentIndex());

      // Check if hasNext, fetching is started
      Assertions.assertTrue(iterator.hasNext());
      // Re-invoking hasNext() should not fetch the next row
      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(cursor.isOpen());
      Assertions.assertFalse(cursor.isConsumed());

      // next() has not been called, index is still -1
      Assertions.assertEquals(-1, cursor.getCurrentIndex());

      User user;
      user = iterator.next();
      Assertions.assertNull(user);
      Assertions.assertEquals(0, cursor.getCurrentIndex());

      Assertions.assertTrue(iterator.hasNext());
      user = iterator.next();
      Assertions.assertEquals("Kate", user.getName());
      Assertions.assertEquals(1, cursor.getCurrentIndex());

      Assertions.assertTrue(iterator.hasNext());
      user = iterator.next();
      Assertions.assertNull(user);
      Assertions.assertEquals(2, cursor.getCurrentIndex());

      Assertions.assertTrue(iterator.hasNext());
      user = iterator.next();
      Assertions.assertNull(user);
      Assertions.assertEquals(3, cursor.getCurrentIndex());

      // Check no more elements
      Assertions.assertFalse(iterator.hasNext());
      Assertions.assertFalse(cursor.isOpen());
      Assertions.assertTrue(cursor.isConsumed());
    }
  }

  @Test
  void shouldRowBoundsCountNullItem() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Cursor<User> cursor = mapper.getNullUsers(new RowBounds(1, 2));
      Iterator<User> iterator = cursor.iterator();

      Assertions.assertFalse(cursor.isOpen());

      // Check if hasNext, fetching is started
      Assertions.assertTrue(iterator.hasNext());
      // Re-invoking hasNext() should not fetch the next row
      Assertions.assertTrue(iterator.hasNext());
      Assertions.assertTrue(cursor.isOpen());
      Assertions.assertFalse(cursor.isConsumed());

      User user;
      user = iterator.next();
      Assertions.assertEquals("Kate", user.getName());
      Assertions.assertEquals(1, cursor.getCurrentIndex());

      Assertions.assertTrue(iterator.hasNext());
      user = iterator.next();
      Assertions.assertNull(user);
      Assertions.assertEquals(2, cursor.getCurrentIndex());

      // Check no more elements
      Assertions.assertFalse(iterator.hasNext());
      Assertions.assertFalse(cursor.isOpen());
      Assertions.assertTrue(cursor.isConsumed());
    }
  }
}
