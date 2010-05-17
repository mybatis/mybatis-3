package com.ibatis.jpetstore.persistence.sqlmapdao;

import com.ibatis.dao.client.DaoManager;
import com.ibatis.jpetstore.domain.Account;
import com.ibatis.jpetstore.persistence.iface.AccountDao;

public class AccountSqlMapDao extends BaseSqlMapDao implements AccountDao {

  public AccountSqlMapDao(DaoManager daoManager) {
    super(daoManager);
  }

  public Account getAccount(String username) {
    return (Account) queryForObject("getAccountByUsername", username);
  }

  public Account getAccount(String username, String password) {
    Account account = new Account();
    account.setUsername(username);
    account.setPassword(password);
    return (Account) queryForObject("getAccountByUsernameAndPassword", account);
  }

  public void insertAccount(Account account) {
    update("insertAccount", account);
    update("insertProfile", account);
    update("insertSignon", account);
  }

  public void updateAccount(Account account) {
    update("updateAccount", account);
    update("updateProfile", account);

    if (account.getPassword() != null && account.getPassword().length() > 0) {
      update("updateSignon", account);
    }
  }


}
