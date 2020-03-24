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
package org.apache.ibatis.submitted.batch_keys;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchKeysTest {

  private SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/batch_keys/Config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/batch_keys/CreateDB.sql");
  }

  public void testJdbc3Support() throws Exception {
    try (Connection conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
         PreparedStatement stmt = conn.prepareStatement("insert into users2 values(null, 'Pocoyo')", Statement.RETURN_GENERATED_KEYS)) {
      stmt.addBatch();
      stmt.executeBatch();
      try (ResultSet rs = stmt.getGeneratedKeys()) {
        if (rs.next()) {
          ResultSetMetaData rsmd = rs.getMetaData();
          int colCount = rsmd.getColumnCount();
          do {
            for (int i = 1; i <= colCount; i++) {
              String key = rs.getString(i);
              System.out.println("key " + i + " is " + key);
            }
          } while (rs.next());
        } else {
          System.out.println("There are no generated keys.");
        }
      }
    }
  }

  @Test
  void testInsert() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      User user1 = new User(null, "Pocoyo");
      sqlSession.insert("insert", user1);
      User user2 = new User(null, "Valentina");
      sqlSession.insert("insert", user2);
      sqlSession.flushStatements();
      assertEquals(Integer.valueOf(50), user1.getId());
      assertEquals(Integer.valueOf(50), user2.getId());
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<User> users = sqlSession.selectList("select");
      Assertions.assertEquals( 2, users.size());
    }
  }

  @Test
  void testInsertJdbc3() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      User user1 = new User(null, "Pocoyo");
      sqlSession.insert("insertIdentity", user1);
      User user2 = new User(null, "Valentina");
      sqlSession.insert("insertIdentity", user2);
      sqlSession.flushStatements();
      assertEquals(Integer.valueOf(0), user1.getId());
      assertEquals(Integer.valueOf(1), user2.getId());
      sqlSession.commit();
    }

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<User> users = sqlSession.selectList("selectIdentity");
      Assertions.assertEquals(2, users.size());
    }
  }

  @Test
  void testInsertWithMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      Mapper userMapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User(null, "Pocoyo");
      userMapper.insert(user1);
      User user2 = new User(null, "Valentina");
      userMapper.insert(user2);
      sqlSession.flushStatements();
      assertEquals(Integer.valueOf(50), user1.getId());
      assertEquals(Integer.valueOf(50), user2.getId());
      sqlSession.commit();
    }

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<User> users = sqlSession.selectList("select");
      Assertions.assertEquals(2, users.size());
    }
  }

  @Test
  void testInsertMapperJdbc3() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      Mapper userMapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User(null, "Pocoyo");
      userMapper.insertIdentity(user1);
      User user2 = new User(null, "Valentina");
      userMapper.insertIdentity(user2);
      sqlSession.flushStatements();
      assertEquals(Integer.valueOf(0), user1.getId());
      assertEquals(Integer.valueOf(1), user2.getId());
      sqlSession.commit();
    }

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<User> users = sqlSession.selectList("selectIdentity");
      Assertions.assertEquals(2, users.size());
    }
  }

  @Test
  void testInsertMapperNoBatchJdbc3() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper userMapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User(null, "Pocoyo");
      userMapper.insertIdentity(user1);
      assertEquals(Integer.valueOf(0), user1.getId());
      sqlSession.commit();
    }

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<User> users = sqlSession.selectList("selectIdentity");
      Assertions.assertEquals(1, users.size());
    }
  }

}
