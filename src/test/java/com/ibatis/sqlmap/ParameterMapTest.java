package com.ibatis.sqlmap;

import com.testdomain.Account;

import java.sql.SQLException;

public class ParameterMapTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("com/scripts/account-init.sql");
  }

  protected void tearDown() throws Exception {
  }

  // PARAMETER MAP FEATURE TESTS

  public void testSpecifiedType() throws SQLException {
    Account account = newAccount6();

    sqlMap.update("insertAccountNullableEmail", account);

    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));

    assertAccount6(account);
  }

  public void testUnknownParameterClass() throws SQLException {
    Account account = newAccount6();

    sqlMap.update("insertAccountUknownParameterClass", account);

    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));

    assertAccount6(account);
  }

  public void testNullParameter() throws SQLException {

    Account account = (Account) sqlMap.queryForObject("getAccountNullParameter", null);


    assertNull(account);
  }

  public void testNullParameter2() throws SQLException {

    Account account = (Account) sqlMap.queryForObject("getAccountNullParameter");


    assertNull(account);
  }
}
