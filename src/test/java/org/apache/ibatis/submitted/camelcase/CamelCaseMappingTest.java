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
package org.apache.ibatis.submitted.camelcase;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CamelCaseMappingTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/camelcase/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/camelcase/CreateDB.sql");
  }

  @Test
  void testList() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Camel> list = sqlSession.selectList("org.apache.ibatis.submitted.camel.doSelect");
      Assertions.assertTrue(list.size() > 0);
      Assertions.assertNotNull(list.get(0).getFirstName());
      Assertions.assertNull(list.get(0).getLAST_NAME());
    }
  }

  @Test
  void testMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Map<String, Object>> list = sqlSession.selectList("org.apache.ibatis.submitted.camel.doSelectMap");
      Assertions.assertTrue(list.size() > 0);
      Assertions.assertTrue(list.get(0).containsKey("LAST_NAME"));
    }
  }

}
