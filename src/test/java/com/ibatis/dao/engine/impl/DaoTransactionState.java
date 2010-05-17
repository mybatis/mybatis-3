package com.ibatis.dao.engine.impl;

public class DaoTransactionState {

  public static final DaoTransactionState ACTIVE = new DaoTransactionState();
  public static final DaoTransactionState INACTIVE = new DaoTransactionState();
  public static final DaoTransactionState COMMITTED = new DaoTransactionState();
  public static final DaoTransactionState ROLLEDBACK = new DaoTransactionState();

  private DaoTransactionState() {
  }

}
