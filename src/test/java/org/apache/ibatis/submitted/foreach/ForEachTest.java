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
package org.apache.ibatis.submitted.foreach;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ForEachTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/foreach/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/foreach/CreateDB.sql");
  }

  @Test
  void shouldGetAUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User testProfile = new User();
      testProfile.setId(2);
      User friendProfile = new User();
      friendProfile.setId(6);
      List<User> friendList = new ArrayList<>();
      friendList.add(friendProfile);
      testProfile.setFriendList(friendList);
      User user = mapper.getUser(testProfile);
      Assertions.assertEquals("User6", user.getName());
    }
  }

  @Test
  void shouldHandleComplexNullItem() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User();
      user1.setId(2);
      user1.setName("User2");
      List<User> users = new ArrayList<>();
      users.add(user1);
      users.add(null);
      int count = mapper.countByUserList(users);
      Assertions.assertEquals(1, count);
    }
  }

  @Test
  void shouldHandleMoreComplexNullItem() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User();
      User bestFriend = new User();
      bestFriend.setId(5);
      user1.setBestFriend(bestFriend);
      List<User> users = new ArrayList<>();
      users.add(user1);
      users.add(null);
      int count = mapper.countByBestFriend(users);
      Assertions.assertEquals(1, count);
    }
  }

  @Test
  void nullItemInContext() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User();
      user1.setId(3);
      List<User> users = new ArrayList<>();
      users.add(user1);
      users.add(null);
      String name = mapper.selectWithNullItemCheck(users);
      Assertions.assertEquals("User3", name);
    }
  }

  @Test
  void shouldReportMissingPropertyName() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      when(() -> mapper.typoInItemProperty(Collections.singletonList(new User())));
      then(caughtException()).isInstanceOf(PersistenceException.class)
        .hasMessageContaining("There is no getter for property named 'idd' in 'class org.apache.ibatis.submitted.foreach.User'");
    }
  }

  @Test
  void shouldRemoveItemVariableInTheContext() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      int result = mapper.itemVariableConflict(5, Arrays.asList(1, 2), Arrays.asList(3, 4));
      Assertions.assertEquals(5, result);
    }
  }

  @Test
  void shouldRemoveIndexVariableInTheContext() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      int result = mapper.indexVariableConflict(4, Arrays.asList(6, 7), Arrays.asList(8, 9));
      Assertions.assertEquals(4, result);
    }
  }

}
