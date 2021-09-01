/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.mapper_extend;

import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MapperExtendTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/mapper_extend/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/mapper_extend/CreateDB.sql");
  }

  @Test
  void shouldGetAUserWithAnExtendedXMLMethod() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ParentMapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserXML();
      Assertions.assertEquals("User1", user.getName());
    }
  }

  @Test
  void shouldGetAUserWithAnExtendedAnnotatedMethod() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ParentMapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUserAnnotated();
      Assertions.assertEquals("User1", user.getName());
    }
  }

  @Test
  void shouldGetAUserWithAnOverloadedXMLMethod() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ParentMapper mapper = sqlSession.getMapper(MapperOverload.class);
      User user = mapper.getUserXML();
      Assertions.assertEquals("User2", user.getName());
    }
  }

  @Test
  void shouldGetAUserWithAnOverloadedAnnotatedMethod() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ParentMapper mapper = sqlSession.getMapper(MapperOverload.class);
      User user = mapper.getUserAnnotated();
      Assertions.assertEquals("User2", user.getName());
    }
  }

  @Test
  void shouldFindStatementInSubInterfaceOfDeclaringClass() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ChildMapper mapper = sqlSession.getMapper(ChildMapper.class);
      User user = mapper.getUserByName("User1");
      Assertions.assertNotNull(user);
    }
  }

  @Test
  void shouldThrowExceptionIfNoMatchingStatementFound() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      when(mapper::noMappedStatement);
      then(caughtException()).isInstanceOf(BindingException.class)
        .hasMessage("Invalid bound statement (not found): "
          + Mapper.class.getName() + ".noMappedStatement");
    }
  }
}
