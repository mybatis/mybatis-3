/*
 *    Copyright 2009-2026 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.SyntheticState;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodCall;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
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
    PersistenceException e = assertThrows(PersistenceException.class, () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        Mapper mapper = sqlSession.getMapper(Mapper.class);
        mapper.selectNameAndIdWithBogusLabel(1);
      }
    });
    ExecutorException ex = (ExecutorException) e.getCause();
    assertEquals("""
        Constructor auto-mapping of 'public org.apache.ibatis.submitted.arg_name_based_constructor_automapping.\
        User(java.lang.Integer,java.lang.String)' failed \
        because '[id]' were not found in the result set; \
        Available columns are '[NAME, BAR]' and mapUnderscoreToCamelCase is 'true'.\
        """, ex.getMessage());
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

  @Test
  void shouldUseTheOnlyNonSyntheticConstructorWhenCompilerAddedASyntheticOne() throws Exception {
    Class<? extends User> userType = defineUserWithDeclaredAndSyntheticConstructors();
    assertTrue(Arrays.stream(userType.getDeclaredConstructors()).anyMatch(Constructor::isSynthetic));

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      User user = sqlSession.selectOne(addSelectUserStatement(userType));
      assertEquals(Integer.valueOf(1), user.getId());
      assertEquals("User1!", user.getName());
    }
  }

  @Test
  void shouldReportNoConstructorFoundWhenEveryConstructorIsSynthetic() throws Exception {
    Class<? extends User> userType = defineUserWithOnlySyntheticConstructor();
    assertTrue(Arrays.stream(userType.getDeclaredConstructors()).anyMatch(Constructor::isSynthetic));

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      String statementId = addSelectUserStatement(userType);
      PersistenceException e = assertThrows(PersistenceException.class, () -> sqlSession.selectOne(statementId));
      ExecutorException ex = (ExecutorException) e.getCause();
      assertEquals("No constructor found in " + userType.getName() + " matching [java.lang.String, java.lang.Integer]",
          ex.getMessage());
    }
  }

  private static String addSelectUserStatement(Class<? extends User> userType) {
    Configuration configuration = sqlSessionFactory.getConfiguration();
    String statementId = "select" + userType.getSimpleName();
    ResultMap resultMap = new ResultMap.Builder(configuration, statementId + "Result", userType, new ArrayList<>())
        .build();
    configuration.addMappedStatement(new MappedStatement.Builder(configuration, statementId,
        new StaticSqlSource(configuration, "select name, id from users where id = 1"), SqlCommandType.SELECT)
            .resultMaps(List.of(resultMap)).build());
    return statementId;
  }

  private static Class<? extends User> defineUserWithDeclaredAndSyntheticConstructors() throws Exception {
    return addSyntheticConstructor(
        subclassUser().defineConstructor(Visibility.PUBLIC).withParameter(Integer.class, "id")
            .withParameter(String.class, "name").intercept(MethodCall.invoke(userConstructor()).withAllArguments()));
  }

  private static Class<? extends User> defineUserWithOnlySyntheticConstructor() throws Exception {
    return addSyntheticConstructor(subclassUser());
  }

  private static DynamicType.Builder<User> subclassUser() {
    return new ByteBuddy().subclass(User.class, ConstructorStrategy.Default.NO_CONSTRUCTORS);
  }

  // Mimics the shape of Kotlin's default-argument constructor: source parameters, an int mask, and a marker.
  private static Class<? extends User> addSyntheticConstructor(DynamicType.Builder<User> builder) throws Exception {
    return builder.defineConstructor(Visibility.PUBLIC, SyntheticState.SYNTHETIC)
        .withParameters(Integer.class, String.class, int.class, Object.class)
        .intercept(MethodCall.invoke(userConstructor()).withArgument(0, 1)).make().load(User.class.getClassLoader())
        .getLoaded();
  }

  private static Constructor<User> userConstructor() throws NoSuchMethodException {
    return User.class.getConstructor(Integer.class, String.class);
  }
}
