package com.ibatis.sqlmap.engine.transaction;

import org.apache.ibatis.session.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

public interface TransactionConfig {

  Transaction newTransaction(Configuration configuration, int transactionIsolation)
      throws SQLException, TransactionException;

  DataSource getDataSource();

  void setDataSource(DataSource ds);

  boolean isForceCommit();

  void setForceCommit(boolean forceCommit);

  void setProperties(Properties props)
      throws SQLException, TransactionException;

}
