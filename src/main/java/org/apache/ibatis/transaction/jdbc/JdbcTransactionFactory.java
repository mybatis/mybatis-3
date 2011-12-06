package org.apache.ibatis.transaction.jdbc;

import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

/**
 * Creates {@link JdbcTransaction} instances.
 *
 * @see JdbcTransaction
 */
public class JdbcTransactionFactory implements TransactionFactory {

  public void setProperties(Properties props) {
  }

  public Transaction newTransaction(Connection conn) {
    return new JdbcTransaction(conn);
  }

  public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
    return new JdbcTransaction(ds, level, autoCommit);
  }
}
