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
package org.apache.ibatis.submitted.lazyload_proxyfactory_comparison;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

abstract class AbstractLazyTest {

  private SqlSession sqlSession;
  private Mapper mapper;

  protected abstract String getConfiguration();

  @BeforeEach
  void before() throws Exception {
    // create a SqlSessionFactory
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader(
        "org/apache/ibatis/submitted/lazyload_proxyfactory_comparison/mybatis-config-" + getConfiguration() + ".xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/lazyload_proxyfactory_comparison/CreateDB.sql");

    sqlSession = sqlSessionFactory.openSession();
    mapper = sqlSession.getMapper(Mapper.class);
  }

  @AfterEach
  void after() {
    if (sqlSession != null) {
      sqlSession.close();
    }
  }

  @Test
  void lazyLoadUserWithGetObjectWithInterface() {
    Assertions.assertNotNull(mapper.getUserWithGetObjectWithInterface(1).getOwner());
  }

  @Test
  void lazyLoadUserWithGetObjectWithoutInterface() {
    Assertions.assertNotNull(mapper.getUserWithGetObjectWithoutInterface(1).getOwner());
  }

  @Test
  void lazyLoadUserWithGetXxxWithInterface() {
    Assertions.assertNotNull(mapper.getUserWithGetXxxWithInterface(1).getOwner());
  }

  @Test
  void lazyLoadUserWithGetXxxWithoutInterface() {
    Assertions.assertNotNull(mapper.getUserWithGetXxxWithoutInterface(1).getOwner());
  }

  @Test
  void lazyLoadUserWithNothingWithInterface() {
    Assertions.assertNotNull(mapper.getUserWithNothingWithInterface(1).getOwner());
  }

  @Test
  void lazyLoadUserWithNothingWithoutInterface() {
    Assertions.assertNotNull(mapper.getUserWithNothingWithoutInterface(1).getOwner());
  }
}
