/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.sptests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Reader;
import java.sql.Array;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SPTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void initDatabase() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/sptests/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    ScriptRunner runner = new ScriptRunner(
        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection());
    runner.setDelimiter("go");
    runner.setLogWriter(null);
    runner.setErrorLogWriter(null);
    BaseDataTest.runScript(runner, "org/apache/ibatis/submitted/sptests/CreateDB.sql");
  }

  /**
   * This test shows how to use input and output parameters in a stored procedure. This procedure does not return a
   * result set.
   * <p>
   * This test shows using a multi-property parameter.
   */
  @Test
  void adderAsSelect() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      spMapper.adderAsSelect(parameter);

      assertEquals((Integer) 5, parameter.getSum());
    }
  }

  /**
   * This test shows how to use input and output parameters in a stored procedure. This procedure does not return a
   * result set.
   * <p>
   * This test shows using a multi-property parameter.
   */
  @Test
  void adderAsSelectDoubleCall1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
    }
  }

  /**
   * This test shows how to use input and output parameters in a stored procedure. This procedure does not return a
   * result set. This test also demonstrates session level cache for output parameters.
   * <p>
   * This test shows using a multi-property parameter.
   */
  @Test
  void adderAsSelectDoubleCall2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
    }
  }

  /**
   * This test shows how to call a stored procedure defined as <update> rather then <select>. Of course, this only works
   * if you are not returning a result set.
   * <p>
   * This test shows using a multi-property parameter.
   */
  @Test
  void adderAsUpdate() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
    }
  }

  // issue #145
  @Test
  void echoDate() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      HashMap<String, Object> parameter = new HashMap<>();
      Date now = new Date();
      parameter.put("input date", now);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      spMapper.echoDate(parameter);

      java.sql.Date outDate = new java.sql.Date(now.getTime());
      assertEquals(outDate.toString(), parameter.get("output date").toString());
    }
  }

  /**
   * This test shows the use of a declared parameter map. We generally prefer inline parameters, because the syntax is
   * more intuitive (no pesky question marks), but a parameter map will work.
   */
  @Test
  void adderAsUpdateWithParameterMap() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Map<String, Object> parms = new HashMap<>();
      parms.put("addend1", 3);
      parms.put("addend2", 4);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      spMapper.adderWithParameterMap(parms);
      assertEquals(7, parms.get("sum"));

      parms = new HashMap<>();
      parms.put("addend1", 2);
      parms.put("addend2", 3);
      spMapper.adderWithParameterMap(parms);
      assertEquals(5, parms.get("sum"));
    }
  }

  /**
   * This test shows how to use an input parameter and return a result set from a stored procedure.
   * <p>
   * This test shows using a single value parameter.
   */
  @Test
  void callWithResultSet1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Name name = spMapper.getName(1);
      assertNotNull(name);
      assertEquals("Wilma", name.getFirstName());
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a single value parameter.
   */
  @Test
  void callWithResultSet2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 1);
      List<Name> names = spMapper.getNames(parms);
      assertEquals(3, names.size());
      assertEquals(3, parms.get("totalRows"));
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a Map parameter.
   */
  @Test
  void callWithResultSet3() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNames(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<>();
      parms.put("lowestId", 3);
      names = spMapper.getNames(parms);
      assertEquals(1, names.size());
      assertEquals(1, parms.get("totalRows"));
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a Map parameter.
   */
  @Test
  void callWithResultSet4() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNames(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<>();
      parms.put("lowestId", 2);
      names = spMapper.getNames(parms);
      assertEquals(2, names.size());
      assertEquals(2, parms.get("totalRows"));
    }
  }

  /**
   * This test shows how to use the ARRAY JDBC type with MyBatis.
   *
   * @throws SQLException
   */
  @Test
  void getNamesWithArray() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Array array = sqlSession.getConnection().createArrayOf("int", new Integer[] { 1, 2, 5 });

      Map<String, Object> parms = new HashMap<>();
      parms.put("ids", array);
      List<Name> names = spMapper.getNamesWithArray(parms);
      Object[] returnedIds = (Object[]) parms.get("returnedIds");
      assertEquals(4, returnedIds.length);
      assertEquals(3, parms.get("requestedRows"));
      assertEquals(2, names.size());
    }
  }

  /**
   * This test shows how to call procedures that return multiple result sets
   *
   * @throws SQLException
   */
  @Test
  void getNamesAndItems() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<List<?>> results = spMapper.getNamesAndItems();
      assertEquals(2, results.size());
      assertEquals(4, results.get(0).size());
      assertEquals(3, results.get(1).size());
    }
  }

  /**
   * This test shows how to use input and output parameters in a stored procedure. This procedure does not return a
   * result set. This test shows using a multi-property parameter.
   * <p>
   * This test shows using annotations for stored procedures.
   */
  @Test
  void adderAsSelectAnnotated() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Parameter parameter = new Parameter();
      parameter.setAddend1(2);
      parameter.setAddend2(3);

      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      spMapper.adderAsSelectAnnotated(parameter);

      assertEquals((Integer) 5, parameter.getSum());
    }
  }

  /**
   * This test shows how to use input and output parameters in a stored procedure. This procedure does not return a
   * result set. This test shows using a multi-property parameter.
   * <p>
   * This test shows using annotations for stored procedures.
   */
  @Test
  void adderAsSelectDoubleCallAnnotated1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
    }
  }

  /**
   * This test shows how to use input and output parameters in a stored procedure. This procedure does not return a
   * result set.
   * <p>
   * This test also demonstrates session level cache for output parameters.
   * <p>
   * This test shows using a multi-property parameter.
   * <p>
   * This test shows using annotations for stored procedures.
   */
  @Test
  void adderAsSelectDoubleCallAnnotated2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
    }
  }

  /**
   * This test shows how to call a stored procedure defined as <update> rather then <select>. Of course, this only works
   * if you are not returning a result set.
   * <p>
   * This test shows using a multi-property parameter.
   * <p>
   * This test shows using annotations for stored procedures.
   */
  @Test
  void adderAsUpdateAnnotated() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
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
    }
  }

  /**
   * This test shows how to use an input parameter and return a result set from a stored procedure.
   * <p>
   * This test shows using a single value parameter.
   * <p>
   * This test shows using annotations for stored procedures.
   */
  @Test
  void callWithResultSet1Annotated() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Name name = spMapper.getNameAnnotated(1);
      assertNotNull(name);
      assertEquals("Wilma", name.getFirstName());
    }
  }

  /**
   * This test shows how to use an input parameter and return a result set from a stored procedure.
   * <p>
   * This test shows using a single value parameter.
   * <p>
   * This test shows using annotations for stored procedures and using a resultMap in XML.
   */
  @Test
  void callWithResultSet1A2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Name name = spMapper.getNameAnnotatedWithXMLResultMap(1);
      assertNotNull(name);
      assertEquals("Wilma", name.getFirstName());
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a single value parameter.
   * <p>
   * This test shows using annotations for stored procedures.
   */
  @Test
  void callWithResultSet2A1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 1);
      List<Name> names = spMapper.getNamesAnnotated(parms);
      assertEquals(3, names.size());
      assertEquals(3, parms.get("totalRows"));
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a single value parameter.
   * <p>
   * This test shows using annotations for stored procedures and using a resultMap in XML.
   */
  @Test
  void callWithResultSet2A2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 1);
      List<Name> names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(3, names.size());
      assertEquals(3, parms.get("totalRows"));
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a Map parameter.
   * <p>
   * This test shows using annotations for stored procedures.
   */
  @Test
  void callWithResultSet3A1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNamesAnnotated(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<>();
      parms.put("lowestId", 3);
      names = spMapper.getNamesAnnotated(parms);
      assertEquals(1, names.size());
      assertEquals(1, parms.get("totalRows"));
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a Map parameter.
   * <p>
   * This test shows using annotations for stored procedures and using a resultMap in XML.
   */
  @Test
  void callWithResultSet3A2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<>();
      parms.put("lowestId", 3);
      names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(1, names.size());
      assertEquals(1, parms.get("totalRows"));
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a Map parameter.
   * <p>
   * This test shows using annotations for stored procedures.
   */
  @Test
  void callWithResultSet4A1() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNamesAnnotated(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<>();
      parms.put("lowestId", 2);
      names = spMapper.getNamesAnnotated(parms);
      assertEquals(2, names.size());
      assertEquals(2, parms.get("totalRows"));
    }
  }

  /**
   * This test shows how to use an input and output parameters and return a result set from a stored procedure.
   * <p>
   * This test shows using a Map parameter.
   * <p>
   * This test shows using annotations for stored procedures and using a resultMap in XML.
   */
  @Test
  void callWithResultSet4A2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Map<String, Object> parms = new HashMap<>();
      parms.put("lowestId", 2);
      List<Name> names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(2, parms.get("totalRows"));
      assertEquals(2, names.size());

      parms = new HashMap<>();
      parms.put("lowestId", 2);
      names = spMapper.getNamesAnnotatedWithXMLResultMap(parms);
      assertEquals(2, names.size());
      assertEquals(2, parms.get("totalRows"));
    }
  }

  /**
   * This test shows using a two named parameters.
   * <p>
   * This test shows using annotations for stored procedures and using a resultMap in XML
   */
  @Test
  void callLowHighWithResultSet() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      List<Name> names = spMapper.getNamesAnnotatedLowHighWithXMLResultMap(1, 1);
      assertEquals(1, names.size());
    }
  }

  /**
   * This test shows how to use the ARRAY JDBC type with MyBatis.
   * <p>
   * This test shows using annotations for stored procedures.
   *
   * @throws SQLException
   */
  @Test
  void getNamesWithArrayA1() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Array array = sqlSession.getConnection().createArrayOf("int", new Integer[] { 1, 2, 5 });

      Map<String, Object> parms = new HashMap<>();
      parms.put("ids", array);
      List<Name> names = spMapper.getNamesWithArrayAnnotated(parms);
      Object[] returnedIds = (Object[]) parms.get("returnedIds");
      assertEquals(4, returnedIds.length);
      assertEquals(3, parms.get("requestedRows"));
      assertEquals(2, names.size());
    }
  }

  /**
   * This test shows how to use the ARRAY JDBC type with MyBatis.
   * <p>
   * This test shows using annotations for stored procedures and using a resultMap in XML.
   *
   * @throws SQLException
   */
  @Test
  void getNamesWithArrayA2() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      Array array = sqlSession.getConnection().createArrayOf("int", new Integer[] { 1, 2, 5 });

      Map<String, Object> parms = new HashMap<>();
      parms.put("ids", array);
      List<Name> names = spMapper.getNamesWithArrayAnnotatedWithXMLResultMap(parms);
      Object[] returnedIds = (Object[]) parms.get("returnedIds");
      assertEquals(4, returnedIds.length);
      assertEquals(3, parms.get("requestedRows"));
      assertEquals(2, names.size());
    }
  }

  /**
   * This test shows how to call procedures that return multiple result sets.
   * <p>
   * This test shows using annotations for stored procedures and referring to multiple resultMaps in XML.
   *
   * @throws SQLException
   */
  @Test
  void getNamesAndItemsA2() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<List<?>> results = spMapper.getNamesAndItemsAnnotatedWithXMLResultMap();
      assertEquals(2, results.size());
      assertEquals(4, results.get(0).size());
      assertEquals(3, results.get(1).size());
    }
  }

  @Test
  void getNamesAndItemsA3() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<List<?>> results = spMapper.getNamesAndItemsAnnotatedWithXMLResultMapArray();
      assertEquals(2, results.size());
      assertEquals(4, results.get(0).size());
      assertEquals(3, results.get(1).size());
    }
  }

  @Test
  void getNamesAndItemsLinked() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<Name> names = spMapper.getNamesAndItemsLinked();
      assertEquals(4, names.size());
      assertEquals(2, names.get(0).getItems().size());
      assertEquals(1, names.get(1).getItems().size());
      assertNull(names.get(2).getItems());
      assertNull(names.get(3).getItems());
    }
  }

  @Test
  void getNamesAndItemsLinkedWithNoMatchingInfo() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);

      List<Name> names = spMapper.getNamesAndItemsLinkedById(0);
      assertEquals(1, names.size());
      assertEquals(2, names.get(0).getItems().size());
    }
  }

  @Test
  void multipleForeignKeys() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
      List<Book> books = spMapper.getBookAndGenre();
      assertEquals("Book1", books.get(0).getName());
      assertEquals("Genre1", books.get(0).getGenre().getName());
      assertEquals("Book2", books.get(1).getName());
      assertEquals("Genre2", books.get(1).getGenre().getName());
      assertEquals("Book3", books.get(2).getName());
      assertEquals("Genre1", books.get(2).getGenre().getName());
    }
  }
}
