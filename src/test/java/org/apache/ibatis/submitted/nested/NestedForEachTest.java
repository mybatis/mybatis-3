/**
 *    Copyright 2009-2015 the original author or authors.
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

import static org.junit.Assert.assertEquals;

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
import org.junit.BeforeClass;
import org.junit.Test;

public class NestedForEachTest {

  protected static SqlSessionFactory sqlSessionFactory;
  
  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:nested", "sa",
          "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testSimpleSelect() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Name name = new Name();
      name.setLastName("Flintstone");
      Parameter parameter = new Parameter();
      parameter.addName(name);

      List<Map<String, Object>> answer =
          sqlSession.selectList("org.apache.ibatis.submitted.nested.Mapper.simpleSelect", parameter);

      assertEquals(3, answer.size());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSimpleSelectWithPrimitives() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Map<String, Object> parameter = new HashMap<String, Object>();
      int[] array = new int[] {1, 3, 5};
      parameter.put("ids", array);

      List<Map<String, Object>> answer =
          sqlSession.selectList("org.apache.ibatis.submitted.nested.Mapper.simpleSelectWithPrimitives", parameter);

      assertEquals(3, answer.size());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSimpleSelectWithMapperAndPrimitives() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Map<String, Object>> answer = mapper.simpleSelectWithMapperAndPrimitives(1, 3, 5);
      assertEquals(3, answer.size());
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void testNestedSelect() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Name name = new Name();
      name.setLastName("Flintstone");
      name.addFirstName("Fred");
      name.addFirstName("Wilma");

      Parameter parameter = new Parameter();
      parameter.addName(name);

      List<Map<String, Object>> answer =
          sqlSession.selectList("org.apache.ibatis.submitted.nested.Mapper.nestedSelect", parameter);

      assertEquals(2, answer.size());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testNestedSelect2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
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

      List<Map<String, Object>> answer =
          sqlSession.selectList("org.apache.ibatis.submitted.nested.Mapper.nestedSelect", parameter);

      assertEquals(3, answer.size());
    } finally {
      sqlSession.close();
    }
  }
}
