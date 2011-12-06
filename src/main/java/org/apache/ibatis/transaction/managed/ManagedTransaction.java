package org.apache.ibatis.transaction.managed;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;

/**
 * {@link Transaction} that lets the container manage the full lifecycle of the transaction. 
 * Delays connection retrieval until getConnection() is called.
 * Ignores all commit or rollback requests.
 * By default, it closes the connection but can be configured not to do it.
 * 
 * @see ManagedTransactionFactory
 */
public class ManagedTransaction implements Transaction {

  private DataSource dataSource;
  private TransactionIsolationLevel level;
  private Connection connection;
  private boolean closeConnection;

  public ManagedTransaction(Connection connection, boolean closeConnection) {
    this.connection = connection;
    this.closeConnection = closeConnection;
  }
  
  public ManagedTransaction(DataSource ds, TransactionIsolationLevel level, boolean closeConnection) {
    this.dataSource = ds;
    this.level = level;
    this.closeConnection = closeConnection;
  }

  public Connection getConnection() throws SQLException {
    if (this.connection == null) {
      openConnection();
    }
    return this.connection;
  }

  public void commit() throws SQLException {
    // Does nothing
  }

  public void rollback() throws SQLException {
    // Does nothing
  }

  public void close() throws SQLException {
    if (this.closeConnection && this.connection != null) {
      this.connection.close();
    }
  }

  protected void openConnection() throws SQLException {
    this.connection = this.dataSource.getConnection();
    if (this.level != null) {
      this.connection.setTransactionIsolation(this.level.getLevel());
    }
  }
  
}
