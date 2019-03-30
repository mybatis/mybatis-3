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
package org.apache.ibatis.submitted.sqlprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Reader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SqlProviderTest {

  private static SqlSessionFactory sqlSessionFactory;
  private static SqlSessionFactory sqlSessionFactoryForDerby;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/sqlprovider/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().addMapper(StaticMethodSqlProviderMapper.class);
      sqlSessionFactory.getConfiguration().addMapper(DatabaseIdMapper.class);
    }
    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/sqlprovider/CreateDB.sql");

    // create a SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/sqlprovider/mybatis-config.xml")) {
      sqlSessionFactoryForDerby = new SqlSessionFactoryBuilder().build(reader, "development-derby");
      sqlSessionFactoryForDerby.getConfiguration().addMapper(DatabaseIdMapper.class);
    }
  }

  // Test for list
  @Test
  void shouldGetTwoUsers() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Integer> list = new ArrayList<>();
      list.add(1);
      list.add(3);
      List<User> users = mapper.getUsers(list);
      assertEquals(2, users.size());
      assertEquals("User1", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
    }
  }

  // Test for simple value without @Param
  @Test
  void shouldGetOneUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      {
        User user = mapper.getUser(4);
        assertNotNull(user);
        assertEquals("User4", user.getName());
      }
      {
        User user = mapper.getUser(null);
        assertNull(user);
      }
    }
  }

  // Test for empty
  @Test
  void shouldGetAllUsers() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getAllUsers();
      assertEquals(4, users.size());
      assertEquals("User1", users.get(0).getName());
      assertEquals("User2", users.get(1).getName());
      assertEquals("User3", users.get(2).getName());
      assertEquals("User4", users.get(3).getName());
    }
  }

  // Test for single JavaBean
  @Test
  void shouldGetUsersByCriteria() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      {
        User criteria = new User();
        criteria.setId(1);
        List<User> users = mapper.getUsersByCriteria(criteria);
        assertEquals(1, users.size());
        assertEquals("User1", users.get(0).getName());
      }
      {
        User criteria = new User();
        criteria.setName("User");
        List<User> users = mapper.getUsersByCriteria(criteria);
        assertEquals(4, users.size());
        assertEquals("User1", users.get(0).getName());
        assertEquals("User2", users.get(1).getName());
        assertEquals("User3", users.get(2).getName());
        assertEquals("User4", users.get(3).getName());
      }
    }
  }

  // Test for single map
  @Test
  void shouldGetUsersByCriteriaMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("id", 1);
        List<User> users = mapper.getUsersByCriteriaMap(criteria);
        assertEquals(1, users.size());
        assertEquals("User1", users.get(0).getName());
      }
      {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("name", "User");
        List<User> users = mapper.getUsersByCriteriaMap(criteria);
        assertEquals(4, users.size());
        assertEquals("User1", users.get(0).getName());
        assertEquals("User2", users.get(1).getName());
        assertEquals("User3", users.get(2).getName());
        assertEquals("User4", users.get(3).getName());
      }
    }
  }

  // Test for multiple parameter without @Param
  @Test
  void shouldGetUsersByName() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersByName("User", "id DESC");
      assertEquals(4, users.size());
      assertEquals("User4", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
      assertEquals("User2", users.get(2).getName());
      assertEquals("User1", users.get(3).getName());
    }
  }

  // Test for map without @Param
  @Test
  void shouldGetUsersByNameUsingMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersByNameUsingMap("User", "id DESC");
      assertEquals(4, users.size());
      assertEquals("User4", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
      assertEquals("User2", users.get(2).getName());
      assertEquals("User1", users.get(3).getName());
    }
  }

  // Test for multiple parameter with @Param
  @Test
  void shouldGetUsersByNameWithParamNameAndOrderBy() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersByNameWithParamNameAndOrderBy("User", "id DESC");
      assertEquals(4, users.size());
      assertEquals("User4", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
      assertEquals("User2", users.get(2).getName());
      assertEquals("User1", users.get(3).getName());
    }
  }

  // Test for map with @Param
  @Test
  void shouldGetUsersByNameWithParamNameUsingMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersByNameWithParamNameAndOrderBy("User", "id DESC");
      assertEquals(4, users.size());
      assertEquals("User4", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
      assertEquals("User2", users.get(2).getName());
      assertEquals("User1", users.get(3).getName());
    }
  }

  // Test for simple value with @Param
  @Test
  void shouldGetUsersByNameWithParamName() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      {
        List<User> users = mapper.getUsersByNameWithParamName("User");
        assertEquals(4, users.size());
        assertEquals("User4", users.get(0).getName());
        assertEquals("User3", users.get(1).getName());
        assertEquals("User2", users.get(2).getName());
        assertEquals("User1", users.get(3).getName());
      }
      {
        List<User> users = mapper.getUsersByNameWithParamName(null);
        assertEquals(4, users.size());
        assertEquals("User4", users.get(0).getName());
        assertEquals("User3", users.get(1).getName());
        assertEquals("User2", users.get(2).getName());
        assertEquals("User1", users.get(3).getName());
      }
    }
  }

  @Test
  void methodNotFound() throws NoSuchMethodException {
    try {
      Class<?> mapperType = ErrorMapper.class;
      Method mapperMethod = mapperType.getMethod("methodNotFound");
      new ProviderSqlSource(new Configuration(),
            mapperMethod.getAnnotation(SelectProvider.class), mapperType, mapperMethod);
      fail();
    } catch (BuilderException e) {
      assertTrue(e.getMessage().contains("Error creating SqlSource for SqlProvider. Method 'methodNotFound' not found in SqlProvider 'org.apache.ibatis.submitted.sqlprovider.SqlProviderTest$ErrorSqlBuilder'."));
    }
  }

  @Test
  void methodOverload() throws NoSuchMethodException {
    try {
      Class<?> mapperType = ErrorMapper.class;
      Method mapperMethod = mapperType.getMethod("methodOverload", String.class);
      new ProviderSqlSource(new Configuration(),
              mapperMethod.getAnnotation(SelectProvider.class), mapperType, mapperMethod);
      fail();
    } catch (BuilderException e) {
      assertTrue(e.getMessage().contains("Error creating SqlSource for SqlProvider. Method 'overload' is found multiple in SqlProvider 'org.apache.ibatis.submitted.sqlprovider.SqlProviderTest$ErrorSqlBuilder'. Sql provider method can not overload."));
    }
  }

  @Test
  void notSqlProvider() {
    try {
      new ProviderSqlSource(new Configuration(), new Object(), null, null);
      fail();
    } catch (BuilderException e) {
      assertTrue(e.getMessage().contains("Error creating SqlSource for SqlProvider.  Cause: java.lang.NoSuchMethodException: java.lang.Object.type()"));
    }
  }

  @Test
  void multipleProviderContext() throws NoSuchMethodException {
    try {
      Class<?> mapperType = ErrorMapper.class;
      Method mapperMethod = mapperType.getMethod("multipleProviderContext");
      new ProviderSqlSource(new Configuration(),
            mapperMethod.getAnnotation(SelectProvider.class), mapperType, mapperMethod);
      fail();
    } catch (BuilderException e) {
      assertTrue(e.getMessage().contains("Error creating SqlSource for SqlProvider. ProviderContext found multiple in SqlProvider method (org.apache.ibatis.submitted.sqlprovider.SqlProviderTest$ErrorSqlBuilder.multipleProviderContext). ProviderContext can not define multiple in SqlProvider method argument."));
    }
  }

  @Test
  void notSupportParameterObjectOnMultipleArguments() throws NoSuchMethodException {
    try {
      Class<?> mapperType = Mapper.class;
      Method mapperMethod = mapperType.getMethod("getUsersByName", String.class, String.class);
      new ProviderSqlSource(new Configuration(),
            mapperMethod.getAnnotation(SelectProvider.class), mapperType, mapperMethod)
              .getBoundSql(new Object());
      fail();
    } catch (BuilderException e) {
      assertTrue(e.getMessage().contains("Error invoking SqlProvider method (org.apache.ibatis.submitted.sqlprovider.OurSqlBuilder.buildGetUsersByNameQuery). Cannot invoke a method that holds multiple arguments using a specifying parameterObject. In this case, please specify a 'java.util.Map' object."));
    }
  }

  @Test
  void notSupportParameterObjectOnNamedArgument() throws NoSuchMethodException {
    try {
      Class<?> mapperType = Mapper.class;
      Method mapperMethod = mapperType.getMethod("getUsersByNameWithParamName", String.class);
      new ProviderSqlSource(new Configuration(),
            mapperMethod.getAnnotation(SelectProvider.class), mapperType, mapperMethod)
              .getBoundSql(new Object());
      fail();
    } catch (BuilderException e) {
      assertTrue(e.getMessage().contains("Error invoking SqlProvider method (org.apache.ibatis.submitted.sqlprovider.OurSqlBuilder.buildGetUsersByNameWithParamNameQuery). Cannot invoke a method that holds named argument(@Param) using a specifying parameterObject. In this case, please specify a 'java.util.Map' object."));
    }
  }

  @Test
  void invokeError() throws NoSuchMethodException {
    try {
      Class<?> mapperType = ErrorMapper.class;
      Method mapperMethod = mapperType.getMethod("invokeError");
      new ProviderSqlSource(new Configuration(),
            mapperMethod.getAnnotation(SelectProvider.class), mapperType, mapperMethod)
              .getBoundSql(new Object());
      fail();
    } catch (BuilderException e) {
      assertTrue(e.getMessage().contains("Error invoking SqlProvider method (org.apache.ibatis.submitted.sqlprovider.SqlProviderTest$ErrorSqlBuilder.invokeError).  Cause: java.lang.reflect.InvocationTargetException"));
    }
  }

  @Test
  void shouldInsertUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(999);
      user.setName("MyBatis");
      mapper.insert(user);

      User loadedUser = mapper.getUser(999);
      assertEquals("MyBatis", loadedUser.getName());
    }
  }

  @Test
  void shouldUpdateUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(999);
      user.setName("MyBatis");
      mapper.insert(user);

      user.setName("MyBatis3");
      mapper.update(user);

      User loadedUser = mapper.getUser(999);
      assertEquals("MyBatis3", loadedUser.getName());
    }
  }

  @Test
  void shouldDeleteUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(999);
      user.setName("MyBatis");
      mapper.insert(user);

      user.setName("MyBatis3");
      mapper.delete(999);

      User loadedUser = mapper.getUser(999);
      assertNull(loadedUser);
    }
  }

  @Test
  void mapperProviderContextOnly() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("User4", mapper.selectById(4).getName());
      assertNull(mapper.selectActiveById(4));
    }
  }

  @Test
  void mapperOneParamAndProviderContext() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals(1, mapper.selectByName("User4").size());
      assertEquals(0, mapper.selectActiveByName("User4").size());
    }
  }

  @Test
  void mapperMultipleParamAndProviderContextWithAtParam() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals(1, mapper.selectByIdAndNameWithAtParam(4,"User4").size());
      assertEquals(0, mapper.selectActiveByIdAndNameWithAtParam(4,"User4").size());
    }
  }

  @Test
  void mapperMultipleParamAndProviderContext() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals(1, mapper.selectByIdAndName(4,"User4").size());
      assertEquals(0, mapper.selectActiveByIdAndName(4,"User4").size());
    }
  }

  @Test
  void staticMethodNoArgument() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals(1, mapper.noArgument());
    }
  }

  @Test
  void staticMethodOneArgument() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals(10, mapper.oneArgument(10));
    }
  }

  @Test
  void staticMethodMultipleArgument() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals(2, mapper.multipleArgument(1, 1));
    }
  }

  @Test
  void staticMethodOnlyProviderContext() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals("onlyProviderContext", mapper.onlyProviderContext());
    }
  }

  @Test
  void staticMethodOneArgumentAndProviderContext() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals("oneArgumentAndProviderContext 100", mapper.oneArgumentAndProviderContext(100));
    }
  }

  public interface ErrorMapper {
    @SelectProvider(type = ErrorSqlBuilder.class, method = "methodNotFound")
    void methodNotFound();

    @SelectProvider(type = ErrorSqlBuilder.class, method = "overload")
    void methodOverload(String value);

    @SelectProvider(type = ErrorSqlBuilder.class, method = "invokeError")
    void invokeError();

    @SelectProvider(type = ErrorSqlBuilder.class, method = "multipleProviderContext")
    void multipleProviderContext();
  }

  @SuppressWarnings("unused")
  public static class ErrorSqlBuilder {
    public void methodNotFound() {
      throw new UnsupportedOperationException("methodNotFound");
    }

    public String overload() {
      throw new UnsupportedOperationException("overload");
    }

    public String overload(String value) {
      throw new UnsupportedOperationException("overload");
    }

    public String invokeError() {
      throw new UnsupportedOperationException("invokeError");
    }

    public String multipleProviderContext(ProviderContext providerContext1, ProviderContext providerContext2) {
      throw new UnsupportedOperationException("multipleProviderContext");
    }
  }

  public interface StaticMethodSqlProviderMapper {
    @SelectProvider(type = SqlProvider.class, method = "noArgument")
    int noArgument();

    @SelectProvider(type = SqlProvider.class, method = "oneArgument")
    int oneArgument(Integer value);

    @SelectProvider(type = SqlProvider.class, method = "multipleArgument")
    int multipleArgument(@Param("value1") Integer value1, @Param("value2") Integer value2);

    @SelectProvider(type = SqlProvider.class, method = "onlyProviderContext")
    String onlyProviderContext();

    @SelectProvider(type = SqlProvider.class, method = "oneArgumentAndProviderContext")
    String oneArgumentAndProviderContext(Integer value);

    @SuppressWarnings("unused")
    class SqlProvider {

      public static String noArgument() {
        return "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }

      public static StringBuilder oneArgument(Integer value) {
        return new StringBuilder().append("SELECT ").append(value)
            .append(" FROM INFORMATION_SCHEMA.SYSTEM_USERS");
      }

      public static CharSequence multipleArgument(@Param("value1") Integer value1,
          @Param("value2") Integer value2) {
        return "SELECT " + (value1 + value2) + " FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }

      public static CharSequence onlyProviderContext(ProviderContext context) {
        return new StringBuilder().append("SELECT '").append(context.getMapperMethod().getName())
            .append("' FROM INFORMATION_SCHEMA.SYSTEM_USERS");
      }

      public static String oneArgumentAndProviderContext(Integer value, ProviderContext context) {
        return "SELECT '" + context.getMapperMethod().getName() + " " + value
            + "' FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      }

    }

  }

  @Test
  void shouldInsertUserSelective() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(999);
      mapper.insertSelective(user);

      User loadedUser = mapper.getUser(999);
      assertNull(loadedUser.getName());
    }
  }


  @Test
  void shouldUpdateUserSelective() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(999);
      user.setName("MyBatis");
      mapper.insert(user);

      user.setName(null);
      mapper.updateSelective(user);

      User loadedUser = mapper.getUser(999);
      assertEquals("MyBatis", loadedUser.getName());
    }
  }

  @Test
  void mapperGetByEntity() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User query = new User();
      query.setName("User4");
      assertEquals(1, mapper.getByEntity(query).size());
      query = new User();
      query.setId(1);
      assertEquals(1, mapper.getByEntity(query).size());
      query = new User();
      query.setId(1);
      query.setName("User4");
      assertEquals(0, mapper.getByEntity(query).size());
    }
  }

  @Test
  void shouldPassedDatabaseIdToProviderMethod() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()){
      DatabaseIdMapper mapper = sqlSession.getMapper(DatabaseIdMapper.class);
      assertEquals("hsql", mapper.selectDatabaseId());
    }
    try (SqlSession sqlSession = sqlSessionFactoryForDerby.openSession()){
      DatabaseIdMapper mapper = sqlSession.getMapper(DatabaseIdMapper.class);
      assertEquals("derby", mapper.selectDatabaseId());
    }
  }

  interface DatabaseIdMapper {
    @SelectProvider(type = SqlProvider.class)
    String selectDatabaseId();

    @SuppressWarnings("unused")
    class SqlProvider {
      public static String provideSql(ProviderContext context) {
        if ("hsql".equals(context.getDatabaseId())) {
          return "SELECT '" + context.getDatabaseId() + "' FROM INFORMATION_SCHEMA.SYSTEM_USERS";
        } else {
          return "SELECT '" + context.getDatabaseId() + "' FROM SYSIBM.SYSDUMMY1";
        }
      }
    }
  }

}
