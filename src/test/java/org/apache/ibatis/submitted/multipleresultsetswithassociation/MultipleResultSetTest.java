/*
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
package org.apache.ibatis.submitted.multipleresultsetswithassociation;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/*
 * This class contains tests for multiple result sets with an association mapping.
 * This test is based on the org.apache.ibatis.submitted.multiple_resultsets test.
 *
 */
class MultipleResultSetTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/multipleresultsetswithassociation/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    // Could not get the table creation, procedure creation, and data population to work from the same script.
    // Once it was in three scripts, all seemed well.
    try (SqlSession session = sqlSessionFactory.openSession();
         Connection conn = session.getConnection()) {
      try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multipleresultsetswithassociation/CreateDB1.sql")) {
        runReaderScript(conn, reader);
      }
      try (Reader reader = Resources
          .getResourceAsReader("org/apache/ibatis/submitted/multipleresultsetswithassociation/CreateDB2.sql")) {
        runReaderScript(conn, reader);
      }
      try (Reader reader = Resources
          .getResourceAsReader("org/apache/ibatis/submitted/multipleresultsetswithassociation/CreateDB3.sql")) {
        runReaderScript(conn, reader);
      }
    }
  }

  private static void runReaderScript(Connection conn, Reader reader) {
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.setSendFullScript(true);
    runner.setAutoCommit(true);
    runner.setStopOnError(false);
    runner.runScript(reader);
  }

  @Test
  void shouldGetOrderDetailsEachHavingAnOrderHeader() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<OrderDetail> orderDetails = mapper.getOrderDetailsWithHeaders();

      // There are six order detail records in the database
      // As long as the data does not change this should be successful
      Assertions.assertEquals(6, orderDetails.size());

      // Each order detail should have a corresponding OrderHeader
      // Only 2 of 6 orderDetails have orderHeaders
      for (OrderDetail orderDetail : orderDetails) {
        Assertions.assertNotNull(orderDetail.getOrderHeader());
      }
    }
  }

  @Test
  void shouldGetOrderDetailsEachHavingAnOrderHeaderAnnotationBased() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<OrderDetail> orderDetails = mapper.getOrderDetailsWithHeadersAnnotationBased();

      // There are six order detail records in the database
      // As long as the data does not change this should be successful
      Assertions.assertEquals(6, orderDetails.size());

      // Each order detail should have a corresponding OrderHeader
      // Only 2 of 6 orderDetails have orderHeaders
      for (OrderDetail orderDetail : orderDetails) {
        Assertions.assertNotNull(orderDetail.getOrderHeader());
      }
    }
  }

}
