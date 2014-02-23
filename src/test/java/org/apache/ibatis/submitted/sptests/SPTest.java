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
package org.apache.ibatis.submitted.sptests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Reader;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
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

public class SPTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void initDatabase() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:sptest", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/sptests/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setDelimiter("go");
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/sptests/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  /*
   * This test shows how to use input and output parameters in a stored
   * procedure. This procedure does not return a result set.
   * 
   * This test shows using a multi-property parameter.
   */
  @Test
  public void testAdderAsSelect() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      spMapper.adderAsSelect(parameter);

      assertEquals((Integer) 5, parameter.getSum());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use input and output parameters in a stored
   * procedure. This procedure does not return a result set.
   * 
   * This test shows using a multi-property parameter.
   */
  @Test
  public void testAdderAsSelectDoubleCall1() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      spMapper.adderAsSelect(parameter);
      assertEquals((Integer) 5, parameter.getSum());

      parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);
      spMapper.adderAsSelect(parameter);
      assertEquals((Integer) 5, parameter.getSum());

    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use input and output parameters in a stored
   * procedure. This procedure does not return a result set.
   * 
   * This test also demonstrates session level cache for output parameters.
   * 
   * This test shows using a multi-property parameter.
   */
  @Test
  public void testAdderAsSelectDoubleCall2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      spMapper.adderAsSelect(parameter);
      assertEquals((Integer) 5, parameter.getSum());

      parameter = new Parameter();
      parameter.setAddend1(4);
      parameter.setAddend2(5);
      spMapper.adderAsSelect(parameter);
      assertEquals((Integer) 9, parameter.getSum());

    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to call a stored procedure defined as <update> rather
   * then <select>. Of course, this only works if you are not returning a result
   * set.
   * 
   * This test shows using a multi-property parameter.
   */
  @Test
  public void testAdderAsUpdate() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      spMapper.adderAsUpdate(parameter);
      assertEquals((Integer) 5, parameter.getSum());

      parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);
      spMapper.adderAsUpdate(parameter);
      assertEquals((Integer) 5, parameter.getSum());

    } finally {
      sqlSession.close();
    }
  }

  // issue #145  
  @Test
  public void testEchoDate() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      HashMap<String, Object> parameter = new HashMap<String, Object>();
      Date now = new Date();
      parameter.put("input date", now);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      spMapper.echoDate(parameter);

      java.sql.Date outDate = new java.sql.Date(now.getTime());      
      assertEquals(outDate.toString(), parameter.get("output date").toString());
    } finally {
      sqlSession.close();
    }
  }
  
  /*
   * This test shows the use of a declared parameter map. We generally prefer
   * inline parameters, because the syntax is more intuitive (no pesky question
   * marks), but a parameter map will work.
   */
  @Test
  public void testAdderAsUpdateWithParameterMap() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("addend1", 3);
      parms.put("addend2", 4);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      spMapper.adderWithParameterMap(parms);
      assertEquals(7, parms.get("sum"));

      parms = new HashMap<String, Object>();
      parms.put("addend1", 2);
      parms.put("addend2", 3);
      spMapper.adderWithParameterMap(parms);
      assertEquals(5, parms.get("sum"));

    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use an input parameter and return a result set from
   * a stored procedure.
   * 
   * This test shows using a single value parameter.
   */
  @Test
  public void testCallWithResultSet1() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Name name = spMapper.getName(1);
      assertNotNull(name);
      assertEquals("Wilma", name.getFirstName());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a single value parameter.
   */
  @Test
  public void testCallWithResultSet2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 1);
      List<Name> names = spMapper.getNames(parms);
      assertEquals(3, names.size());
      assertEquals(3, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a Map parameter.
   */
  @Test
  public void testCallWithResultSet3() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNames(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<String, Object>();
      parms.put("lowestId", 3);
      names = spMapper.getNames(parms);
      assertEquals(1, names.size());
      assertEquals(1, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a Map parameter.
   */
  @Test
  public void testCallWithResultSet4() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNames(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      names = spMapper.getNames(parms);
      assertEquals(2, names.size());
      assertEquals(2, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use the ARRAY JDBC type with MyBatis.
   * 
   * @throws SQLException
   */
  @Test
  public void testGetNamesWithArray() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Array array = sqlSession.getConnection().createArrayOf("int", new Integer[] { 1, 2, 5 });

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("ids", array);
      List<Name> names = spMapper.getNamesWithArray(parms);
      Object[] returnedIds = (Object[]) parms.get("returnedIds");
      assertEquals(4, returnedIds.length);
      assertEquals(3, parms.get("requestedRows"));
      assertEquals(2, names.size());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to call procedures that return multiple result sets
   * 
   * @throws SQLException
   */
  @Test
  public void testGetNamesAndItems() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<List<?>> results = spMapper.getNamesAndItems();
      assertEquals(2, results.size());
      assertEquals(4, results.get(0).size());
      assertEquals(3, results.get(1).size());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use input and output parameters in a stored
   * procedure. This procedure does not return a result set.
   * 
   * This test shows using a multi-property parameter.
   * 
   * This test shows using annotations for stored procedures
   */
  @Test
  public void testAdderAsSelectAnnotated() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      spMapper.adderAsSelectAnnotated(parameter);

      assertEquals((Integer) 5, parameter.getSum());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use input and output parameters in a stored
   * procedure. This procedure does not return a result set.
   * 
   * This test shows using a multi-property parameter.
   * 
   * This test shows using annotations for stored procedures
   */
  @Test
  public void testAdderAsSelectDoubleCallAnnotated1() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      spMapper.adderAsSelectAnnotated(parameter);
      assertEquals((Integer) 5, parameter.getSum());

      parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);
      spMapper.adderAsSelectAnnotated(parameter);
      assertEquals((Integer) 5, parameter.getSum());

    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use input and output parameters in a stored
   * procedure. This procedure does not return a result set.
   * 
   * This test also demonstrates session level cache for output parameters.
   * 
   * This test shows using a multi-property parameter.
   * 
   * This test shows using annotations for stored procedures
   */
  @Test
  public void testAdderAsSelectDoubleCallAnnotated2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      spMapper.adderAsSelectAnnotated(parameter);
      assertEquals((Integer) 5, parameter.getSum());

      parameter = new Parameter();
      parameter.setAddend1(4);
      parameter.setAddend2(5);
      spMapper.adderAsSelectAnnotated(parameter);
      assertEquals((Integer) 9, parameter.getSum());

    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to call a stored procedure defined as <update> rather
   * then <select>. Of course, this only works if you are not returning a result
   * set.
   * 
   * This test shows using a multi-property parameter.
   * 
   * This test shows using annotations for stored procedures
   */
  @Test
  public void testAdderAsUpdateAnnotated() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      spMapper.adderAsUpdateAnnotated(parameter);
      assertEquals((Integer) 5, parameter.getSum());

      parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);
      spMapper.adderAsUpdateAnnotated(parameter);
      assertEquals((Integer) 5, parameter.getSum());

    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use an input parameter and return a result set from
   * a stored procedure.
   * 
   * This test shows using a single value parameter.
   * 
   * This test shows using annotations for stored procedures
   */
  @Test
  public void testCallWithResultSet1Annotated() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Name name = spMapper.getNameAnnotated(1);
      assertNotNull(name);
      assertEquals("Wilma", name.getFirstName());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use an input parameter and return a result set from
   * a stored procedure.
   * 
   * This test shows using a single value parameter.
   * 
   * This test shows using annotations for stored procedures and using a
   * resultMap in XML
   */
  @Test
  public void testCallWithResultSet1_a2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Name name = spMapper.getNameAnnotatedWithXMLResultMap(1);
      assertNotNull(name);
      assertEquals("Wilma", name.getFirstName());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a single value parameter.
   * 
   * This test shows using annotations for stored procedures
   */
  @Test
  public void testCallWithResultSet2_a1() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 1);
      List<Name> names = spMapper.getNamesAnnotated(parms);
      assertEquals(3, names.size());
      assertEquals(3, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a single value parameter.
   * 
   * This test shows using annotations for stored procedures and using a
   * resultMap in XML
   */
  @Test
  public void testCallWithResultSet2_a2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 1);
      List<Name> names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(3, names.size());
      assertEquals(3, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a Map parameter.
   * 
   * This test shows using annotations for stored procedures
   */
  @Test
  public void testCallWithResultSet3_a1() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNamesAnnotated(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<String, Object>();
      parms.put("lowestId", 3);
      names = spMapper.getNamesAnnotated(parms);
      assertEquals(1, names.size());
      assertEquals(1, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a Map parameter.
   * 
   * This test shows using annotations for stored procedures and using a
   * resultMap in XML
   */
  @Test
  public void testCallWithResultSet3_a2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<String, Object>();
      parms.put("lowestId", 3);
      names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(1, names.size());
      assertEquals(1, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a Map parameter.
   * 
   * This test shows using annotations for stored procedures
   */
  @Test
  public void testCallWithResultSet4_a1() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNamesAnnotated(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      names = spMapper.getNamesAnnotated(parms);
      assertEquals(2, names.size());
      assertEquals(2, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use a input and output parameters and return a
   * result set from a stored procedure.
   * 
   * This test shows using a Map parameter.
   * 
   * This test shows using annotations for stored procedures and using a
   * resultMap in XML
   */
  @Test
  public void testCallWithResultSet4_a2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<String, Object>();
      parms.put("lowestId", 2);
      names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(2, names.size());
      assertEquals(2, parms.get("totalRows"));
    } finally {
      sqlSession.close();
    }
  }

  /*
   * 
   * This test shows using a two named parameters.
   * 
   * This test shows using annotations for stored procedures and using a
   * resultMap in XML
   */
  @Test
  public void testCallLowHighWithResultSet() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      List<Name> names = spMapper.getNamesAnnotatedLowHighWithXMLResultMap(1, 1);
      assertEquals(1, names.size());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use the ARRAY JDBC type with MyBatis.
   * 
   * This test shows using annotations for stored procedures
   * 
   * @throws SQLException
   */
  @Test
  public void testGetNamesWithArray_a1() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Array array = sqlSession.getConnection().createArrayOf("int", new Integer[] { 1, 2, 5 });

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("ids", array);
      List<Name> names = spMapper.getNamesWithArrayAnnotated(parms);
      Object[] returnedIds = (Object[]) parms.get("returnedIds");
      assertEquals(4, returnedIds.length);
      assertEquals(3, parms.get("requestedRows"));
      assertEquals(2, names.size());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to use the ARRAY JDBC type with MyBatis.
   * 
   * This test shows using annotations for stored procedures and using a
   * resultMap in XML
   * 
   * @throws SQLException
   */
  @Test
  public void testGetNamesWithArray_a2() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Array array = sqlSession.getConnection().createArrayOf("int", new Integer[] { 1, 2, 5 });

      Map<String, Object> parms = new HashMap<String, Object>();
      parms.put("ids", array);
      List<Name> names = spMapper.getNamesWithArrayAnnotatedWithXMLResultMap(parms);
      Object[] returnedIds = (Object[]) parms.get("returnedIds");
      assertEquals(4, returnedIds.length);
      assertEquals(3, parms.get("requestedRows"));
      assertEquals(2, names.size());
    } finally {
      sqlSession.close();
    }
  }

  /*
   * This test shows how to call procedures that return multiple result sets
   * 
   * This test shows using annotations for stored procedures and referring to
   * multiple resultMaps in XML
   * 
   * @throws SQLException
   */
  @Test
  public void testGetNamesAndItems_a2() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<List<?>> results = spMapper.getNamesAndItemsAnnotatedWithXMLResultMap();
      assertEquals(2, results.size());
      assertEquals(4, results.get(0).size());
      assertEquals(3, results.get(1).size());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testGetNamesAndItems_a3() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<List<?>> results = spMapper.getNamesAndItemsAnnotatedWithXMLResultMapArray();
      assertEquals(2, results.size());
      assertEquals(4, results.get(0).size());
      assertEquals(3, results.get(1).size());
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void testGetNamesAndItemsLinked() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<Name> names = spMapper.getNamesAndItemsLinked();
      assertEquals(4, names.size());
      assertEquals(2, names.get(0).getItems().size());
      assertEquals(1, names.get(1).getItems().size());
      assertNull(names.get(2).getItems());
      assertNull(names.get(3).getItems());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testGetNamesAndItemsLinkedWithNoMatchingInfo() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<Name> names = spMapper.getNamesAndItemsLinkedById(0);
      assertEquals(1, names.size());
      assertEquals(2, names.get(0).getItems().size());
    } finally {
      sqlSession.close();
    }
  }

}
