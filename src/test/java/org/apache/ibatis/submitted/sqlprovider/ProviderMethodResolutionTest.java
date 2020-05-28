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
package org.apache.ibatis.submitted.sqlprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test for https://github.com/mybatis/mybatis-3/issues/1279
 */
class ProviderMethodResolutionTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/sqlprovider/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().addMapper(ProvideMethodResolverMapper.class);
    }
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/sqlprovider/CreateDB.sql");
  }

  @Test
  void shouldResolveWhenDefaultResolverMatchedMethodIsOne() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProvideMethodResolverMapper mapper = sqlSession.getMapper(ProvideMethodResolverMapper.class);
      assertEquals(1, mapper.select());
    }
  }

  @Test
  void shouldErrorWhenDefaultResolverMethodNameMatchedMethodIsNone() {
    BuilderException e = Assertions.assertThrows(BuilderException.class, () -> sqlSessionFactory.getConfiguration()
        .addMapper(DefaultProvideMethodResolverMethodNameMatchedMethodIsNoneMapper.class));
    assertEquals(
        "Cannot resolve the provider method because 'insert' not found in SqlProvider 'org.apache.ibatis.submitted.sqlprovider.ProviderMethodResolutionTest$DefaultProvideMethodResolverMethodNameMatchedMethodIsNoneMapper$MethodResolverBasedSqlProvider'.",
        e.getMessage());
  }

  @Test
  void shouldErrorWhenDefaultResolverReturnTypeMatchedMethodIsNone() {
    BuilderException e = Assertions.assertThrows(BuilderException.class, () -> sqlSessionFactory.getConfiguration()
        .addMapper(DefaultProvideMethodResolverReturnTypeMatchedMethodIsNoneMapper.class));
    assertEquals(
        "Cannot resolve the provider method because 'insert' does not return the CharSequence or its subclass in SqlProvider 'org.apache.ibatis.submitted.sqlprovider.ProviderMethodResolutionTest$DefaultProvideMethodResolverReturnTypeMatchedMethodIsNoneMapper$MethodResolverBasedSqlProvider'.",
        e.getMessage());
  }

  @Test
  void shouldErrorWhenDefaultResolverMatchedMethodIsMultiple() {
    BuilderException e = Assertions.assertThrows(BuilderException.class, () -> sqlSessionFactory.getConfiguration()
        .addMapper(DefaultProvideMethodResolverMatchedMethodIsMultipleMapper.class));
    assertEquals(
        "Cannot resolve the provider method because 'update' is found multiple in SqlProvider 'org.apache.ibatis.submitted.sqlprovider.ProviderMethodResolutionTest$DefaultProvideMethodResolverMatchedMethodIsMultipleMapper$MethodResolverBasedSqlProvider'.",
        e.getMessage());
  }

  @Test
  void shouldResolveReservedMethod() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProvideMethodResolverMapper mapper = sqlSession.getMapper(ProvideMethodResolverMapper.class);
      assertEquals(1, mapper.delete());
    }
  }

  @Test
  void shouldUseSpecifiedMethodOnSqlProviderAnnotation() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProvideMethodResolverMapper mapper = sqlSession.getMapper(ProvideMethodResolverMapper.class);
      assertEquals(2, mapper.select2());
    }
  }

  @Test
  void shouldResolveMethodUsingCustomResolver() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProvideMethodResolverMapper mapper = sqlSession.getMapper(ProvideMethodResolverMapper.class);
      assertEquals(3, mapper.select3());
    }
  }

  @Test
  void shouldResolveReservedNameMethodWhenCustomResolverReturnNull() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ProvideMethodResolverMapper mapper = sqlSession.getMapper(ProvideMethodResolverMapper.class);
      assertEquals(99, mapper.select4());
    }
  }

  @Test
  void shouldErrorWhenCannotDetectsReservedNameMethod() {
    BuilderException e = Assertions.assertThrows(BuilderException.class,
        () -> sqlSessionFactory.getConfiguration().addMapper(ReservedNameMethodIsNoneMapper.class));
    assertEquals(
        "Error creating SqlSource for SqlProvider. Method 'provideSql' not found in SqlProvider 'org.apache.ibatis.submitted.sqlprovider.ProviderMethodResolutionTest$ReservedNameMethodIsNoneMapper$SqlProvider'.",
        e.getMessage());
  }

  interface ProvideMethodResolverMapper {

    @SelectProvider(MethodResolverBasedSqlProvider.class)
    int select();

    @SelectProvider(type = MethodResolverBasedSqlProvider.class, method = "provideSelect2Sql")
    int select2();

    @SelectProvider(type = CustomMethodResolverBasedSqlProvider.class)
    int select3();

    @SelectProvider(type = CustomMethodResolverBasedSqlProvider.class)
    int select4();

    @DeleteProvider(ReservedMethodNameBasedSqlProvider.class)
    int delete();

    class MethodResolverBasedSqlProvider implements ProviderMethodResolver {
      public static String select() {
        return "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }

      public static String select2() {
        throw new IllegalStateException(
            "This method should not called when specify `method` attribute on @SelectProvider.");
      }

      public static String provideSelect2Sql() {
        return "SELECT 2 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }
    }

    class ReservedMethodNameBasedSqlProvider {
      public static String provideSql() {
        return "DELETE FROM memos WHERE id = 1";
      }
    }

    class CustomMethodResolverBasedSqlProvider implements CustomProviderMethodResolver {
      public static String select3Sql() {
        return "SELECT 3 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }

      public static String provideSql() {
        return "SELECT 99 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }
    }

  }

  interface CustomProviderMethodResolver extends ProviderMethodResolver {
    @Override
    default Method resolveMethod(ProviderContext context) {
      List<Method> targetMethods = Arrays.stream(getClass().getMethods())
          .filter(m -> m.getName().equals(context.getMapperMethod().getName() + "Sql"))
          .filter(m -> CharSequence.class.isAssignableFrom(m.getReturnType())).collect(Collectors.toList());
      if (targetMethods.size() == 1) {
        return targetMethods.get(0);
      }
      return null;
    }
  }

  interface DefaultProvideMethodResolverMethodNameMatchedMethodIsNoneMapper {

    @InsertProvider(type = MethodResolverBasedSqlProvider.class)
    int insert();

    class MethodResolverBasedSqlProvider implements ProviderMethodResolver {
      public static String provideInsertSql() {
        return "INSERT INTO foo (name) VALUES(#{name})";
      }
    }

  }

  interface DefaultProvideMethodResolverReturnTypeMatchedMethodIsNoneMapper {

    @InsertProvider(MethodResolverBasedSqlProvider.class)
    int insert();

    class MethodResolverBasedSqlProvider implements ProviderMethodResolver {
      public static int insert() {
        return 1;
      }
    }

  }

  interface DefaultProvideMethodResolverMatchedMethodIsMultipleMapper {

    @UpdateProvider(MethodResolverBasedSqlProvider.class)
    int update();

    class MethodResolverBasedSqlProvider implements ProviderMethodResolver {
      public static String update() {
        return "UPDATE foo SET name = #{name} WHERE id = #{id}";
      }

      public static StringBuilder update(ProviderContext context) {
        return new StringBuilder("UPDATE foo SET name = #{name} WHERE id = #{id}");
      }
    }

  }

  interface ReservedNameMethodIsNoneMapper {

    @UpdateProvider(type = SqlProvider.class)
    int update();

    class SqlProvider {
      public static String select() {
        return "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }
    }

  }

}
