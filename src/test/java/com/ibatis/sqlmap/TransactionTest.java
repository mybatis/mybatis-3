package com.ibatis.sqlmap;


import com.ibatis.sqlmap.engine.transaction.TransactionException;
import com.testdomain.Account;

import java.sql.SQLException;

public class TransactionTest extends BaseSqlMapTest {

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("com/scripts/account-init.sql");
  }

  protected void tearDown() throws Exception {
  }

  // TRANSACTION TESTS

  public void testStartCommitTransaction() throws SQLException {
    Account account = newAccount6();
    try {
      sqlMap.startTransaction();
      sqlMap.insert("insertAccountViaParameterMap", account);
      sqlMap.commitTransaction();
    } finally {
      sqlMap.endTransaction();
    }

    // This will use autocommit...
    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));

    assertAccount6(account);
  }


  public void testTransactionAlreadyStarted() throws SQLException {
    Account account = newAccount6();
    boolean exceptionThrownAsExpected = false;

    try {
      sqlMap.startTransaction();
      sqlMap.insert("insertAccountViaParameterMap", account);

      try {
        sqlMap.startTransaction(); // transaction already started
      } catch (TransactionException e) {
        exceptionThrownAsExpected = true;
      }

      sqlMap.commitTransaction();
    } finally {
      sqlMap.endTransaction();
    }

    // This will use autocommit...
    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));
    assertAccount6(account);
    assertTrue(exceptionThrownAsExpected);
  }

  public void testNoTransactionStarted() throws SQLException {
    Account account = newAccount6();

    sqlMap.insert("insertAccountViaParameterMap", account);

    boolean exceptionThrownAsExpected = false;
    try {
      sqlMap.commitTransaction(); // No transaction started
    } catch (TransactionException e) {
      exceptionThrownAsExpected = true;
    }

    // This will use autocommit...
    assertTrue(exceptionThrownAsExpected);
    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));
    assertAccount6(account);
  }

  public void testTransactionFailed() throws SQLException {
    Account account = newAccount6();


    boolean exceptionThrownAsExpected = false;
    try {
      sqlMap.insert("insertAccountViaParameterMap", null);
    } catch (Exception e) {
      exceptionThrownAsExpected = true;
    }

    sqlMap.update("insertAccountViaParameterMap", account);

    // This will use autocommit...
    assertTrue(exceptionThrownAsExpected);
    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));
    assertAccount6(account);
  }

  public void testTransactionFailed2() throws SQLException {
    // testes method that does not require a parameter object
    Account account = newAccount6();


    boolean exceptionThrownAsExpected = false;
    try {
      sqlMap.insert("insertAccountViaParameterMap");
    } catch (Exception e) {
      exceptionThrownAsExpected = true;
    }

    sqlMap.update("insertAccountViaParameterMap", account);

    // This will use autocommit...
    assertTrue(exceptionThrownAsExpected);
    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));
    assertAccount6(account);
  }

  public void testStartRollbackTransaction() throws SQLException {
    Account account = newAccount6();

    try {
      sqlMap.startTransaction();
      sqlMap.update("insertAccountViaParameterMap", account);
      //sqlMap.commitTransaction();
    } finally {
      sqlMap.endTransaction();
    }

    // This will use autocommit...
    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));
    assertNull(account);
  }

  // AUTOCOMMIT TESTS

  public void testAutoCommitUpdate() throws SQLException {
    Account account = newAccount6();
    sqlMap.insert("insertAccountViaParameterMap", account);
    account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(6));
    assertAccount6(account);
  }

  public void testAutoCommitQuery() throws SQLException {
    Account account = (Account) sqlMap.queryForObject("getAccountNullableEmail", new Integer(1));
    assertAccount1(account);
  }


}
