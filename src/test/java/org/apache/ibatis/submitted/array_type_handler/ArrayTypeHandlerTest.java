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
package org.apache.ibatis.submitted.array_type_handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArrayTypeHandlerTest {

  private SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/array_type_handler/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/array_type_handler/CreateDB.sql");
  }

  @Test
  void shouldInsertArrayValue() throws Exception {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      User user = new User();
      user.setId(1);
      user.setName("User 1");
      user.setNicknames(new String[] { "User", "one" });

      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.insert(user);
      sqlSession.commit();

      int usersInDatabase = mapper.getUserCount();
      assertEquals(1, usersInDatabase);

      Integer nicknameCount = mapper.getNicknameCount();
      assertEquals(2, nicknameCount);
    }
  }

  @Test
  void shouldInsertNullValue() throws Exception {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      User user = new User();
      user.setId(1);
      user.setName("User 1");
      // note how the user does not have nicknames

      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.insert(user);
      sqlSession.commit();

      int usersInDatabase = mapper.getUserCount();
      assertEquals(1, usersInDatabase);

      Integer nicknameCount = mapper.getNicknameCount();
      assertNull(nicknameCount);
    }
  }
}
