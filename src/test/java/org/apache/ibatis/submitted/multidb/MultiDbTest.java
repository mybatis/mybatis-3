/**
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.multidb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MultiDbTest {

  protected static SqlSessionFactory sqlSessionFactory;
  protected static SqlSessionFactory sqlSessionFactory2;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multidb/MultiDbConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/multidb/CreateDB.sql");
  }

  @Test
  void shouldExecuteHsqlQuery() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      String answer = mapper.select1(1);
      assertEquals("hsql", answer);
    }
  }

  @Test
  void shouldExecuteCommonQuery() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      String answer = mapper.select2(1);
      assertEquals("common", answer);
    }
  }

  @Test
  void shouldExecuteHsqlQueryWithDynamicIf() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      String answer = mapper.select3(1);
      assertEquals("hsql", answer);
    }
  }

  @Test
  void shouldExecuteHsqlQueryWithInclude() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      String answer = mapper.select4(1);
      assertEquals("hsql", answer);
    }
  }

  @Test
  void shouldInsertInCommonWithSelectKey() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      mapper.insert(new User(2, "test"));
      String answer = mapper.select2(1);
      assertEquals("common", answer);
    }
  }

  @Test
  void shouldInsertInCommonWithSelectKey2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      mapper.insert2(new User(2, "test"));
      String answer = mapper.select2(1);
      assertEquals("common", answer);
    }
  }

}
