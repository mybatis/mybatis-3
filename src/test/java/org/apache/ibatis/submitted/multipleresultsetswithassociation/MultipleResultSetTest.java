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
package org.apache.ibatis.submitted.multipleresultsetswithassociation;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * This class contains tests for multiple result sets with an association mapping.
 * This test is based on the org.apache.ibatis.submitted.multiple_resultsets test.
 * 
 */
public class MultipleResultSetTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multipleresultsetswithassociation/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();
    
    // populate in-memory database
    // Could not get the table creation, procedure creation, and data population to work from the same script.
    // Once it was in three scripts, all seemed well.
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multipleresultsetswithassociation/CreateDB1.sql");
    runReaderScript(conn, session, reader);
    reader.close();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multipleresultsetswithassociation/CreateDB2.sql");
    runReaderScript(conn, session, reader);
    reader.close();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multipleresultsetswithassociation/CreateDB3.sql");
    runReaderScript(conn, session, reader);
    reader.close();
    conn.close();
    session.close();
  }
  
  private static void runReaderScript(Connection conn, SqlSession session, Reader reader) throws Exception {
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.setSendFullScript(true);
    runner.setAutoCommit(true);
    runner.setStopOnError(false);
    runner.runScript(reader);
  }

  @Test
  public void shouldGetOrderDetailsEachHavingAnOrderHeader() throws IOException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<OrderDetail> orderDetails = mapper.getOrderDetailsWithHeaders();
      
      // There are six order detail records in the database
      // As long as the data does not change this should be successful
      Assert.assertEquals(6, orderDetails.size());
      
      // Each order detail should have a corresponding OrderHeader
      // Only 2 of 6 orderDetails have orderHeaders
      for(OrderDetail orderDetail : orderDetails){
          Assert.assertNotNull(orderDetail.getOrderHeader());
      }
      
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetOrderDetailsEachHavingAnOrderHeaderAnnotationBased() throws IOException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<OrderDetail> orderDetails = mapper.getOrderDetailsWithHeadersAnnotationBased();

      // There are six order detail records in the database
      // As long as the data does not change this should be successful
      Assert.assertEquals(6, orderDetails.size());

      // Each order detail should have a corresponding OrderHeader
      // Only 2 of 6 orderDetails have orderHeaders
      for(OrderDetail orderDetail : orderDetails){
          Assert.assertNotNull(orderDetail.getOrderHeader());
      }

    } finally {
      sqlSession.close();
    }
  }

}
