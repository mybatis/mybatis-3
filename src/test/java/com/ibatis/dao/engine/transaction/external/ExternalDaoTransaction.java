package com.ibatis.dao.engine.transaction.external;

import com.ibatis.dao.client.DaoTransaction;

public class ExternalDaoTransaction implements DaoTransaction {

  public void commit() {
    // Do nothing
  }

  public void rollback() {
    // Do nothing
  }

}
