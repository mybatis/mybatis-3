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
package org.apache.ibatis.submitted.nested;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NestedForEachTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/nested/CreateDB.sql");
  }

  @Test
  void testSimpleSelect() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setLastName("Flintstone");
      Parameter parameter = new Parameter();
      parameter.addName(name);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.nested.Mapper.simpleSelect",
          parameter);

      assertEquals(3, answer.size());
    }
  }

  @Test
  void testSimpleSelectWithPrimitives() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, Object> parameter = new HashMap<>();
      int[] array = new int[] { 1, 3, 5 };
      parameter.put("ids", array);

      List<Map<String, Object>> answer = sqlSession
          .selectList("org.apache.ibatis.submitted.nested.Mapper.simpleSelectWithPrimitives", parameter);

      assertEquals(3, answer.size());
    }
  }

  @Test
  void testSimpleSelectWithMapperAndPrimitives() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Map<String, Object>> answer = mapper.simpleSelectWithMapperAndPrimitives(1, 3, 5);
      assertEquals(3, answer.size());
    }
  }

  @Test
  void testNestedSelect() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setLastName("Flintstone");
      name.addFirstName("Fred");
      name.addFirstName("Wilma");

      Parameter parameter = new Parameter();
      parameter.addName(name);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.nested.Mapper.nestedSelect",
          parameter);

      assertEquals(2, answer.size());
    }
  }

  @Test
  void testNestedSelect2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Name name = new Name();
      name.setLastName("Flintstone");
      name.addFirstName("Fred");
      name.addFirstName("Wilma");

      Parameter parameter = new Parameter();
      parameter.addName(name);

      name = new Name();
      name.setLastName("Rubble");
      name.addFirstName("Betty");
      parameter.addName(name);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.nested.Mapper.nestedSelect",
          parameter);

      assertEquals(3, answer.size());
    }
  }
}
