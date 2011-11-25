package org.apache.ibatis.transaction.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionException;

public class JdbcTransaction implements Transaction {

  private static final Log log = LogFactory.getLog(JdbcTransaction.class);

  protected Connection connection;
  protected DataSource dataSource;
  protected TransactionIsolationLevel level;
  protected boolean autoCommmit;
  
  public JdbcTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
    this.dataSource = ds;
    this.level = level;
    this.autoCommmit = autoCommit;
  }

  public JdbcTransaction(Connection connection) {    
    this.connection = connection;
  }

  public Connection getConnection() throws SQLException {
    if (this.connection == null) {
      openConnection();
    }
    return this.connection;
  }

  public void commit() throws SQLException {
    if (this.connection != null && !this.connection.getAutoCommit()) {
      this.connection.commit();
    }
  }

  public void rollback() throws SQLException {
    if (this.connection != null && !this.connection.getAutoCommit()) {
      this.connection.rollback();
    }
  }

  public void close() throws SQLException {
    if (this.connection != null) {
      resetAutoCommit();
      this.connection.close();
    }
  }
  
  protected void setDesiredAutoCommit(boolean desiredAutoCommit) {
    try {
      if (this.connection.getAutoCommit() != desiredAutoCommit) {
        this.connection.setAutoCommit(desiredAutoCommit);
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
      if (!this.connection.getAutoCommit()) {
        // MyBatis does not call commit/rollback on a connection if just selects were performed.
        // Some databases start transactions with select statements 
        // and they mandate a commit/rollback before closing the connection. 
        // A workaround is setting the autocommit to true before closing the connection.
        this.connection.setAutoCommit(true);
      }
    } catch (SQLException e) {
      log.debug("Error resetting autocommit to true " +
          "before closing the connection.  Cause: " + e);
    }
  }

  protected void openConnection() throws SQLException {
    this.connection = this.dataSource.getConnection();
    if (this.level != null) {
      this.connection.setTransactionIsolation(level.getLevel());
    }
    setDesiredAutoCommit(this.autoCommmit);
  }
    
}
