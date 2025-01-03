/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.submitted.handle_by_jdbc_type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HandlerByJdbcTypeTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/handle_by_jdbc_type/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/handle_by_jdbc_type/CreateDB.sql");
  }

  @Test
  void shouldResultMappingChooseTypeHandlerBasedOnJdbcType() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      BoolsBean bools = mapper.getBools(1);
      assertTrue(bools.isB1());
      assertTrue(bools.isB2());
    }
  }

  @Test
  void shouldUseDifferentTypeHandlerForDifferentProperty() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      BoolsBean bools = mapper.getBoolsSameColumnToDifferentProperty(1);
      assertTrue(bools.isB2());
      assertEquals('T', bools.getC());
    }
  }

  @Test
  void shouldUseDifferentTypeHandlerForDifferentColumn() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      BoolsBean bools = mapper.getBoolsDifferentColumnToSameProperty(1);
      assertTrue(bools.isB1());
    }
  }

  @Test
  void shouldParameterMappingChooseTypeHandlerBasedOnJdbcType() throws Exception {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      BoolsBean bools = new BoolsBean();
      bools.setId(2);
      bools.setB1(true);
      bools.setB2(true);
      mapper.insertBools(bools);
      sqlSession.commit();
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try (Statement stmt = sqlSession.getConnection().createStatement();
          ResultSet rs = stmt.executeQuery("select * from bools where id = 2")) {
        assertTrue(rs.next());
        assertEquals("1", rs.getString("B1"));
        assertEquals("T", rs.getString("B2"));
      }
    }
  }

}
