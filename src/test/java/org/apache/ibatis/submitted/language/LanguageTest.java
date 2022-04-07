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
package org.apache.ibatis.submitted.language;

import static org.junit.jupiter.api.Assertions.*;

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

/**
 * Just a test case. Not a real Velocity implementation.
 */
class LanguageTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/language/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/language/CreateDB.sql");
  }

  @Test
  void testDynamicSelectWithPropertyParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectNames", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }

      p = new Parameter(false, "Fli%");
      answer = sqlSession.selectList("selectNames", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertNull(n.getLastName());
      }

      p = new Parameter(false, "Rub%");
      answer = sqlSession.selectList("selectNames", p);
      assertEquals(2, answer.size());
      for (Name n : answer) {
        assertNull(n.getLastName());
      }
    }
  }

  @Test
  void testDynamicSelectWithExpressionParams() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      Parameter p = new Parameter(true, "Fli");
      List<Name> answer = sqlSession.selectList("selectNamesWithExpressions", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }

      p = new Parameter(false, "Fli");
      answer = sqlSession.selectList("selectNamesWithExpressions", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertNull(n.getLastName());
      }

      p = new Parameter(false, "Rub");
      answer = sqlSession.selectList("selectNamesWithExpressions", p);
      assertEquals(2, answer.size());
      for (Name n : answer) {
        assertNull(n.getLastName());
      }
    }
  }

  @Test
  void testDynamicSelectWithIteration() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      int[] ids = { 2, 4, 5 };
      Map<String, Object> param = new HashMap<>();
      param.put("ids", ids);
      List<Name> answer = sqlSession.selectList("selectNamesWithIteration", param);
      assertEquals(3, answer.size());
      for (int i = 0; i < ids.length; i++) {
        assertEquals(ids[i], answer.get(i).getId());
      }
    }
  }

  @Test
  void testLangRaw() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectRaw", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testLangRawWithInclude() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectRawWithInclude", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testLangRawWithIncludeAndCData() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectRawWithIncludeAndCData", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testLangXmlTags() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectXml", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testLangRawWithMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter p = new Parameter(true, "Fli%");
      Mapper m = sqlSession.getMapper(Mapper.class);
      List<Name> answer = m.selectRawWithMapper(p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testLangVelocityWithMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter p = new Parameter(true, "Fli%");
      Mapper m = sqlSession.getMapper(Mapper.class);
      List<Name> answer = m.selectVelocityWithMapper(p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testLangXmlWithMapper() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter p = new Parameter(true, "Fli%");
      Mapper m = sqlSession.getMapper(Mapper.class);
      List<Name> answer = m.selectXmlWithMapper(p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

  @Test
  void testLangXmlWithMapperAndSqlSymbols() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter p = new Parameter(true, "Fli%");
      Mapper m = sqlSession.getMapper(Mapper.class);
      List<Name> answer = m.selectXmlWithMapperAndSqlSymbols(p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    }
  }

}
