/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.submitted.dirty_select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Iterator;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.testcontainers.PgContainer;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("TestcontainersTests")
class DirtySelectTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        PgContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/dirty_select/CreateDB.sql");
  }

  @Test
  void shouldRollbackIfCalled() {
    Integer id;
    try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.insertReturn("Jimmy");
      id = user.getId();
      assertNotNull(id);
      assertEquals("Jimmy", user.getName());
      sqlSession.rollback();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectById(id);
      assertNull(user);
    }
  }

  @Test
  void shouldRollbackIfCalled_Xml() {
    Integer id;
    try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.insertReturnXml("Jimmy");
      id = user.getId();
      assertNotNull(id);
      assertEquals("Jimmy", user.getName());
      sqlSession.rollback();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectById(id);
      assertNull(user);
    }
  }

  @Test
  void shouldRollbackIfCalled_Provider() {
    Integer id;
    try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.insertReturnProvider("Jimmy");
      id = user.getId();
      assertNotNull(id);
      assertEquals("Jimmy", user.getName());
      sqlSession.rollback();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectById(id);
      assertNull(user);
    }
  }

  @Test
  void shouldRollbackIfCalled_Cursor() throws Exception {
    Integer id;
    try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      try (Cursor<User> cursor = mapper.insertReturnCursor("Kate")) {
        Iterator<User> iterator = cursor.iterator();
        User user = iterator.next();
        id = user.getId();
        assertNotNull(id);
        assertEquals("Kate", user.getName());
      }
      sqlSession.rollback();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectById(id);
      assertNull(user);
    }
  }

  @Test
  void shouldNonDirtySelectNotUnsetDirtyFlag() {
    Integer id;
    try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      // INSERT
      User user = new User();
      user.setName("Bobby");
      mapper.insert(user);
      id = user.getId();
      assertNotNull(id);
      assertEquals("Bobby", user.getName());
      // Non-dirty SELECT
      mapper.selectById(id);
      sqlSession.rollback();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectById(id);
      assertNull(user);
    }
  }

  @Test
  void shouldNonDirtySelectNotUnsetDirtyFlag_Cursor() throws Exception {
    Integer id;
    try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      // INSERT
      User user = new User();
      user.setName("Bobby");
      mapper.insert(user);
      id = user.getId();
      assertNotNull(id);
      assertEquals("Bobby", user.getName());
      // Non-dirty SELECT
      try (Cursor<User> cursor = mapper.selectCursorById(id)) {
        Iterator<User> iterator = cursor.iterator();
        iterator.next();
      }
      sqlSession.rollback();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectById(id);
      assertNull(user);
    }
  }

}
