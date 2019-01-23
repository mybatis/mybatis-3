/**
 *    Copyright 2009-2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class DynSqlTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader configReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/dynsql/MapperConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/dynsql/CreateDB.sql");
  }

  @Test
  void testSelect() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Integer> ids = new ArrayList<>();
      ids.add(1);
      ids.add(3);
      ids.add(5);
      Parameter parameter = new Parameter();
      parameter.setEnabled(true);
      parameter.setSchema("ibtest");
      parameter.setIds(ids);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.select", parameter);

      assertEquals(3, answer.size());
    }
  }

  @Test
  void testSelectSimple() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Integer> ids = new ArrayList<>();
      ids.add(1);
      ids.add(3);
      ids.add(5);
      Parameter parameter = new Parameter();
      parameter.setEnabled(true);
      parameter.setSchema("ibtest");
      parameter.setIds(ids);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.select_simple", parameter);

      assertEquals(3, answer.size());
    }
  }

  @Test
  void testSelectLike() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.selectLike", "Ba");

      assertEquals(2, answer.size());
      assertEquals(4, answer.get(0).get("ID"));
      assertEquals(6, answer.get(1).get("ID"));
    }
  }

  @Test
  void testNumerics() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<NumericRow> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.selectNumerics");

      assertEquals(1, answer.size());

      NumericRow row = answer.get(0);
      assertEquals(1, (int) row.getId());
      assertEquals(2, (int) row.getTinynumber());
      assertEquals(3, (int) row.getSmallnumber());
      assertEquals(4L, (long) row.getLonginteger());
      assertEquals(new BigInteger("5"), row.getBiginteger());
      assertEquals(new BigDecimal("6.00"), row.getNumericnumber());
      assertEquals(new BigDecimal("7.00"), row.getDecimalnumber());
      assertEquals((Float) 8.0f, row.getRealnumber());
      assertEquals((Float) 9.0f, row.getFloatnumber());
      assertEquals((Double) 10.0, row.getDoublenumber());
    }
  }

  @Test
  void testOgnlStaticMethodCall() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.ognlStaticMethodCall", "Rock 'n Roll");
      assertEquals(1, answer.size());
      assertEquals(7, answer.get(0).get("ID"));
    }
  }

  @Test
  void testBindNull() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      DynSqlMapper mapper = sqlSession.getMapper(DynSqlMapper.class);
      String description = mapper.selectDescription(null);
      assertEquals("Pebbles", description);
    }
  }

}
