package com.ibatis.dao.engine.transaction.external;

import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;

import java.util.Properties;

public class ExternalDaoTransactionManager implements DaoTransactionManager {

  public void configure(Properties properties) {
    // Do nothing
  }

  public DaoTransaction startTransaction() {
    return new ExternalDaoTransaction();
  }

  public void commitTransaction(DaoTransaction trans) {
    ((ExternalDaoTransaction) trans).commit();
  }

  public void rollbackTransaction(DaoTransaction trans) {
    ((ExternalDaoTransaction) trans).rollback();
  }

}
