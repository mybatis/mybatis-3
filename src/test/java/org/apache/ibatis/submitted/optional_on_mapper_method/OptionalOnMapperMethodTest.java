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
package org.apache.ibatis.submitted.optional_on_mapper_method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.Reader;
import java.util.Optional;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for support the {@code java.util.Optional} as return type of mapper method.
 *
 * @since 3.5.0
 * @author Kazuki Shimizu
 */
class OptionalOnMapperMethodTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/optional_on_mapper_method/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/optional_on_mapper_method/CreateDB.sql");
  }

  @Test
  void returnNotNullOnAnnotation() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingAnnotation(1);
      assertTrue(user.isPresent());
      assertEquals("User1", user.get().getName());
    }
  }

  @Test
  void returnNullOnAnnotation() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingAnnotation(3);
      assertFalse(user.isPresent());
    }
  }

  @Test
  void returnNotNullOnXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingXml(2);
      assertTrue(user.isPresent());
      assertEquals("User2", user.get().getName());
    }
  }

  @Test
  void returnNullOnXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingXml(3);
      assertFalse(user.isPresent());
    }
  }

  @Test
  void returnOptionalFromSqlSession() {
    try (SqlSession sqlSession = Mockito.spy(sqlSessionFactory.openSession())) {
      User mockUser = new User();
      mockUser.setName("mock user");
      Optional<User> optionalMockUser = Optional.of(mockUser);
      doReturn(optionalMockUser).when(sqlSession).selectOne(any(String.class), any(Object.class));

      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Optional<User> user = mapper.getUserUsingAnnotation(3);
      assertSame(optionalMockUser, user);
    }
  }

}
