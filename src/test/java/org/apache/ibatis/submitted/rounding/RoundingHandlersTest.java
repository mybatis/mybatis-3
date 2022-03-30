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
package org.apache.ibatis.submitted.rounding;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RoundingHandlersTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/rounding/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/rounding/CreateDB.sql");
  }

  @Test
  void shouldGetAUser() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals(RoundingMode.UP, user.getRoundingMode());
      user = mapper.getUser2(1);
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals(RoundingMode.UP, user.getRoundingMode());
    }
  }

  @Test
  void shouldInsertUser2() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      User user = new User();
      user.setId(2);
      user.setName("User2");
      user.setFunkyNumber(BigDecimal.ZERO);
      user.setRoundingMode(RoundingMode.UNNECESSARY);
      mapper.insert(user);
      mapper.insert2(user);
    }
  }

}
