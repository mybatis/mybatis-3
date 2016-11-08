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
package org.apache.ibatis.submitted.lazy_properties;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.executor.loader.cglib.CglibProxyFactory;
import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class LazyPropertiesTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/lazy_properties/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/lazy_properties/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }

  @Test
  public void shouldLoadOnlyTheInvokedLazyProperty() {
    sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(false);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      assertEquals(0, user.lazyLoadCounter);
      assertNotNull(user.getLazy1());
      assertEquals("Should NOT load other lazy properties.", 1, user.lazyLoadCounter);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void verifyAggressiveLazyLoadingBehavior() {
    sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(true);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      // Setter invocation by MyBatis triggers aggressive lazy-loading.
      assertEquals("Should load all lazy properties.", 3,
          user.lazyLoadCounter);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldInvokingSetterNotTriggerLazyLoading_Javassist() {
    Configuration config = sqlSessionFactory.getConfiguration();
    config.setProxyFactory(new JavassistProxyFactory());
    config.setAggressiveLazyLoading(false);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      user.setLazy1(new User());
      assertNotNull(user.getLazy1().getId());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldInvokingSetterNotTriggerLazyLoading_Cglib() {
    Configuration config = sqlSessionFactory.getConfiguration();
    config.setProxyFactory(new CglibProxyFactory());
    config.setAggressiveLazyLoading(false);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      user.setLazy1(new User());
      assertNotNull(user.getLazy1().getId());
    } finally {
      sqlSession.close();
    }
  }
}
