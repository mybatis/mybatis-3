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
package org.apache.ibatis.submitted.arg_name_based_constructor_automapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArgNameBasedConstructorAutoMappingTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/arg_name_based_constructor_automapping/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    sqlSessionFactory.getConfiguration().setArgNameBasedConstructorAutoMapping(true);
    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/arg_name_based_constructor_automapping/CreateDB.sql");
  }

  @Test
  void shouldFindResultsInDifferentOrder() {
    // This test requires -parameters compiler option
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectNameAndId(1);
      assertEquals(Integer.valueOf(1), user.getId());
      assertEquals("User1!", user.getName());
    }
  }

  @Test
  void shouldRespectUseColumnLabelSetting() {
    // This test requires -parameters compiler option
    sqlSessionFactory.getConfiguration().setUseColumnLabel(false);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectNameAndIdWithBogusLabel(1);
      assertEquals(Integer.valueOf(1), user.getId());
      assertEquals("User1!", user.getName());
    } finally {
      sqlSessionFactory.getConfiguration().setUseColumnLabel(true);
    }
  }

  @Test
  void shouldErrorMessageBeHelpful() {
    // This test requires -parameters compiler option
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.selectNameAndIdWithBogusLabel(1);
      fail("Exception should be thrown");
    } catch (PersistenceException e) {
      ExecutorException ex = (ExecutorException) e.getCause();
      assertEquals(
          "Constructor auto-mapping of 'public org.apache.ibatis.submitted.arg_name_based_constructor_automapping."
              + "User(java.lang.Integer,java.lang.String)' failed "
              + "because '[id]' were not found in the result set; "
              + "Available columns are '[NAME, BAR]' and mapUnderscoreToCamelCase is 'true'.",
          ex.getMessage());
    }
  }

  @Test
  void shouldWorkWithExtraColumns() {
    // This test requires -parameters compiler option
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.selectNameTeamAndId(1);
      assertEquals(Integer.valueOf(1), user.getId());
      assertEquals("User1!", user.getName());
      assertEquals(99, user.getTeam());
    }
  }

  @Test
  void shouldRespectParamAnnotation() {
    // This test requires -parameters compiler option
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User2 user = mapper.selectUserIdAndUserName(1);
      assertEquals(Integer.valueOf(1), user.getUserId());
      assertEquals("User1", user.getName());
    }
  }

  @Test
  void shouldRespectMapUnderscoreToCamelCaseSetting() {
    // This test requires -parameters compiler option
    sqlSessionFactory.getConfiguration().setMapUnderscoreToCamelCase(true);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User2 user = mapper.selectUserIdAndUserNameUnderscore(1);
      assertEquals(Integer.valueOf(1), user.getUserId());
      assertEquals("User1", user.getName());
    }
  }

  @Test
  void shouldApplyColumnPrefix() {
    sqlSessionFactory.getConfiguration().setMapUnderscoreToCamelCase(true);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Task task = mapper.selectTask(11);
      assertEquals(Integer.valueOf(1), task.getAssignee().getId());
      assertEquals("User1!", task.getAssignee().getName());
      assertEquals(99, task.getAssignee().getTeam());
    }
  }
}
