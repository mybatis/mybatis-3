package org.apache.ibatis.transaction.managed;

import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import java.sql.Connection;
import java.util.Properties;

public class ManagedTransactionFactory implements TransactionFactory {

  private boolean closeConnection = true;

  public void setProperties(Properties props) {
    if (props != null) {
      String closeConnectionProperty = props.getProperty("closeConnection");
      if (closeConnectionProperty != null) {
        closeConnection = Boolean.valueOf(closeConnectionProperty);
      }
    }
  }

  public Transaction newTransaction(Connection conn, boolean autoCommit) {
    // Silently ignores autocommit, as managed transactions are entirely
    // controlled by an external manager.  It's silently ignored so that
    // code remains portable between managed and unmanaged configurations.
    return new ManagedTransaction(conn, closeConnection);
  }
}
