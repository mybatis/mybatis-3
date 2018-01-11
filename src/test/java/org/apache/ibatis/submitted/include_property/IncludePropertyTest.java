/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.include_property;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class IncludePropertyTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create an SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/include_property/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/include_property/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    conn.close();
    reader.close();
    session.close();
  }

  @Test
  public void testSimpleProperty() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<String> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectSimpleA");
      assertEquals("col_a value", results.get(0));
      results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectSimpleB");
      assertEquals("col_b value", results.get(0));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testPropertyContext() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Map<String, String>> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectPropertyContext");
      Map<String, String> map = results.get(0);
      assertEquals(2, map.size());
      assertEquals("col_a value", map.get("COL_A"));
      assertEquals("col_b value", map.get("COL_B"));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testNestedDynamicValue() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<String> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectNestedDynamicValue");
      assertEquals("col_a value", results.get(0));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testEmptyString() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<String> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectEmptyProperty");
      assertEquals("a value", results.get(0));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testPropertyInRefid() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<String> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectPropertyInRefid");
      assertEquals("col_a value", results.get(0));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testConfigVar() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<String> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectConfigVar");
      assertEquals("Property defined in the config file should be used.", "col_c value", results.get(0));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testRuntimeVar() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("suffix", "b");
      List<String> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectRuntimeVar", params);
      assertEquals("col_b value", results.get(0));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testNestedInclude() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<String> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectNestedInclude");
      assertEquals("a value", results.get(0));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testParametersInAttribute() throws Exception {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Map<String, String>> results = sqlSession.selectList("org.apache.ibatis.submitted.include_property.Mapper.selectPropertyInAttribute");
      Map<String, String> map = results.get(0);
      assertEquals(2, map.size());
      assertEquals("col_a value", map.get("COL_1"));
      assertEquals("col_b value", map.get("COL_2"));
    } finally {
      sqlSession.close();
    }
  }
}
