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
package org.apache.ibatis.submitted.cursor_cache_oom;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.cursor.defaults.DefaultCursor;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ALL")
class CursorOomTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/cursor_cache_oom/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/cursor_cache_oom/CreateDB.sql");
  }

  private static Map<CacheKey, Object> getNestedResultObjects(Cursor<User> users)
      throws IllegalAccessException, NoSuchFieldException {
    DefaultCursor<User> defaultCursor = (DefaultCursor<User>) users;
    Field resultSetHandlerField = DefaultCursor.class.getDeclaredField("resultSetHandler");
    resultSetHandlerField.setAccessible(true);
    DefaultResultSetHandler defaultResultSetHandler = (DefaultResultSetHandler) resultSetHandlerField
        .get(defaultCursor);
    Field nestedResultObjectsField = DefaultResultSetHandler.class.getDeclaredField("nestedResultObjects");
    nestedResultObjectsField.setAccessible(true);
    return (Map<CacheKey, Object>) nestedResultObjectsField.get(defaultResultSetHandler);
  }

  private static List<Cursor<?>> getCursors(SqlSession sqlSession) throws NoSuchFieldException, IllegalAccessException {
    DefaultSqlSession session = (DefaultSqlSession) sqlSession;
    Field cursorListField = DefaultSqlSession.class.getDeclaredField("cursorList");
    cursorListField.setAccessible(true);
    return (List<Cursor<?>>) cursorListField.get(session);
  }

  @Test
  void shouldNotCacheAllDataForWholeSessionWhileUsingCursor()
      throws IOException, NoSuchFieldException, IllegalAccessException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      try (Cursor<User> users = mapper.fetchUsers()) {
        for (User user : users) {
          consumeUser(user);
        }
        Map nestedResultObjects = getNestedResultObjects(users);

        Assertions.assertFalse(nestedResultObjects.isEmpty());

        // does not pass now
        // will be great, if cursor will use constant memory instead of linear one
        // Assertions.assertTrue(nestedResultObjects.size() <= 2);
      }

      List<Cursor<?>> cursorList = getCursors(sqlSession);

      // expect that either reference to the cursor itselfis gone or cursor does not contains all the fetched data
      // the most preferrable way will be not to cache data, when the row is already processed (see commented
      // line above)
      Assertions
          .assertTrue(cursorList.isEmpty() || getNestedResultObjects((Cursor<User>) cursorList.get(0)).size() <= 2);
    }
  }

  private void consumeUser(User user) {
    // do nothing
  }
}
