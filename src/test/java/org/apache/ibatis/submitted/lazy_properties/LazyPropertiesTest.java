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
package org.apache.ibatis.submitted.lazy_properties;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.executor.loader.cglib.CglibProxyFactory;
import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class LazyPropertiesTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/lazy_properties/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/lazy_properties/CreateDB.sql");
  }

  @Test
  void shouldLoadOnlyTheInvokedLazyProperty() {
    sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(false);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      assertEquals(0, user.setterCounter);
      assertNotNull(user.getLazy1());
      assertEquals(1, user.setterCounter, "Should NOT load other lazy properties.");
    }
  }

  @Test
  void verifyAggressiveLazyLoadingBehavior() {
    sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(true);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      // Setter invocation by MyBatis triggers aggressive lazy-loading.
      assertEquals(3, user.setterCounter, "Should load all lazy properties.");
    }
  }

  @Test
  void shouldToStringTriggerLazyLoading() {
    sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(false);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      user.toString();
      assertEquals(3, user.setterCounter);
    }
  }

  @Test
  void shouldHashCodeTriggerLazyLoading() {
    sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(false);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      user.hashCode();
      assertEquals(3, user.setterCounter);
    }
  }

  @Test
  void shouldEqualsTriggerLazyLoading() {
    sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(false);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      user.equals(null);
      assertEquals(3, user.setterCounter);
    }
  }

  @Test
  void shouldCloneTriggerLazyLoading() {
    sqlSessionFactory.getConfiguration().setAggressiveLazyLoading(false);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      user.clone();
      assertEquals(3, user.setterCounter);
    }
  }

  @Test
  void verifyEmptyLazyLoadTriggerMethods() {
    Configuration configuration = sqlSessionFactory.getConfiguration();
    configuration.setAggressiveLazyLoading(false);
    configuration.setLazyLoadTriggerMethods(new HashSet<>());
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      user.toString();
      user.hashCode();
      user.equals(null);
      user.clone();
      assertEquals(0, user.setterCounter);
    }
  }

  @Test
  void verifyCustomLazyLoadTriggerMethods() {
    Configuration configuration = sqlSessionFactory.getConfiguration();
    configuration.setAggressiveLazyLoading(false);
    configuration.setLazyLoadTriggerMethods(new HashSet<>(Collections.singleton("trigger")));
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      user.toString();
      user.hashCode();
      user.equals(null);
      user.clone();
      assertEquals(0, user.setterCounter);
      user.trigger();
      assertEquals(3, user.setterCounter);
    }
  }

  @Test
  void shouldInvokingSetterInvalidateLazyLoading_Javassist() {
    shoulInvokingSetterInvalidateLazyLoading(new JavassistProxyFactory());
  }

  @Tag("RequireIllegalAccess")
  @Test
  void shouldInvokingSetterInvalidateLazyLoading_Cglib() {
    shoulInvokingSetterInvalidateLazyLoading(new CglibProxyFactory());
  }

  private void shoulInvokingSetterInvalidateLazyLoading(ProxyFactory proxyFactory) {
    Configuration config = sqlSessionFactory.getConfiguration();
    config.setProxyFactory(proxyFactory);
    config.setAggressiveLazyLoading(false);
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      User u2 = new User();
      u2.setId(99);
      user.setLazy1(u2);
      assertEquals(1, user.setterCounter);
      assertEquals(Integer.valueOf(99), user.getLazy1().getId());
      assertEquals(1, user.setterCounter);
    }
  }
}
