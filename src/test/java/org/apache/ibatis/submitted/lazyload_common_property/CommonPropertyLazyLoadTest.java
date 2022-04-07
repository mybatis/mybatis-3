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
package org.apache.ibatis.submitted.lazyload_common_property;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CommonPropertyLazyLoadTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void initDatabase() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/lazyload_common_property/ibatisConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/lazyload_common_property/CreateDB.sql");
  }

  @Test
  void testLazyLoadWithNoAncestor() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      ChildMapper childMapper = sqlSession.getMapper(ChildMapper.class);

      childMapper.selectById(1);
    }
  }

  @Test
  void testLazyLoadWithFirstAncestor() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);
      ChildMapper childMapper = sqlSession.getMapper(ChildMapper.class);

      fatherMapper.selectById(1);
      childMapper.selectById(1);
    }
  }

  @Test
  void testLazyLoadWithAllAncestors() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      GrandFatherMapper grandFatherMapper = sqlSession.getMapper(GrandFatherMapper.class);
      FatherMapper fatherMapper = sqlSession.getMapper(FatherMapper.class);
      ChildMapper childMapper = sqlSession.getMapper(ChildMapper.class);

      grandFatherMapper.selectById(1);
      fatherMapper.selectById(1);
      childMapper.selectById(1);
    }
  }

  @Test
  void testLazyLoadSkipFirstAncestor() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      GrandFatherMapper grandFatherMapper = sqlSession.getMapper(GrandFatherMapper.class);
      ChildMapper childMapper = sqlSession.getMapper(ChildMapper.class);

      grandFatherMapper.selectById(1);
      childMapper.selectById(1);
    }
  }
}
