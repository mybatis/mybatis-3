/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Reader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PluginTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/plugin/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/plugin/CreateDB.sql");
  }

  @Test
  void shouldPluginSwitchSchema() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("Public user 1", mapper.selectNameById(1));
    }

    SchemaHolder.set("MYSCHEMA");

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      assertEquals("Private user 1", mapper.selectNameById(1));
    }
  }

  static final class SchemaHolder {
    private static ThreadLocal<String> value = ThreadLocal.withInitial(() -> "PUBLIC");

    public static void set(String tenantName) {
      value.set(tenantName);
    }

    public static String get() {
      return value.get();
    }

    private SchemaHolder() {
    }
  }

  @Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }))
  public static class SwitchCatalogInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
      Object[] args = invocation.getArgs();
      Connection con = (Connection) args[0];
      con.setSchema(SchemaHolder.get());
      return invocation.proceed();
    }
  }

  @Test
  void shouldPluginNotInvokeArbitraryMethod() {
    Map<?, ?> map = new HashMap<>();
    map = (Map<?, ?>) new AlwaysMapPlugin().plugin(map);
    try {
      map.get("Anything");
      fail("Exected IllegalArgumentException, but no exception was thrown.");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "Method 'public abstract java.lang.Object java.util.Map.get(java.lang.Object)' is not supported as a plugin target.",
          e.getMessage());
    } catch (Exception e) {
      fail("Exected IllegalArgumentException, but was " + e.getClass(), e);
    }
  }

  @Intercepts({ @Signature(type = Map.class, method = "get", args = { Object.class }) })
  public static class AlwaysMapPlugin implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) {
      return "Always";
    }
  }
}
