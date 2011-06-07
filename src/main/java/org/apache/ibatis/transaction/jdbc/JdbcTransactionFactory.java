package org.apache.ibatis.transaction.jdbc;

import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import java.sql.Connection;
import java.util.Properties;

public class JdbcTransactionFactory implements TransactionFactory {

  public void setProperties(Properties props) {
  }

  public Transaction newTransaction(Connection conn, boolean autoCommit) {
    return new JdbcTransaction(conn, autoCommit);
  }
}
