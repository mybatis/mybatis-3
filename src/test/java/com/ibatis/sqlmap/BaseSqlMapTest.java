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

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import junit.framework.TestCase;
import com.testdomain.Account;
import com.testdomain.FieldAccount;
import com.testdomain.Order;
import com.testdomain.PrivateAccount;

import javax.sql.DataSource;
import java.io.Reader;
import java.sql.Connection;
import java.util.*;

public class BaseSqlMapTest extends TestCase {

  protected static SqlMapClient sqlMap;

  protected static void initSqlMap(String configFile, Properties props) throws Exception {
    Reader reader = Resources.getResourceAsReader(configFile);
    sqlMap = SqlMapClientBuilder.buildSqlMapClient(reader, props);
    reader.close();
  }

  protected static void initScript(String script) throws Exception {
    DataSource ds = sqlMap.getDataSource();

    Connection conn = ds.getConnection();

    Reader reader = Resources.getResourceAsReader(script);

    ScriptRunner runner = new ScriptRunner(conn, false, false);
    runner.setLogWriter(null);
    runner.setErrorLogWriter(null);

    runner.runScript(reader);
    conn.commit();
    conn.close();
    reader.close();
  }

  protected Account newAccount6() {
    Account account = new Account();
    account.setId(6);
    account.setFirstName("Jennifer");
    account.setLastName("Begin");
    account.setEmailAddress("no_email@provided.com");
    account.setBannerOption(true);
    account.setCartOption(true);
    return account;
  }

  protected FieldAccount newFieldAccount6() {
    FieldAccount account = new FieldAccount();
    account.id(6);
    account.firstName("Jennifer");
    account.lastName("Begin");
    account.emailAddress("no_email@provided.com");
    return account;
  }

  protected void assertAccount1(Account account) {
    assertNotNull(account);
    assertEquals(1, account.getId());
    assertEquals("Clinton", account.getFirstName());
    assertEquals("Begin", account.getLastName());
    assertEquals("clinton.begin@ibatis.com", account.getEmailAddress());
  }

  protected void assertAccount2(Account account) {
    assertNotNull(account);
    assertEquals(2, account.getId());
    assertEquals("Jim", account.getFirstName());
    assertEquals("Smith", account.getLastName());
    assertEquals(account.getEmailAddress(), "jim.smith@somewhere.com");
  }

  protected void assertList(List list) {
    assertEquals(2, list.size());
  }

  protected void assertAccount6(Account account) {
    assertNotNull(account);
    assertEquals(6, account.getId());
    assertEquals("Jennifer", account.getFirstName());
    assertEquals("Begin", account.getLastName());
    assertEquals("no_email@provided.com", account.getEmailAddress());
  }

  protected void assertPrivateAccount6(PrivateAccount account) {
    assertNotNull(account);
    assertEquals(6, account.getId());
    assertEquals("Jennifer", account.getFirstName());
    assertEquals("Begin", account.getLastName());
    assertEquals("no_email@provided.com", account.getEmailAddress());
  }

  protected void assertFieldAccount6(FieldAccount account) {
    assertNotNull(account);
    assertEquals(6, account.id());
    assertEquals("Jennifer", account.firstName());
    assertEquals("Begin", account.lastName());
    assertEquals("no_email@provided.com", account.emailAddress());
  }

  protected void assertAccount1(Map account) {
    Integer id = (Integer) account.get("id");
    String firstName = (String) account.get("firstName");
    String lastName = (String) account.get("lastName");
    String emailAddress = (String) account.get("emailAddress");

    if (id == null) {
      id = (Integer) account.get("ID");
      firstName = (String) account.get("FIRSTNAME");
      lastName = (String) account.get("LASTNAME");
      emailAddress = (String) account.get("EMAILADDRESS");
    }

    assertEquals(new Integer(1), id);
    assertEquals("Clinton", firstName);
    assertEquals("Begin", lastName);
    assertEquals("clinton.begin@ibatis.com", emailAddress);
  }

  protected void assertOrder1(Order order) {
    Calendar cal = new GregorianCalendar(2003, 1, 15, 8, 15, 00);

    assertEquals(1, order.getId());
    assertEquals(cal.getTime().getTime(), order.getDate().getTime());
    assertEquals("VISA", order.getCardType());
    assertEquals("999999999999", order.getCardNumber());
    assertEquals("05/03", order.getCardExpiry());
    assertEquals("11 This Street", order.getStreet());
    assertEquals("Victoria", order.getCity());
    assertEquals("BC", order.getProvince());
    assertEquals("C4B 4F4", order.getPostalCode());
  }

  protected void assertOrder1(Map order) {
    Calendar cal = new GregorianCalendar(2003, 1, 15, 8, 15, 00);

    assertEquals(new Integer(1), order.get("id"));
    assertEquals(cal.getTime().getTime(), ((Date) order.get("date")).getTime());
    assertEquals("VISA", order.get("cardType"));
    assertEquals("999999999999", order.get("cardNumber"));
    assertEquals("05/03", order.get("cardExpiry"));
    assertEquals("11 This Street", order.get("street"));
    assertEquals("Victoria", order.get("city"));
    assertEquals("BC", order.get("province"));
    assertEquals("C4B 4F4", order.get("postalCode"));
  }

  public void testDummy() {
    // just to avoid warnings when running all tests.
  }

}
