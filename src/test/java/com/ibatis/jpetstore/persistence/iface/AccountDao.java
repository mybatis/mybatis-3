package com.ibatis.jpetstore.persistence.iface;

import com.ibatis.jpetstore.domain.Account;

public interface AccountDao {

  Account getAccount(String username);

  Account getAccount(String username, String password);

  void insertAccount(Account account);

  void updateAccount(Account account);

}
