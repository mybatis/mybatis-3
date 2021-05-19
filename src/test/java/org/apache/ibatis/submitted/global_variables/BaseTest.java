/*
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
package org.apache.ibatis.submitted.global_variables;

import java.io.Reader;
import java.lang.reflect.Field;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BaseTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/global_variables/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/global_variables/CreateDB.sql");
  }

  @Test
  void shouldGetAUser() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      User user = mapper.getUser(1);
      CustomCache customCache = unwrap(sqlSessionFactory.getConfiguration().getCache(Mapper.class.getName()));
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("foo", customCache.getStringValue());
      Assertions.assertEquals(10, customCache.getIntegerValue().intValue());
      Assertions.assertEquals(1000, customCache.getLongValue());
    }
  }

  @Test
  void shouldGetAUserFromAnnotation() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      AnnotationMapper mapper = sqlSession.getMapper(AnnotationMapper.class);
      User user = mapper.getUser(1);
      CustomCache customCache = unwrap(sqlSessionFactory.getConfiguration().getCache(Mapper.class.getName()));
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("foo", customCache.getStringValue());
      Assertions.assertEquals(10, customCache.getIntegerValue().intValue());
      Assertions.assertEquals(1000, customCache.getLongValue());
    }
  }

  private CustomCache unwrap(Cache cache) {
    Field field;
    try {
      field = cache.getClass().getDeclaredField("delegate");
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
    try {
      field.setAccessible(true);
      return (CustomCache) field.get(cache);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } finally {
      field.setAccessible(false);
    }
  }

}
