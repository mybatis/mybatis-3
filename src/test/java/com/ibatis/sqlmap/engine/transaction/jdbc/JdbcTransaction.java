package com.ibatis.sqlmap.engine.transaction.jdbc;

import com.ibatis.sqlmap.engine.transaction.BaseTransaction;
import com.ibatis.sqlmap.engine.transaction.IsolationLevel;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransaction extends BaseTransaction {

  private DataSource dataSource;
  private IsolationLevel isolationLevel = new IsolationLevel();
  private Configuration configuration;
  private Executor executor;

  public JdbcTransaction(Configuration configuration, DataSource ds, int isolationLevel) throws TransactionException {
    // Check Parameters
    this.configuration = configuration;
    dataSource = ds;
    if (dataSource == null) {
      throw new TransactionException("JdbcTransaction initialization failed.  DataSource was null.");
    }
    this.isolationLevel.setIsolationLevel(isolationLevel);
  }

  private void init() throws SQLException, TransactionException {
    // Open JDBC Transaction
    Connection connection = dataSource.getConnection();
    connection = ConnectionLogger.newInstance(connection);        
    if (connection == null) {
      throw new TransactionException("JdbcTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    // Isolation Level
    isolationLevel.applyIsolationLevel(connection);
    // AutoCommit
    if (connection.getAutoCommit()) {
      connection.setAutoCommit(false);
    }
    executor = configuration.newExecutor(new org.apache.ibatis.transaction.jdbc.JdbcTransaction(connection, false));
  }

  public void commit(boolean required) throws SQLException, TransactionException {
    if (executor != null) {
      executor.commit(required);
    }
  }

  public void rollback(boolean required) throws SQLException, TransactionException {
    if (executor != null) {
      executor.rollback(required);
    }
  }

  public void close() throws SQLException, TransactionException {
    if (executor != null) {
      try {
        isolationLevel.restoreIsolationLevel(executor.getTransaction().getConnection());
      } finally {
        executor.close(false);
        executor = null;
      }
    }
  }

  public Executor getExecutor() throws SQLException, TransactionException {
    if (executor == null) {
      init();
    }
    return executor;
  }

}
