/**
 *    Copyright 2009-2017 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Reader;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqlProviderTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/sqlprovider/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    sqlSessionFactory.getConfiguration().addMapper(StaticMethodSqlProviderMapper.class);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/sqlprovider/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  // Test for list
  @Test
  public void shouldGetTwoUsers() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Integer> list = new ArrayList<Integer>();
      list.add(1);
      list.add(3);
      List<User> users = mapper.getUsers(list);
      assertEquals(2, users.size());
      assertEquals("User1", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
    } finally {
      sqlSession.close();
    }
  }

  // Test for simple value without @Param
  @Test
  public void shouldGetOneUser() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
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
    } finally {
      sqlSession.close();
    }
  }

  // Test for empty
  @Test
  public void shouldGetAllUsers() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getAllUsers();
      assertEquals(4, users.size());
      assertEquals("User1", users.get(0).getName());
      assertEquals("User2", users.get(1).getName());
      assertEquals("User3", users.get(2).getName());
      assertEquals("User4", users.get(3).getName());
    } finally {
      sqlSession.close();
    }
  }

  // Test for single JavaBean
  @Test
  public void shouldGetUsersByCriteria() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
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
    } finally {
      sqlSession.close();
    }
  }

  // Test for single map
  @Test
  public void shouldGetUsersByCriteriaMap() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      {
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("id", 1);
        List<User> users = mapper.getUsersByCriteriaMap(criteria);
        assertEquals(1, users.size());
        assertEquals("User1", users.get(0).getName());
      }
      {
        Map<String, Object> criteria = new HashMap<String, Object>();
        criteria.put("name", "User");
        List<User> users = mapper.getUsersByCriteriaMap(criteria);
        assertEquals(4, users.size());
        assertEquals("User1", users.get(0).getName());
        assertEquals("User2", users.get(1).getName());
        assertEquals("User3", users.get(2).getName());
        assertEquals("User4", users.get(3).getName());
      }
    } finally {
      sqlSession.close();
    }
  }

  // Test for multiple parameter without @Param
  @Test
  public void shouldGetUsersByName() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersByName("User", "id DESC");
      assertEquals(4, users.size());
      assertEquals("User4", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
      assertEquals("User2", users.get(2).getName());
      assertEquals("User1", users.get(3).getName());
    } finally {
      sqlSession.close();
    }
  }

  // Test for map without @Param
  @Test
  public void shouldGetUsersByNameUsingMap() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersByNameUsingMap("User", "id DESC");
      assertEquals(4, users.size());
      assertEquals("User4", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
      assertEquals("User2", users.get(2).getName());
      assertEquals("User1", users.get(3).getName());
    } finally {
      sqlSession.close();
    }
  }

  // Test for multiple parameter with @Param
  @Test
  public void shouldGetUsersByNameWithParamNameAndOrderBy() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersByNameWithParamNameAndOrderBy("User", "id DESC");
      assertEquals(4, users.size());
      assertEquals("User4", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
      assertEquals("User2", users.get(2).getName());
      assertEquals("User1", users.get(3).getName());
    } finally {
      sqlSession.close();
    }
  }

  // Test for map with @Param
  @Test
  public void shouldGetUsersByNameWithParamNameUsingMap() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = mapper.getUsersByNameWithParamNameAndOrderBy("User", "id DESC");
      assertEquals(4, users.size());
      assertEquals("User4", users.get(0).getName());
      assertEquals("User3", users.get(1).getName());
      assertEquals("User2", users.get(2).getName());
      assertEquals("User1", users.get(3).getName());
    } finally {
      sqlSession.close();
    }
  }

  // Test for simple value with @Param
  @Test
  public void shouldGetUsersByNameWithParamName() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
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
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void methodNotFound() throws NoSuchMethodException {
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
  public void methodOverload() throws NoSuchMethodException {
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
  public void notSqlProvider() throws NoSuchMethodException {
    try {
      new ProviderSqlSource(new Configuration(), new Object(), null, null);
      fail();
    } catch (BuilderException e) {
      assertTrue(e.getMessage().contains("Error creating SqlSource for SqlProvider.  Cause: java.lang.NoSuchMethodException: java.lang.Object.type()"));
    }
  }

  @Test
  public void multipleProviderContext() throws NoSuchMethodException {
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
  public void notSupportParameterObjectOnMultipleArguments() throws NoSuchMethodException {
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
  public void notSupportParameterObjectOnNamedArgument() throws NoSuchMethodException {
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
  public void invokeError() throws NoSuchMethodException {
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
  public void shouldInsertUser() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(999);
      user.setName("MyBatis");
      mapper.insert(user);

      User loadedUser = mapper.getUser(999);
      assertEquals("MyBatis", loadedUser.getName());

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldUpdateUser() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(999);
      user.setName("MyBatis");
      mapper.insert(user);

      user.setName("MyBatis3");
      mapper.update(user);

      User loadedUser = mapper.getUser(999);
      assertEquals("MyBatis3", loadedUser.getName());

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldDeleteUser() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = new User();
      user.setId(999);
      user.setName("MyBatis");
      mapper.insert(user);

      user.setName("MyBatis3");
      mapper.delete(999);

      User loadedUser = mapper.getUser(999);
      assertNull(loadedUser);

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void mapperProviderContextOnly() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("User4", mapper.selectById(4).getName());
      assertNull(mapper.selectActiveById(4));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void mapperOneParamAndProviderContext() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals(1, mapper.selectByName("User4").size());
      assertEquals(0, mapper.selectActiveByName("User4").size());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void mapperMultipleParamAndProviderContextWithAtParam() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals(1, mapper.selectByIdAndNameWithAtParam(4,"User4").size());
      assertEquals(0, mapper.selectActiveByIdAndNameWithAtParam(4,"User4").size());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void mapperMultipleParamAndProviderContext() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals(1, mapper.selectByIdAndName(4,"User4").size());
      assertEquals(0, mapper.selectActiveByIdAndName(4,"User4").size());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void staticMethodNoArgument() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals(1, mapper.noArgument());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void staticMethodOneArgument() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals(10, mapper.oneArgument(10));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void staticMethodMultipleArgument() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals(2, mapper.multipleArgument(1, 1));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void staticMethodOnlyProviderContext() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals("onlyProviderContext", mapper.onlyProviderContext());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void staticMethodOneArgumentAndProviderContext() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      StaticMethodSqlProviderMapper mapper =
          sqlSession.getMapper(StaticMethodSqlProviderMapper.class);
      assertEquals("oneArgumentAndProviderContext 100", mapper.oneArgumentAndProviderContext(100));
    } finally {
      sqlSession.close();
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

}
