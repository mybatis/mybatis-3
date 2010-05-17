package org.apache.ibatis.transaction.jdbc;

import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionException;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransaction implements Transaction {

  protected Connection connection;

  public JdbcTransaction(Connection connection, boolean desiredAutoCommit) {
    this.connection = connection;
    setDesiredAutoCommit(desiredAutoCommit);
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
        // for compatibility we always use true, as some drivers don't like being left in "false" mode.
        connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      // Only a very poorly implemented driver would fail here,
      // and there's not much we can do about that.
      throw new TransactionException("Error configuring AutoCommit.  " +
          "Your driver may not support getAutoCommit() or setAutoCommit(). Cause: " + e, e);
    }
  }

}
