/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.submitted.bind_in_foreach;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BindInForeachTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/bind_in_foreach/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/bind_in_foreach/CreateDB.sql");
  }

  @Test
  void testBindInForeach() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals(3, mapper.createUsers(List.of(2, 4, 6)));
      List<User> users = mapper.selectUsers();
      assertEquals(3, users.size());
      assertEquals(1, users.get(0).getId());
      assertEquals("User2", users.get(0).getName());
      assertEquals(2, users.get(1).getId());
      assertEquals("User4", users.get(1).getName());
      assertEquals(3, users.get(2).getId());
      assertEquals("User6", users.get(2).getName());
    }
  }

}
