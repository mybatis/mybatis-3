/**
 *    Copyright 2009-2018 the original author or authors.
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

public abstract class AbstractLazyTest {

  private SqlSessionFactory sqlSessionFactory;
  private SqlSession sqlSession;
  private Mapper mapper;

  protected abstract String getConfiguration();

  @BeforeEach
  public void before() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/lazyload_proxyfactory_comparison/mybatis-config-" + getConfiguration() + ".xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/lazyload_proxyfactory_comparison/CreateDB.sql");

    sqlSession = sqlSessionFactory.openSession();
    mapper = sqlSession.getMapper(Mapper.class);
  }

  @AfterEach
  public void after() {
    if (sqlSession != null) {
      sqlSession.close();
    }
  }

  @Test
  public void lazyLoadUserWithGetObjectWithInterface() throws Exception {
    Assertions.assertNotNull(mapper.getUserWithGetObjectWithInterface(1).getOwner());
  }

  @Test
  public void lazyLoadUserWithGetObjectWithoutInterface() throws Exception {
    Assertions.assertNotNull(mapper.getUserWithGetObjectWithoutInterface(1).getOwner());
  }

  @Test
  public void lazyLoadUserWithGetXxxWithInterface() throws Exception {
    Assertions.assertNotNull(mapper.getUserWithGetXxxWithInterface(1).getOwner());
  }

  @Test
  public void lazyLoadUserWithGetXxxWithoutInterface() throws Exception {
    Assertions.assertNotNull(mapper.getUserWithGetXxxWithoutInterface(1).getOwner());
  }

  @Test
  public void lazyLoadUserWithNothingWithInterface() throws Exception {
    Assertions.assertNotNull(mapper.getUserWithNothingWithInterface(1).getOwner());
  }

  @Test
  public void lazyLoadUserWithNothingWithoutInterface() throws Exception {
    Assertions.assertNotNull(mapper.getUserWithNothingWithoutInterface(1).getOwner());
  }
}
