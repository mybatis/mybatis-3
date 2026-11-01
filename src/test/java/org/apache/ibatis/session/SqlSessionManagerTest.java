/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.PostLite;
import org.apache.ibatis.domain.blog.mappers.AuthorMapper;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SqlSessionManagerTest extends BaseDataTest {

  private static SqlSessionManager manager;

  @BeforeAll
  static void setup() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    manager = SqlSessionManager.newInstance(reader);
  }

  @Test
  void shouldThrowExceptionIfMappedStatementDoesNotExistAndSqlSessionIsOpen() {
    try {
      manager.startManagedSession();
      manager.selectList("ThisStatementDoesNotExist");
      fail("Expected exception to be thrown due to statement that does not exist.");
    } catch (PersistenceException e) {
      assertTrue(e.getMessage().contains("does not contain value for ThisStatementDoesNotExist"));
    } finally {
      manager.close();
    }
  }

  @Test
  void shouldCommitInsertedAuthor() {
    try {
      manager.startManagedSession();
      AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      mapper.insertAuthor(expected);
      manager.commit();
      Author actual = mapper.selectAuthor(500);
      assertNotNull(actual);
    } finally {
      manager.close();
    }
  }

  @Test
  void shouldRollbackInsertedAuthor() {
    try {
      manager.startManagedSession();
      AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
      Author expected = new Author(501, "lmeadors", "******", "lmeadors@somewhere.com", "Something...", null);
      mapper.insertAuthor(expected);
      manager.rollback();
      Author actual = mapper.selectAuthor(501);
      assertNull(actual);
    } finally {
      manager.close();
    }
  }

  @Test
  void shouldImplicitlyRollbackInsertedAuthor() {
    manager.startManagedSession();
    AuthorMapper mapper = manager.getMapper(AuthorMapper.class);
    Author expected = new Author(502, "emacarron", "******", "emacarron@somewhere.com", "Something...", null);
    mapper.insertAuthor(expected);
    manager.close();
    Author actual = mapper.selectAuthor(502);
    assertNull(actual);
  }

  @Test
  void shouldFindAllPostLites() throws Exception {
    List<PostLite> posts = manager.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.selectPostLite");
    assertEquals(2, posts.size()); // old gcode issue #392, new #1848
  }

  @Test
  void shouldFindAllMutablePostLites() throws Exception {
    List<PostLite> posts = manager.selectList("org.apache.ibatis.domain.blog.mappers.PostMapper.selectMutablePostLite");
    assertEquals(2, posts.size()); // old gcode issue #392, new #1848
  }

  @Test
  void shouldCloseSessionWhenManagedSessionIsStarted() {
    manager.startManagedSession();
    assertTrue(manager.isManagedSessionStarted());

    manager.close();

    assertFalse(manager.isManagedSessionStarted());
  }

  @Test
  void shouldThrowExceptionWhenClosingWithoutManagedSession() {
    SqlSessionException exception = assertThrows(SqlSessionException.class, () -> manager.close());

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Cannot close"));
    assertTrue(exception.getMessage().contains("No managed session is started"));
  }

  @Test
  void shouldClearThreadLocalEvenWhenSessionCloseThrowsException() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";

    final Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactory baseFactory = new SqlSessionFactoryBuilder().build(reader);
    Configuration configuration = baseFactory.getConfiguration();
    SqlSession baseSession = baseFactory.openSession();

    SqlSession failingSession = new SqlSession() {
      @Override
      public <T> T selectOne(String statement) {
        return baseSession.selectOne(statement);
      }

      @Override
      public <T> T selectOne(String statement, Object parameter) {
        return baseSession.selectOne(statement, parameter);
      }

      @Override
      public <K, V> java.util.Map<K, V> selectMap(String statement, String mapKey) {
        return baseSession.selectMap(statement, mapKey);
      }

      @Override
      public <K, V> java.util.Map<K, V> selectMap(String statement, Object parameter, String mapKey) {
        return baseSession.selectMap(statement, parameter, mapKey);
      }

      @Override
      public <K, V> java.util.Map<K, V> selectMap(String statement, Object parameter, String mapKey,
          RowBounds rowBounds) {
        return baseSession.selectMap(statement, parameter, mapKey, rowBounds);
      }

      @Override
      public <T> org.apache.ibatis.cursor.Cursor<T> selectCursor(String statement) {
        return baseSession.selectCursor(statement);
      }

      @Override
      public <T> org.apache.ibatis.cursor.Cursor<T> selectCursor(String statement, Object parameter) {
        return baseSession.selectCursor(statement, parameter);
      }

      @Override
      public <T> org.apache.ibatis.cursor.Cursor<T> selectCursor(String statement, Object parameter,
          RowBounds rowBounds) {
        return baseSession.selectCursor(statement, parameter, rowBounds);
      }

      @Override
      public <E> java.util.List<E> selectList(String statement) {
        return baseSession.selectList(statement);
      }

      @Override
      public <E> java.util.List<E> selectList(String statement, Object parameter) {
        return baseSession.selectList(statement, parameter);
      }

      @Override
      public <E> java.util.List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        return baseSession.selectList(statement, parameter, rowBounds);
      }

      @Override
      public void select(String statement, ResultHandler handler) {
        baseSession.select(statement, handler);
      }

      @Override
      public void select(String statement, Object parameter, ResultHandler handler) {
        baseSession.select(statement, parameter, handler);
      }

      @Override
      public void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler) {
        baseSession.select(statement, parameter, rowBounds, handler);
      }

      @Override
      public int insert(String statement) {
        return baseSession.insert(statement);
      }

      @Override
      public int insert(String statement, Object parameter) {
        return baseSession.insert(statement, parameter);
      }

      @Override
      public int update(String statement) {
        return baseSession.update(statement);
      }

      @Override
      public int update(String statement, Object parameter) {
        return baseSession.update(statement, parameter);
      }

      @Override
      public int delete(String statement) {
        return baseSession.delete(statement);
      }

      @Override
      public int delete(String statement, Object parameter) {
        return baseSession.delete(statement, parameter);
      }

      @Override
      public void commit() {
        baseSession.commit();
      }

      @Override
      public void commit(boolean force) {
        baseSession.commit(force);
      }

      @Override
      public void rollback() {
        baseSession.rollback();
      }

      @Override
      public void rollback(boolean force) {
        baseSession.rollback(force);
      }

      @Override
      public java.util.List<org.apache.ibatis.executor.BatchResult> flushStatements() {
        return baseSession.flushStatements();
      }

      @Override
      public void close() {
        baseSession.close();
        throw new RuntimeException("Simulated close failure");
      }

      @Override
      public void clearCache() {
        baseSession.clearCache();
      }

      @Override
      public Configuration getConfiguration() {
        return baseSession.getConfiguration();
      }

      @Override
      public <T> T getMapper(Class<T> type) {
        return baseSession.getMapper(type);
      }

      @Override
      public Connection getConnection() {
        return baseSession.getConnection();
      }
    };

    SqlSessionFactory factoryWithFailingSession = new SqlSessionFactory() {
      @Override
      public SqlSession openSession() {
        return failingSession;
      }

      @Override
      public SqlSession openSession(boolean autoCommit) {
        return failingSession;
      }

      @Override
      public SqlSession openSession(TransactionIsolationLevel level) {
        return failingSession;
      }

      @Override
      public SqlSession openSession(ExecutorType execType) {
        return failingSession;
      }

      @Override
      public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
        return failingSession;
      }

      @Override
      public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
        return failingSession;
      }

      @Override
      public SqlSession openSession(ExecutorType execType, Connection connection) {
        return failingSession;
      }

      @Override
      public SqlSession openSession(Connection connection) {
        return failingSession;
      }

      @Override
      public Configuration getConfiguration() {
        return configuration;
      }
    };

    SqlSessionManager newManager = SqlSessionManager.newInstance(factoryWithFailingSession);
    newManager.startManagedSession();
    assertTrue(newManager.isManagedSessionStarted());

    RuntimeException exception = assertThrows(RuntimeException.class, () -> newManager.close());

    assertNotNull(exception);
    assertTrue(exception.getMessage().contains("Simulated close failure"));
    assertFalse(newManager.isManagedSessionStarted());
  }

}
