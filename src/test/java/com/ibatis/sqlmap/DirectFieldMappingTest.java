package com.ibatis.sqlmap;

import com.testdomain.FieldAccount;
import com.testdomain.PrivateAccount;

import java.sql.SQLException;

public class DirectFieldMappingTest extends BaseSqlMapTest {

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("com/scripts/account-init.sql");
  }

  public void testInsertAndSelectDirectToFields() throws SQLException {
    FieldAccount account = newFieldAccount6();

    sqlMap.insert("insertAccountFromFields", account);

    account = (FieldAccount) sqlMap.queryForObject("getAccountToFields", new Integer(6));

    assertFieldAccount6(account);
    assertFieldAccount6(account.account());
  }

  public void testGetAccountWithPrivateConstructor() throws SQLException {
    FieldAccount account = newFieldAccount6();

    sqlMap.insert("insertAccountFromFields", account);

    PrivateAccount pvt = (PrivateAccount) sqlMap.queryForObject("getAccountWithPrivateConstructor", new Integer(6));

    assertPrivateAccount6(pvt);
  }


}
