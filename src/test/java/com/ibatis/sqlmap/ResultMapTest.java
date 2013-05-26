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
package com.ibatis.sqlmap;

import com.testdomain.Account;
import com.testdomain.Order;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ResultMapTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("com/scripts/account-init.sql");
    initScript("com/scripts/order-init.sql");
    initScript("com/scripts/line_item-init.sql");
  }

  protected void tearDown() throws Exception {
  }

  // RESULT MAP FEATURE TESTS

  public void testColumnsByName() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderLiteByColumnName", new Integer(1));
    assertOrder1(order);
  }

  public void testExtendedResultMap() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderLiteByColumnName", new Integer(1));
    assertOrder1(order);
  }

  public void testColumnsByIndex() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderLiteByColumnIndex", new Integer(1));
    assertOrder1(order);
  }

  public void testTypeSpecified() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderWithTypes", new Integer(1));
    assertOrder1(order);
  }

  public void testComplexObjectMapping() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderWithAccount", new Integer(1));
    assertOrder1(order);
    assertAccount1(order.getAccount());
  }

  public void testCollectionMappingAndExtends() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderWithLineItemsCollection", new Integer(1));

    assertOrder1(order);
    assertNotNull(order.getLineItems());
    assertEquals(2, order.getLineItems().size());
  }

  public void testListMapping() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderWithLineItems", new Integer(1));

    assertOrder1(order);
    assertNotNull(order.getLineItemsList());
    assertEquals(2, order.getLineItemsList().size());
  }

  public void testGetAllLineItemProps() throws SQLException {
    List list = sqlMap.queryForList("getAllLineItemProps", new Integer(1));

    assertNotNull(list);
    assertEquals(2, list.size());
  }

  public void testArrayMapping() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderWithLineItemArray", new Integer(1));

    assertOrder1(order);
    assertNotNull(order.getLineItemArray());
    assertEquals(2, order.getLineItemArray().length);
  }

  public void testHashMapMapping() throws SQLException {
    Map order = (Map) sqlMap.queryForObject("getOrderAsMap", new Integer(1));
    assertOrder1(order);
  }

  public void testNestedObjects() throws SQLException {
    Order order = (Order) sqlMap.queryForObject("getOrderJoinedFavourite", new Integer(1));
    assertOrder1(order);
  }

  public void testSimpleTypeMapping() throws SQLException {
    List list = sqlMap.queryForList("getAllCreditCardNumbersFromOrders", null);

    assertEquals(5, list.size());
    assertEquals("555555555555", list.get(0));
  }

  public void testCompositeKeyMapping() throws SQLException {

    Order order1 = (Order) sqlMap.queryForObject("getOrderWithFavouriteLineItem", new Integer(1));
    Order order2 = (Order) sqlMap.queryForObject("getOrderWithFavouriteLineItem", new Integer(2));

    assertNotNull(order1);
    assertNotNull(order1.getFavouriteLineItem());
    assertEquals(2, order1.getFavouriteLineItem().getId());
    assertEquals(1, order1.getFavouriteLineItem().getOrderId());

    assertNotNull(order2);
    assertNotNull(order2.getFavouriteLineItem());
    assertEquals(1, order2.getFavouriteLineItem().getId());
    assertEquals(2, order2.getFavouriteLineItem().getOrderId());


  }

  public void testDynCompositeKeyMapping() throws SQLException {

    Order order1 = (Order) sqlMap.queryForObject("getOrderWithDynFavouriteLineItem", new Integer(1));

    assertNotNull(order1);
    assertNotNull(order1.getFavouriteLineItem());
    assertEquals(2, order1.getFavouriteLineItem().getId());
    assertEquals(1, order1.getFavouriteLineItem().getOrderId());

  }

  public void testGetDoubleNestedResult() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getNestedAccountViaColumnName", new Integer(1));
    assertAccount1(account);
  }


}
