/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.dynsql;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynSqlTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;
    try {
      Reader configReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/dynsql/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
      configReader.close();
      conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();

      Reader scriptReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/dynsql/CreateDB.sql");
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(scriptReader);
      conn.commit();
      scriptReader.close();

    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testSelect() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Integer> ids = new ArrayList<Integer>();
      ids.add(1);
      ids.add(3);
      ids.add(5);
      Parameter parameter = new Parameter();
      parameter.setEnabled(true);
      parameter.setSchema("ibtest");
      parameter.setIds(ids);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.select", parameter);

      assertTrue(answer.size() == 3);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSelectSimple() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Integer> ids = new ArrayList<Integer>();
      ids.add(1);
      ids.add(3);
      ids.add(5);
      Parameter parameter = new Parameter();
      parameter.setEnabled(true);
      parameter.setSchema("ibtest");
      parameter.setIds(ids);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.select_simple", parameter);

      assertTrue(answer.size() == 3);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSelectLike() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.selectLike", "Ba");

      assertTrue(answer.size() == 2);
      assertEquals(new Integer(4), answer.get(0).get("ID"));
      assertEquals(new Integer(6), answer.get(1).get("ID"));

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testNumerics() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<NumericRow> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.selectNumerics");

      assertTrue(answer.size() == 1);

      NumericRow row = answer.get(0);
      assertEquals(1, (int) row.getId());
      assertEquals(2, (int) row.getTinynumber());
      assertEquals(3, (int) row.getSmallnumber());
      assertEquals(4l, (long) row.getLonginteger());
      assertEquals(new BigInteger("5"), row.getBiginteger());
      assertEquals(new BigDecimal("6.00"), row.getNumericnumber());
      assertEquals(new BigDecimal("7.00"), row.getDecimalnumber());
      assertEquals((Float) 8.0f, row.getRealnumber());
      assertEquals((Float) 9.0f, row.getFloatnumber());
      assertEquals((Double) 10.0, row.getDoublenumber());

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testOgnlStaticMethodCall() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.ognlStaticMethodCall", "Rock 'n Roll");
      assertTrue(answer.size() == 1);
      assertEquals(new Integer(7), answer.get(0).get("ID"));

    } finally {
      sqlSession.close();
    }
  }

}
