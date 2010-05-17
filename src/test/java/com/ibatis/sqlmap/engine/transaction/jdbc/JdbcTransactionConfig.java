package com.ibatis.sqlmap.engine.transaction.jdbc;

import com.ibatis.sqlmap.engine.transaction.BaseTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.Transaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import org.apache.ibatis.session.Configuration;

import java.sql.SQLException;
import java.util.Properties;

public class JdbcTransactionConfig extends BaseTransactionConfig {

  public Transaction newTransaction(Configuration configuration, int transactionIsolation) throws SQLException, TransactionException {
    return new JdbcTransaction(configuration, dataSource, transactionIsolation);
  }

  public void setProperties(Properties props) throws SQLException, TransactionException {
  }

}
