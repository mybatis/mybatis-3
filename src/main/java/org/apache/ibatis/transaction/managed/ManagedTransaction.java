package org.apache.ibatis.transaction.managed;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.transaction.Transaction;

public class ManagedTransaction implements Transaction {

  private Connection connection;
  
  private boolean closeConnection;

  public ManagedTransaction(Connection connection, boolean closeConnection) {
    this.connection = connection;
    this.closeConnection = closeConnection;
  }

  public Connection getConnection() {
    return connection;
  }

  public void commit() throws SQLException {
    // Does nothing
  }

  public void rollback() throws SQLException {
    // Does nothing
  }

  public void close() throws SQLException {
    if (closeConnection) {
      connection.close();
    }
  }

}
