/*
 * Copyright 2012 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.submitted.language;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Just a test case. Not a real Velocity implementation.
 */
public class LanguageTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:language", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/language/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/language/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testDynamicSelectWithPropertyParams() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {

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
        assertTrue(n.getLastName() == null);
      }

      p = new Parameter(false, "Rub%");
      answer = sqlSession.selectList("selectNames", p);
      assertEquals(2, answer.size());
      for (Name n : answer) {
        assertTrue(n.getLastName() == null);
      }

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testDynamicSelectWithExpressionParams() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {

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
        assertTrue(n.getLastName() == null);
      }

      p = new Parameter(false, "Rub");
      answer = sqlSession.selectList("selectNamesWithExpressions", p);
      assertEquals(2, answer.size());
      for (Name n : answer) {
        assertTrue(n.getLastName() == null);
      }

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testDynamicSelectWithIteration() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {

      int[] ids = { 2, 4, 5 };
      Map<String, Object> param = new HashMap<String, Object>();
      param.put("ids", ids);
      List<Name> answer = sqlSession.selectList("selectNamesWithIteration", param);
      assertEquals(3, answer.size());
      for (int i = 0; i < ids.length; i++) {
        assertEquals(ids[i], answer.get(i).getId());
      }

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testLangRaw() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectRaw", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testLangRawWithInclude() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectRawWithInclude", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    } finally {
      sqlSession.close();
    }
  }
  @Test
  public void testLangRawWithIncludeAndCData() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectRawWithIncludeAndCData", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void testLangXmlTags() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("selectXml", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testLangRawWithMapper() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter p = new Parameter(true, "Fli%");
      Mapper m = sqlSession.getMapper(Mapper.class);
      List<Name> answer = m.selectRawWithMapper(p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testLangVelocityWithMapper() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter p = new Parameter(true, "Fli%");
      Mapper m = sqlSession.getMapper(Mapper.class);
      List<Name> answer = m.selectVelocityWithMapper(p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testLangXmlWithMapper() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter p = new Parameter(true, "Fli%");
      Mapper m = sqlSession.getMapper(Mapper.class);
      List<Name> answer = m.selectXmlWithMapper(p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testLangXmlWithMapperAndSqlSymbols() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter p = new Parameter(true, "Fli%");
      Mapper m = sqlSession.getMapper(Mapper.class);
      List<Name> answer = m.selectXmlWithMapperAndSqlSymbols(p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }
    } finally {
      sqlSession.close();
    }
  }

}
