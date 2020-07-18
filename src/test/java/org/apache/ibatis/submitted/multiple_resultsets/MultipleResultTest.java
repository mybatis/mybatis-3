/**
 *    Copyright 2009-2020 the original author or authors.
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
package org.apache.ibatis.submitted.multiple_resultsets;

import java.io.IOException;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.testcontainers.PgContainer;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/*
 * This class contains tests for multiple results.
 * It is based on Jeff's ref cursor tests.
 */
@Tag("TestcontainersTests")
class MultipleResultTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
        PgContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.setMapUnderscoreToCamelCase(true);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/multiple_resultsets/CreateDB.sql");
  }

  @Test
  void shouldGetMultipleResultSetsWithOneStatement() throws IOException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<List<?>> results = mapper.getUsersAndGroups();
      Assertions.assertEquals(2, results.size());

      Assertions.assertEquals(6, results.get(0).size());
      OrderDetail detail = (OrderDetail) results.get(0).get(0);
      Assertions.assertEquals(1, detail.getOrderId());
      Assertions.assertEquals(1, detail.getLineNumber());
      Assertions.assertEquals(1, detail.getQuantity());
      Assertions.assertEquals("Pen", detail.getItemDescription());

      Assertions.assertEquals(2, results.get(1).size());
      OrderHeader header = (OrderHeader) results.get(1).get(0);
      Assertions.assertEquals(1, header.getOrderId());
      Assertions.assertEquals("Fred", header.getCustName());
    }
  }

  @Test
  void shouldSkipNullResultSet() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        Mapper mapper = sqlSession.getMapper(Mapper.class);
        List<List<?>> results = mapper.multiResultsWithUpdate();
        Assertions.assertEquals(2, results.size());

        Assertions.assertEquals(6, results.get(0).size());
        OrderDetail detail = (OrderDetail) results.get(0).get(0);
        Assertions.assertEquals(1, detail.getOrderId());
        Assertions.assertEquals(1, detail.getLineNumber());
        Assertions.assertEquals(1, detail.getQuantity());
        Assertions.assertEquals("Pen", detail.getItemDescription());

        Assertions.assertEquals(2, results.get(1).size());
        OrderHeader header = (OrderHeader) results.get(1).get(0);
        Assertions.assertEquals(1, header.getOrderId());
        Assertions.assertEquals("Fred", header.getCustName());

        results = mapper.getUsersAndGroups();
        Assertions.assertEquals(7, results.get(0).size());
        detail = (OrderDetail) results.get(0).get(6);
        Assertions.assertEquals(2, detail.getOrderId());
        Assertions.assertEquals(4, detail.getLineNumber());
        Assertions.assertEquals(5, detail.getQuantity());
        Assertions.assertEquals("Eraser", detail.getItemDescription());
      } finally {
        sqlSession.rollback(true);
      }
    }
  }
}
