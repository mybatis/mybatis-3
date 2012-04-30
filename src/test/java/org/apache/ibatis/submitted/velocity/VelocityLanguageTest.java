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
package org.apache.ibatis.submitted.velocity;

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
public class VelocityLanguageTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:bname", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/velocity/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/velocity/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDynamicSelectWithPropertyParams() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {

      Parameter p = new Parameter(true, "Fli%");
      List<Name> answer = sqlSession.selectList("org.apache.ibatis.submitted.velocity.selectNames", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }

      p = new Parameter(false, "Fli%");
      answer = sqlSession.selectList("org.apache.ibatis.submitted.velocity.selectNames", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertTrue(n.getLastName() == null);
      }

      p = new Parameter(false, "Rub%");
      answer = sqlSession.selectList("org.apache.ibatis.submitted.velocity.selectNames", p);
      assertEquals(2, answer.size());
      for (Name n : answer) {
        assertTrue(n.getLastName() == null);
      }

    } finally {
      sqlSession.close();
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDynamicSelectWithExpressionParams() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {

      Parameter p = new Parameter(true, "Fli");
      List<Name> answer = sqlSession.selectList("org.apache.ibatis.submitted.velocity.selectNamesWithExpressions", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertEquals("Flintstone", n.getLastName());
      }

      p = new Parameter(false, "Fli");
      answer = sqlSession.selectList("org.apache.ibatis.submitted.velocity.selectNamesWithExpressions", p);
      assertEquals(3, answer.size());
      for (Name n : answer) {
        assertTrue(n.getLastName() == null);
      }

      p = new Parameter(false, "Rub");
      answer = sqlSession.selectList("org.apache.ibatis.submitted.velocity.selectNamesWithExpressions", p);
      assertEquals(2, answer.size());
      for (Name n : answer) {
        assertTrue(n.getLastName() == null);
      }

    } finally {
      sqlSession.close();
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testDynamicSelectWithIteration() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {

      int[] ids = {2,4,5};
      Map param = new HashMap();
      param.put("ids", ids);
      List<Name> answer = sqlSession.selectList("org.apache.ibatis.submitted.velocity.selectNamesWithIteration", param);
      assertEquals(3, answer.size());
      for (int i=0; i<ids.length; i++) {
        assertEquals(ids[i], answer.get(i).getId());
      }

    } finally {
      sqlSession.close();
    }
  }

}
