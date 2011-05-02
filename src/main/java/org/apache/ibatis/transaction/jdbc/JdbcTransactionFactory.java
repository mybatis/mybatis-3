package org.apache.ibatis.transaction.jdbc;

import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

import java.sql.Connection;
import java.util.Properties;

public class JdbcTransactionFactory implements TransactionFactory {
  private Properties properties;

  public void setProperties(Properties props) {
    this.properties = props;
  }

  public Transaction newTransaction(Connection conn, boolean autoCommit) {
    if (properties != null && properties.containsKey("allowAutoCommit")) {
      return new JdbcTransaction(conn, autoCommit,
              Boolean.valueOf(properties.getProperty("allowAutoCommit")));
    } else {
      return new JdbcTransaction(conn, autoCommit);
    }
  }

}
