package com.ibatis.sqlmap.engine.transaction.external;

import com.ibatis.sqlmap.engine.transaction.BaseTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import org.apache.ibatis.session.Configuration;

import java.sql.SQLException;
import java.util.Properties;

public class ExternalTransactionConfig extends BaseTransactionConfig {

  private boolean defaultAutoCommit = false;
  private boolean setAutoCommitAllowed = true;

  public Transaction newTransaction(Configuration configuration, int transactionIsolation) throws SQLException, TransactionException {
    return new ExternalTransaction(configuration, dataSource, defaultAutoCommit, setAutoCommitAllowed, transactionIsolation);
  }

  public void setProperties(Properties props) throws SQLException, TransactionException {
    String dacProp = props.getProperty("DefaultAutoCommit");
    String sacaProp = props.getProperty("SetAutoCommitAllowed");
    defaultAutoCommit = "true".equals(dacProp);
    setAutoCommitAllowed = "true".equals(sacaProp) || sacaProp == null;
  }

}
