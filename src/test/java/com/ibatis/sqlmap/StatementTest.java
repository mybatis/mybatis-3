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

import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.client.event.RowHandler;
import com.ibatis.sqlmap.engine.impl.SqlMapClientImpl;
import com.testdomain.Account;
import com.testdomain.LineItem;
import com.testdomain.Order;
import com.testdomain.SuperAccount;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatementTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("com/scripts/account-init.sql");
    initScript("com/scripts/order-init.sql");
    initScript("com/scripts/line_item-init.sql");
  }

  protected void tearDown() throws Exception {
  }

  // OBJECT QUERY TESTS

  public void testExecuteQueryForObjectViaColumnName() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));
    assertAccount1(account);
  }

  public void testUserConnection() throws SQLException {
    DataSource ds = sqlMap.getDataSource();
    Connection conn = ds.getConnection();
    ((SqlMapClientImpl) sqlMap).getTransactionManager().getTrasactionConfig().setDataSource(null);
    sqlMap.setUserConnection(conn);
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));
    conn.close();
    assertAccount1(account);
    ((SqlMapClientImpl) sqlMap).getTransactionManager().getTrasactionConfig().setDataSource(ds);
  }

  public void testSessionUserConnection() throws SQLException {
    DataSource ds = sqlMap.getDataSource();
    Connection conn = ds.getConnection();
    ((SqlMapClientImpl) sqlMap).getTransactionManager().getTrasactionConfig().setDataSource(null);
    SqlMapSession session = sqlMap.openSession(conn);
    Account account = (Account) session.queryForObject("getAccountViaColumnName", new Integer(1));
    session.close();
    conn.close();
    assertAccount1(account);
    ((SqlMapClientImpl) sqlMap).getTransactionManager().getTrasactionConfig().setDataSource(ds);
  }

  public void testSessionUserConnectionFailures() throws SQLException {
    DataSource ds = sqlMap.getDataSource();
    Connection conn = ds.getConnection();
    ((SqlMapClientImpl) sqlMap).getTransactionManager().getTrasactionConfig().setDataSource(null);
    try {
      SqlMapSession session = sqlMap.openSession(conn);

      Exception expected = null;
      try {
        session.startTransaction();
      } catch (Exception e) {
        expected = e;
      }
      assertNotNull("Expected exception from startTransaction() was not detected.", expected);
      expected = null;

      Account account = (Account) session.queryForObject("getAccountViaColumnName", new Integer(1));
      session.close();
      conn.close();
      assertAccount1(account);
    } finally {
      ((SqlMapClientImpl) sqlMap).getTransactionManager().getTrasactionConfig().setDataSource(ds);
    }
  }

  public void testExecuteQueryForObjectViaColumnIndex() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnIndex", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectViaResultClass() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaResultClass", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectViaResultClassIgnoreCaseTypeAliasCase() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaResultClassIgnoreCaseTypeAlias", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectViaResultClassPlusOne() throws SQLException {
    List list = sqlMap.queryForList("getAccountViaResultClassPlusOne", new Integer(1));
    assertList(list);
  }

  public void testExecuteQueryForObjectAsHashMap() throws SQLException {
    Map account = (HashMap) sqlMap.queryForObject("getAccountAsHashMap", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectAsHashMapResultClass() throws SQLException {
    Map account = (HashMap) sqlMap.queryForObject("getAccountAsHashMapResultClass", new Integer(1));
    assertAccount1(account);
  }

  public void testExecuteQueryForObjectWithSimpleResultClass() throws SQLException {
    String email = (String) sqlMap.queryForObject("getEmailAddressViaResultClass", new Integer(1));
    assertEquals("clinton.begin@ibatis.com", email);
  }

  public void testExecuteQueryForObjectWithSimpleResultMap() throws SQLException {
    String email = (String) sqlMap.queryForObject("getEmailAddressViaResultMap", new Integer(1));
    assertEquals("clinton.begin@ibatis.com", email);
  }

  public void testExecuteQueryForObjectWithResultObject() throws SQLException {
    Account account = new Account();
    Account testAccount = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1), account);
    assertAccount1(account);
    assertTrue(account == testAccount);
  }

  public void testGetSubclass() throws SQLException {
    SuperAccount account = new SuperAccount();
    account.setId(1);
    account = (SuperAccount) sqlMap.queryForObject("getSuperAccount", account);
    assertAccount1(account);
  }

  // LIST QUERY TESTS

  public void testExecuteQueryForListWithResultMap() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsViaResultMap", null);

    assertAccount1((Account) list.get(0));
    assertEquals(5, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());

  }

  public void testExecuteQueryWithCustomTypeHandler() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsViaCustomTypeHandler", null);

    assertAccount1((Account) list.get(0));
    assertEquals(5, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());

    assertFalse(((Account) list.get(0)).isCartOption());
    assertFalse(((Account) list.get(1)).isCartOption());
    assertTrue(((Account) list.get(2)).isCartOption());
    assertTrue(((Account) list.get(3)).isCartOption());
    assertTrue(((Account) list.get(4)).isCartOption());

    assertTrue(((Account) list.get(0)).isBannerOption());
    assertTrue(((Account) list.get(1)).isBannerOption());
    assertFalse(((Account) list.get(2)).isBannerOption());
    assertFalse(((Account) list.get(3)).isBannerOption());
    assertTrue(((Account) list.get(4)).isBannerOption());
  }

  /*
   * for bug 976614 - bug squashed 07-14-04 By Brandon Goodin
   */
  /*
  public void testBrokenExecuteQueryForListWithResultMap() throws SQLException {
    List list = sqlMap.queryForList("getBrokenAllAccountsViaResultMap", null);
    
    assertAccount1((Account) list.get(0));
    assertEquals(5, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());
  }
  */
  public void testExecuteQueryForPaginatedList() throws SQLException {

    // Get List of all 5
    PaginatedList list = sqlMap.queryForPaginatedList("getAllAccountsViaResultMap", null, 2);

    // Test initial state (page 0)
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test illegal previous page (no effect, state should be same)
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test next (page 1)
    list.nextPage();
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test next (page 2 -last)
    list.nextPage();
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());
    assertEquals(5, ((Account) list.get(0)).getId());

    // Test previous (page 1)
    list.previousPage();
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test previous (page 0 -first)
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test goto (page 0)
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test goto (page 1)
    list.gotoPage(1);
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test goto (page 2)
    list.gotoPage(2);
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());
    assertEquals(5, ((Account) list.get(0)).getId());

    // Test illegal goto (page 0)
    list.gotoPage(3);
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    list = sqlMap.queryForPaginatedList("getNoAccountsViaResultMap", null, 2);

    // Test empty list
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test next
    list.nextPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test previous
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test previous
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    list = sqlMap.queryForPaginatedList("getFewAccountsViaResultMap", null, 2);

    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test next
    list.nextPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test previous
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test previous
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test Even - Two Pages
    try {
      initScript("com/scripts/more-account-records.sql");
    } catch (Exception e) {
      fail(e.toString());
    }

    list = sqlMap.queryForPaginatedList("getAllAccountsViaResultMap", null, 5);

    assertEquals(5, list.size());

    list.nextPage();
    assertEquals(5, list.size());


    list.isPreviousPageAvailable();
    list.previousPage();
    assertEquals(5, list.size());


  }

  public void testExecuteQueryForPaginatedList2() throws SQLException {
    // tests methods that don't require a parameter object

    // Get List of all 5
    PaginatedList list = sqlMap.queryForPaginatedList("getAllAccountsViaResultMap", 2);

    // Test initial state (page 0)
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test illegal previous page (no effect, state should be same)
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test next (page 1)
    list.nextPage();
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test next (page 2 -last)
    list.nextPage();
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());
    assertEquals(5, ((Account) list.get(0)).getId());

    // Test previous (page 1)
    list.previousPage();
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test previous (page 0 -first)
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertAccount1((Account) list.get(0));
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test goto (page 0)
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());

    // Test goto (page 1)
    list.gotoPage(1);
    assertTrue(list.isPreviousPageAvailable());
    assertTrue(list.isNextPageAvailable());
    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());

    // Test goto (page 2)
    list.gotoPage(2);
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());
    assertEquals(5, ((Account) list.get(0)).getId());

    // Test illegal goto (page 0)
    list.gotoPage(3);
    assertTrue(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    list = sqlMap.queryForPaginatedList("getNoAccountsViaResultMap", 2);

    // Test empty list
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test next
    list.nextPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test previous
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    // Test previous
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(0, list.size());

    list = sqlMap.queryForPaginatedList("getFewAccountsViaResultMap", 2);

    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test next
    list.nextPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test previous
    list.previousPage();
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test previous
    list.gotoPage(0);
    assertFalse(list.isPreviousPageAvailable());
    assertFalse(list.isNextPageAvailable());
    assertEquals(1, list.size());

    // Test Even - Two Pages
    try {
      initScript("com/scripts/more-account-records.sql");
    } catch (Exception e) {
      fail(e.toString());
    }

    list = sqlMap.queryForPaginatedList("getAllAccountsViaResultMap", 5);

    assertEquals(5, list.size());

    list.nextPage();
    assertEquals(5, list.size());


    list.isPreviousPageAvailable();
    list.previousPage();
    assertEquals(5, list.size());


  }

  public void testExecuteQueryForListWithResultMapWithDynamicElement() throws SQLException {

    List list = sqlMap.queryForList("getAllAccountsViaResultMapWithDynamicElement", "LIKE");

    assertAccount1((Account) list.get(0));
    assertEquals(3, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(4, ((Account) list.get(2)).getId());

    list = sqlMap.queryForList("getAllAccountsViaResultMapWithDynamicElement", "=");

    assertEquals(0, list.size());

  }

  public void testExecuteQueryForListResultClass() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsViaResultClass", null);

    assertAccount1((Account) list.get(0));
    assertEquals(5, list.size());
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());
  }

  public void testExecuteQueryForListWithHashMapResultMap() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsAsHashMapViaResultMap", null);

    assertAccount1((Map) list.get(0));
    assertEquals(5, list.size());
    assertEquals(new Integer(1), ((Map) list.get(0)).get("id"));
    assertEquals(new Integer(2), ((Map) list.get(1)).get("id"));
    assertEquals(new Integer(3), ((Map) list.get(2)).get("id"));
    assertEquals(new Integer(4), ((Map) list.get(3)).get("id"));
    assertEquals(new Integer(5), ((Map) list.get(4)).get("id"));
  }

  public void testExecuteQueryForListWithHashMapResultClass() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsAsHashMapViaResultClass", null);

    assertAccount1((Map) list.get(0));
    assertEquals(5, list.size());
    assertEquals(new Integer(1), ((Map) list.get(0)).get("ID"));
    assertEquals(new Integer(2), ((Map) list.get(1)).get("ID"));
    assertEquals(new Integer(3), ((Map) list.get(2)).get("ID"));
    assertEquals(new Integer(4), ((Map) list.get(3)).get("ID"));
    assertEquals(new Integer(5), ((Map) list.get(4)).get("ID"));
  }

  public void testExecuteQueryForListWithSimpleResultClass() throws SQLException {
    List list = sqlMap.queryForList("getAllEmailAddressesViaResultClass", null);

    assertEquals("clinton.begin@ibatis.com", list.get(0));
    assertEquals(5, list.size());
  }

  public void testExecuteQueryForListWithSimpleResultMap() throws SQLException {
    List list = sqlMap.queryForList("getAllEmailAddressesViaResultMap", null);

    assertEquals("clinton.begin@ibatis.com", list.get(0));
    assertEquals(5, list.size());
  }

  public void testExecuteQueryForListWithSkipAndMax() throws SQLException {
    List list = sqlMap.queryForList("getAllAccountsViaResultMap", null, 2, 2);

    assertEquals(2, list.size());
    assertEquals(3, ((Account) list.get(0)).getId());
    assertEquals(4, ((Account) list.get(1)).getId());
  }

  public void testExecuteQueryForListWithRowHandler() throws SQLException {
    TestRowHandler handler = new TestRowHandler();
    sqlMap.queryWithRowHandler("getAllAccountsViaResultMap", null, handler);
    List list = handler.getList();
    assertEquals(5, handler.getIndex());
    assertEquals(5, list.size());
    assertAccount1((Account) list.get(0));
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());

  }

  public void testExecuteQueryForListWithRowHandler2() throws SQLException {
    // tests method that does not require a parameter object
    TestRowHandler handler = new TestRowHandler();
    sqlMap.queryWithRowHandler("getAllAccountsViaResultMap", handler);
    List list = handler.getList();
    assertEquals(5, handler.getIndex());
    assertEquals(5, list.size());
    assertAccount1((Account) list.get(0));
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());

  }

  public void testLegacyExecuteQueryForListWithRowHandler() throws SQLException {
    TestRowHandler handler = new TestRowHandler();
    sqlMap.queryWithRowHandler("getAllAccountsViaResultMap", null, handler);
    assertEquals(5, handler.getIndex());
    /*
    assertEquals(5, list.size());
    assertAccount1((Account) list.get(0));
    assertEquals(1, ((Account) list.get(0)).getId());
    assertEquals(2, ((Account) list.get(1)).getId());
    assertEquals(3, ((Account) list.get(2)).getId());
    assertEquals(4, ((Account) list.get(3)).getId());
    assertEquals(5, ((Account) list.get(4)).getId());
    */
  }

  // MAP TESTS

  public void testExecuteQueryForMap() throws SQLException {
    Map map = sqlMap.queryForMap("getAllAccountsViaResultClass", null, "lastName");

    assertAccount1((Account) map.get("Begin"));
    assertEquals(5, map.size());
    assertEquals(1, ((Account) map.get("Begin")).getId());
    assertEquals(2, ((Account) map.get("Smith")).getId());
    assertEquals(3, ((Account) map.get("Jones")).getId());
    assertEquals(4, ((Account) map.get("Jackson")).getId());
    assertEquals(5, ((Account) map.get("Goodman")).getId());
  }

  public void testExecuteQueryForMapWithValueProperty() throws SQLException {
    Map map = sqlMap.queryForMap("getAllAccountsViaResultClass", null, "lastName", "firstName");

    assertEquals(5, map.size());
    assertEquals("Clinton", map.get("Begin"));
    assertEquals("Jim", map.get("Smith"));
    assertEquals("Elizabeth", map.get("Jones"));
    assertEquals("Bob", map.get("Jackson"));
    assertEquals("&manda", map.get("Goodman"));
  }

  // UPDATE TESTS

  public void testInsertGeneratedKey() throws SQLException {
    LineItem item = new LineItem();

    item.setId(10);
    item.setItemCode("blah");
    item.setOrderId(333);
    item.setPrice(new BigDecimal("44.00"));
    item.setQuantity(1);

    Object key = sqlMap.insert("insertLineItem", item);

    assertEquals(new Integer(99), key);
    assertEquals(99, item.getId());

    Map param = new HashMap();
    param.put("orderId", new Integer(333));
    param.put("lineId", new Integer(10));
    LineItem testItem = (LineItem) sqlMap.queryForObject("getSpecificLineItem", param);
    assertNotNull(testItem);
    assertEquals(10, testItem.getId());
  }

  public void testInsertGeneratedKeyFailure() throws SQLException {
    LineItem item = new LineItem();

    item.setId(0);
    item.setItemCode("blah");
    item.setOrderId(333);
    item.setPrice(new BigDecimal("44.00"));
    item.setQuantity(1);

    Object key = new Integer(-1);

    try {
      key = sqlMap.insert("insertLineItemOrDie", item);
    } catch (SQLException e) {
      // this is expected
    }

    assertEquals(key, new Integer(-1)); // this should not be changed from above
    assertEquals(0, item.getId()); // this should not be changed from above

  }

  public void testInsertPreKey() throws SQLException {
    LineItem item = new LineItem();

    item.setId(10);
    item.setItemCode("blah");
    item.setOrderId(333);
    item.setPrice(new BigDecimal("44.00"));
    item.setQuantity(1);

    Object key = sqlMap.insert("insertLineItemPreKey", item);

    assertEquals(new Integer(99), key);
    assertEquals(99, item.getId());

    Map param = new HashMap();
    param.put("orderId", new Integer(333));
    param.put("lineId", new Integer(99));
    LineItem testItem = (LineItem) sqlMap.queryForObject("getSpecificLineItem", param);
    assertNotNull(testItem);
    assertEquals(99, testItem.getId());

  }

  public void testInsertNoKey() throws SQLException {
    LineItem item = new LineItem();

    item.setId(100);
    item.setItemCode("blah");
    item.setOrderId(333);
    item.setPrice(new BigDecimal("44.00"));
    item.setQuantity(1);

    Object key = sqlMap.insert("insertLineItemNoKey", item);

    assertNull(null, key);
    assertEquals(100, item.getId());

    Map param = new HashMap();
    param.put("orderId", new Integer(333));
    param.put("lineId", new Integer(100));
    LineItem testItem = (LineItem) sqlMap.queryForObject("getSpecificLineItem", param);
    assertNotNull(testItem);
    assertEquals(100, testItem.getId());

  }

  public void testExecuteUpdateWithParameterMap() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));

    account.setId(6);
    account.setEmailAddress("new.clinton@ibatis.com");
    account.setBannerOption(true);
    account.setCartOption(true);
    sqlMap.update("insertAccountViaParameterMap", account);

    account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(6));

    assertEquals(true, account.isBannerOption());
    assertEquals(true, account.isCartOption());
    assertEquals("new.clinton@ibatis.com", account.getEmailAddress());

  }

  public void testExecuteUpdateWithInlineParameters() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));

    account.setEmailAddress("new.clinton@ibatis.com");
    try {
      sqlMap.startTransaction();
      sqlMap.update("updateAccountViaInlineParameters", account);
      sqlMap.commitTransaction();
    } finally {
      sqlMap.endTransaction();
    }
    account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(1));

    assertEquals("new.clinton@ibatis.com", account.getEmailAddress());

  }

  public void testExecuteUpdateWithParameterClass() throws SQLException {
    Account account = new Account();
    account.setId(5);

    boolean checkForInvalidTypeFailedAppropriately = false;
    try {
      sqlMap.update("deleteAccount", new Object());
    } catch (SQLException e) {
      checkForInvalidTypeFailedAppropriately = true;
    }

    sqlMap.update("deleteAccount", account);

    account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(5));

    assertNull(account);
    assertTrue(checkForInvalidTypeFailedAppropriately);
  }

  /*
   * For bug 959140
   */
