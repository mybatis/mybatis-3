package com.ibatis.sqlmap.engine.transaction.external;

import com.ibatis.sqlmap.engine.transaction.BaseTransaction;
import com.ibatis.sqlmap.engine.transaction.IsolationLevel;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ExternalTransaction extends BaseTransaction {

  private DataSource dataSource;
  private boolean defaultAutoCommit;
  private boolean setAutoCommitAllowed;
  private IsolationLevel isolationLevel = new IsolationLevel();
  private Configuration configuration;
  private Executor executor;

  public ExternalTransaction(Configuration configuration, DataSource ds, boolean defaultAutoCommit, boolean setAutoCommitAllowed, int isolationLevel) throws TransactionException {
    // Check Parameters
    this.configuration = configuration;
    dataSource = ds;
    if (dataSource == null) {
      throw new TransactionException("ExternalTransaction initialization failed.  DataSource was null.");
    }

    this.defaultAutoCommit = defaultAutoCommit;
    this.setAutoCommitAllowed = setAutoCommitAllowed;
    this.isolationLevel.setIsolationLevel(isolationLevel);
  }

  private void init() throws SQLException, TransactionException {
    // Open JDBC Transaction
    Connection connection = dataSource.getConnection();
    if (connection == null) {
      throw new TransactionException("ExternalTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    // Isolation Level
    isolationLevel.applyIsolationLevel(connection);
    // AutoCommit
    if (setAutoCommitAllowed) {
      if (connection.getAutoCommit() != defaultAutoCommit) {
        connection.setAutoCommit(defaultAutoCommit);
      }
    }
    executor = configuration.newExecutor(new JdbcTransaction(connection, false));
  }

  public void commit(boolean required) throws SQLException, TransactionException {
  }

  public void rollback(boolean required) throws SQLException, TransactionException {
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
