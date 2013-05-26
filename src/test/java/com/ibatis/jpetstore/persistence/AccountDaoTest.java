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
