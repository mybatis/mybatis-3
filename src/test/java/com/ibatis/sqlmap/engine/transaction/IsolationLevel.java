package com.ibatis.sqlmap.engine.transaction;

import java.sql.Connection;
import java.sql.SQLException;

public class IsolationLevel {

  public static final int UNSET_ISOLATION_LEVEL = -9999;

  private int isolationLevel = UNSET_ISOLATION_LEVEL;
  private int originalIsolationLevel = UNSET_ISOLATION_LEVEL;

  public void setIsolationLevel(int isolationLevel) {
    this.isolationLevel = isolationLevel;
  }

  public void applyIsolationLevel(Connection conn) throws SQLException {
    if (isolationLevel != UNSET_ISOLATION_LEVEL) {
      originalIsolationLevel = conn.getTransactionIsolation();
      if (isolationLevel != originalIsolationLevel) {
        conn.setTransactionIsolation(isolationLevel);
      }
    }
  }

  public void restoreIsolationLevel(Connection conn) throws SQLException {
    if (isolationLevel != originalIsolationLevel) {
      conn.setTransactionIsolation(originalIsolationLevel);
    }
  }

}
