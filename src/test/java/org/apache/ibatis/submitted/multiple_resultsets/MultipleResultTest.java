/*
 *    Copyright 2009-2013 The MyBatis Team
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
import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * This class contains tests for multiple results.  
 * It is based on Jeff's ref cursor tests.
 * 
 * The tests require a
 * local install of PostgreSQL and cannot be run as a part of the normal
 * MyBatis build unless PostreSQL is setup on the build machine as 
 * described in setupdb.txt
 * 
 * If PostgreSQL is setup as described in setupdb.txt, then remove
 * the @Ignore annotation to enable the tests.
 *
 */
//@Ignore("See setupdb.txt for instructions on how to run the tests in this class")
public class MultipleResultTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multiple_resultsets/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();
  }

  @Test
  public void shouldGetAGraphOutOfMultipleRsWithNoFKs() throws IOException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Order order = mapper.getOrder(1);
      Assert.assertEquals(3, order.getDetailLines().size());
      Assert.assertEquals(order.getDetailLines().get(0).getItemDescription(), "Pen");
      Assert.assertEquals(order.getDetailLines().get(1).getItemDescription(), "Pencil");
      Assert.assertEquals(order.getDetailLines().get(2).getItemDescription(), "Notepad");
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldGetAGraphOutOfMultipleRsWithFKs() throws IOException {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Order> orders = mapper.getOrders();
      Assert.assertEquals(2, orders.size());
      Assert.assertEquals(3, orders.get(0).getDetailLines().size());
      Assert.assertEquals(orders.get(0).getDetailLines().get(0).getItemDescription(), "Pen");
      Assert.assertEquals(orders.get(0).getDetailLines().get(1).getItemDescription(), "Pencil");
      Assert.assertEquals(orders.get(0).getDetailLines().get(2).getItemDescription(), "Notepad");
      Assert.assertEquals(3, orders.get(1).getDetailLines().size());
      Assert.assertEquals(orders.get(1).getDetailLines().get(0).getItemDescription(), "Compass");
      Assert.assertEquals(orders.get(1).getDetailLines().get(1).getItemDescription(), "Protractor");
      Assert.assertEquals(orders.get(1).getDetailLines().get(2).getItemDescription(), "Pencil");
    } finally {
      sqlSession.close();
    }
  }

}
