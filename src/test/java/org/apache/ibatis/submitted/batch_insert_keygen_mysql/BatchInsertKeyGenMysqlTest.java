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
package org.apache.ibatis.submitted.batch_insert_keygen_mysql;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BatchInsertKeyGenMysqlTest {

  private SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/batch_insert_keygen_mysql/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/submitted/batch_insert_keygen_mysql/CreateDB.sql");
  }

  @Test
  void testMysqlConnectorGeneratedKeys1() throws Exception {
    try (Connection conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection()) {
      String sql = "insert into users values(?,?)";
      PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      conn.setAutoCommit(false);
      // 1,2,3,4,5
      for (int i = 1; i <= 5; i++) {
        stmt.setInt(1, i);
        stmt.setString(2, "name" + i);
        stmt.addBatch();
      }
      stmt.executeBatch();
      ResultSet rs = stmt.getGeneratedKeys();

      // 5,6,7,8,9
      System.out.print("ids generated: ");
      while (rs.next()) {
        Object value = rs.getObject(1);
        System.out.print(value + " ");
      }
      conn.commit();
    }
  }


  @Test
  void shouldIdNotChangedAfterBatchInsert1() {
    List<User> userList = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      User user = new User();
      user.setId(i);
      user.setName(i + "name");
      userList.add(user);
    }
    // ids before insert: 1,2,3,4,5
    System.out.println("before insert, id list: " + userList);
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      userList.forEach(user -> {
        mapper.insert(user);
      });
      sqlSession.commit();
    }

    // ids after insert: 5,6,7,8,9,not expected
    System.out.println("after insert, id list: " + userList);
    for (int i = 1; i <= 5; i++) {
      assertEquals(Integer.valueOf(i), userList.get(i - 1).getId());
    }

    // db data: correct as expected
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> dbUsers = mapper.listUsers();
      System.out.println("after insert, id list in db: " + dbUsers);
      assertEquals(dbUsers.size(), userList.size());
    }
  }


  @Test
  void shouldIdNotChangedAfterBatchInsert2() {
    List<User> userList = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      User user = new User();
      user.setId(i);
      if (i == 2 || i == 4) {
        user.setId(null);
      }
      user.setName(i + "name");
      userList.add(user);
    }
    // ids before insert: 1,null,3,null,5
    System.out.println("before insert, id list: " + userList);
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      userList.forEach(user -> {
        mapper.insert(user);
      });
      sqlSession.commit();
    }

    // db data: 1,2,3,4,5
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> dbUsers = mapper.listUsers();
      System.out.println("after insert, id list in db: " + dbUsers);
      assertEquals(dbUsers.size(), userList.size());
    }

    // ids after insert: 2,3,4,5,6
    System.out.println("after insert, id list: " + userList);
    for (int i = 1; i <= 5; i++) {
      assertEquals(Integer.valueOf(i), userList.get(i - 1).getId());
    }
  }

}
