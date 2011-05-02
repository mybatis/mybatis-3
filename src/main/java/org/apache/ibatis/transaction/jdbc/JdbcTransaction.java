package org.apache.ibatis.transaction.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionException;

public class JdbcTransaction implements Transaction {

  private static final Log log = LogFactory.getLog(JdbcTransaction.class);

  protected Connection connection;

  public JdbcTransaction(Connection connection, boolean desiredAutoCommit, boolean allowAutoCommit) {
    this.connection = connection;
    if (allowAutoCommit) {
        setDesiredAutoCommit(desiredAutoCommit);
    }
  }

  public JdbcTransaction(Connection connection, boolean desiredAutoCommit) {
    this(connection, desiredAutoCommit, true);
  }
  
  public Connection getConnection() {
    return connection;
  }

  public void commit() throws SQLException {
    if (!connection.getAutoCommit()) {
      connection.commit();
    }
  }

  public void rollback() throws SQLException {
    if (!connection.getAutoCommit()) {
      connection.rollback();
    }
  }

  public void close() throws SQLException {
    resetAutoCommit();
    connection.close();
  }

  protected void setDesiredAutoCommit(boolean desiredAutoCommit) {
    try {
      if (connection.getAutoCommit() != desiredAutoCommit) {
        connection.setAutoCommit(desiredAutoCommit);
      }
    } catch (SQLException e) {
      // Only a very poorly implemented driver would fail here,
      // and there's not much we can do about that.
      throw new TransactionException("Error configuring AutoCommit.  " +
          "Your driver may not support getAutoCommit() or setAutoCommit(). " +
          "Requested setting: " + desiredAutoCommit + ".  Cause: " + e, e);
    }
  }

  protected void resetAutoCommit() {
    try {
      if (!connection.getAutoCommit()) {
        // MyBatis does not call commit/rollback on a connection if just selects were performed.
        // Some databases start transactions with select statements 
        // and they mandate a commit/rollback before closing the connection. 
        // A workaround is setting the autocommit to true before closing the connection.
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.debug("Error resetting autocommit to true " +
          "before closing the connection.  Cause: " + e);
    }
  }

}
