/**
 *    Copyright 2009-2016 the original author or authors.
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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SqlProviderTest {

  private static SqlSessionFactory sqlSessionFactory;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/sqlprovider/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/sqlprovider/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
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
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Error creating SqlSource for SqlProvider. Method 'methodNotFound' not found in SqlProvider 'org.apache.ibatis.submitted.sqlprovider.SqlProviderTest$ErrorSqlBuilder'."));
    new ProviderSqlSource(new Configuration(),
            ErrorMapper.class.getMethod("methodNotFound").getAnnotation(SelectProvider.class));
  }

  @Test
  public void methodOverload() throws NoSuchMethodException {
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Error creating SqlSource for SqlProvider. Method 'overload' is found multiple in SqlProvider 'org.apache.ibatis.submitted.sqlprovider.SqlProviderTest$ErrorSqlBuilder'. Sql provider method can not overload."));
    new ProviderSqlSource(new Configuration(),
            ErrorMapper.class.getMethod("methodOverload", String.class).getAnnotation(SelectProvider.class));
  }

  @Test
  public void notSqlProvider() throws NoSuchMethodException {
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Error creating SqlSource for SqlProvider.  Cause: java.lang.NoSuchMethodException: java.lang.Object.type()"));
    new ProviderSqlSource(new Configuration(), new Object());
  }

  @Test
  public void notSupportParameterObjectOnMultipleArguments() throws NoSuchMethodException {
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Error invoking SqlProvider method (org.apache.ibatis.submitted.sqlprovider.OurSqlBuilder.buildGetUsersByNameQuery). Cannot invoke a method that holds multiple arguments using a specifying parameterObject. In this case, please specify a 'java.util.Map' object."));
    new ProviderSqlSource(new Configuration(),
            Mapper.class.getMethod("getUsersByName", String.class, String.class).getAnnotation(SelectProvider.class))
            .getBoundSql(new Object());
  }

  @Test
  public void notSupportParameterObjectOnNamedArgument() throws NoSuchMethodException {
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Error invoking SqlProvider method (org.apache.ibatis.submitted.sqlprovider.OurSqlBuilder.buildGetUsersByNameWithParamNameQuery). Cannot invoke a method that holds named argument(@Param) using a specifying parameterObject. In this case, please specify a 'java.util.Map' object."));
    new ProviderSqlSource(new Configuration(),
            Mapper.class.getMethod("getUsersByNameWithParamName", String.class).getAnnotation(SelectProvider.class))
            .getBoundSql(new Object());
  }

  @Test
  public void invokeError() throws NoSuchMethodException {
    expectedException.expect(BuilderException.class);
    expectedException.expectMessage(is("Error invoking SqlProvider method (org.apache.ibatis.submitted.sqlprovider.SqlProviderTest$ErrorSqlBuilder.invokeError).  Cause: java.lang.reflect.InvocationTargetException"));
    new ProviderSqlSource(new Configuration(),
            ErrorMapper.class.getMethod("invokeError").getAnnotation(SelectProvider.class))
            .getBoundSql(new Object());
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

  public interface ErrorMapper {
    @SelectProvider(type = ErrorSqlBuilder.class, method = "methodNotFound")
    void methodNotFound();

    @SelectProvider(type = ErrorSqlBuilder.class, method = "overload")
    void methodOverload(String value);

    @SelectProvider(type = ErrorSqlBuilder.class, method = "invokeError")
    void invokeError();
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
  }

}