//  public void testExecuteUpdateWithDuplicateParams() throws SQLException {
//
//    sqlMap.update("deleteAccountByDuplicateInteger", new Integer (5));
//
//    Account account = (Account) sqlMap.queryForObject("getAccountViaColumnName", new Integer(5));
//
//    assertNull(account);
//  }

  // DYNAMIC SQL
  public void testQueryDynamicSqlElement() throws SQLException {
    List list = sqlMap.queryForList("getDynamicOrderedEmailAddressesViaResultMap", "ACC_ID");

    assertEquals("clinton.begin@ibatis.com", (String) list.get(0));

    list = sqlMap.queryForList("getDynamicOrderedEmailAddressesViaResultMap", "ACC_FIRST_NAME");

    assertNull(list.get(0));

  }

  // INNER CLASSES

  public class TestRowHandler implements RowHandler {
    private int index = 0;

    private List list = new ArrayList();

    public void handleRow(Object object) {
      index++;
      assertEquals(index, ((Account) object).getId());
      list.add(object);
    }

    public void handleRow(Object valueObject, List list) {
      index++;
      assertEquals(index, ((Account) valueObject).getId());
      list.add(valueObject);
    }

    public int getIndex() {
      return index;
    }

    public List getList() {
      return list;
    }

  }

  public void testNestedResultMaps() throws SQLException {
    List list = sqlMap.queryForList("getAllOrdersWithNestedResultMaps");

    assertEquals(10, list.size());

    Order order = (Order) list.get(0);
    assertEquals(1, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(1);
    assertEquals(4, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(2);
    assertEquals(3, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(3);
    assertEquals(2, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(4);
    assertEquals(5, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(5);
    assertEquals(5, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(6);
    assertEquals(4, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(7);
    assertEquals(3, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(8);
    assertEquals(2, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());

    order = (Order) list.get(9);
    assertEquals(1, order.getAccount().getId());
    assertEquals(2, order.getLineItems().size());
  }
}
