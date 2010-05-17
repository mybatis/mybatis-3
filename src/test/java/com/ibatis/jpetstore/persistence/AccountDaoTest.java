package com.ibatis.jpetstore.persistence;

import com.ibatis.jpetstore.domain.Account;
import com.ibatis.jpetstore.domain.DomainFixture;
import com.ibatis.jpetstore.persistence.iface.AccountDao;

public class AccountDaoTest extends BasePersistenceTest {

  private AccountDao acctDao = (AccountDao) daoMgr.getDao(AccountDao.class);

  public void testShouldFindDefaultUserAccountByUsername() throws Exception {
    Account acct = acctDao.getAccount("j2ee");
    assertNotNull(acct);
  }

  public void testShouldFindDefaultUserAccountByUsernameAndPassword() throws Exception {
    Account acct = acctDao.getAccount("j2ee", "j2ee");
    assertNotNull(acct);
  }

  public void testShouldInsertNewAccount() throws Exception {
    Account acct = DomainFixture.newTestAccount();
    acctDao.insertAccount(acct);
    acct = acctDao.getAccount("cbegin");
    assertNotNull(acct);
  }

  public void testShouldUpdateAccountEmailAddress() throws Exception {
    String newEmail = "new@email.com";
    Account acct = acctDao.getAccount("j2ee");
    acct.setEmail(newEmail);
    acctDao.updateAccount(acct);
    acct = acctDao.getAccount("j2ee");
    assertNotNull(acct);
    assertEquals(newEmail, acct.getEmail());
  }

}
