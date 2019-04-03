/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.overload;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverloadStatementTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/overload/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/overload/CreateDB.sql");
  }

  @Test
  void shouldReferenceXmlStatementByAltId() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.select(1);
      assertEquals("User1", user.getName());
    }
  }

  @Test
  void shouldReferenceNestedSelectByAltId() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.select("User1");
      assertEquals(Integer.valueOf(1), user.getId());
      assertNotNull(user.getFriend());
    }
  }

  @Test
  void testImplicitNaming() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(2);
      user.setName("User2");
      mapper.insert(user);
      mapper.insert(new int[] { 3, 4 }, "User");
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user2 = mapper.select(2);
      assertEquals("User2", user2.getName());
      User user3 = mapper.select(3);
      assertEquals("User3", user3.getName());
      User user4 = mapper.select(4);
      assertEquals("User4", user4.getName());
    }
  }

  @Test
  void shouldFailIfIdIsEmpty() {
    try {
      Configuration config = new Configuration();
      config.addMapper(InvalidMapper1.class);
      fail("Should throw BuilderException");
    } catch (BuilderException e) {
      assertEquals(
          String.format("Statement ID cannot be an empty string. Check @StatementId on %s#%s with parameter(s) %s",
              InvalidMapper1.class.getName(), "select", "[java.lang.Integer id]"),
          e.getMessage());
    }
  }

  @Test
  void shouldFailIfIdContainsDot() {
    try {
      Configuration config = new Configuration();
      config.addMapper(InvalidMapper2.class);
      fail("Should throw BuilderException");
    } catch (BuilderException e) {
      assertEquals(
          String.format("Dots are not allowed in statement ID. Check @StatementId on %s#%s with parameter(s) %s",
              InvalidMapper2.class.getName(), "select", "[]"),
          e.getMessage());
    }
  }
}
