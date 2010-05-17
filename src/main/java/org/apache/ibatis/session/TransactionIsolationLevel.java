package org.apache.ibatis.session;

import java.sql.Connection;

public enum TransactionIsolationLevel {
  NONE(Connection.TRANSACTION_NONE),
  READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
  READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
  REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
  SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

  private final int level;

  private TransactionIsolationLevel(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }
}
