package org.apache.ibatis.session.defaults;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DefaultSqlSessionFactory implements SqlSessionFactory {

  private static final Log log = LogFactory.getLog(Connection.class);

  private final Configuration configuration;
  private final TransactionFactory managedTransactionFactory;

  public DefaultSqlSessionFactory(Configuration configuration) {
    this.configuration = configuration;
    this.managedTransactionFactory = new ManagedTransactionFactory();
  }

  public SqlSession openSession() {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, false);
  }

  public SqlSession openSession(boolean autoCommit) {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), null, autoCommit);
  }

  public SqlSession openSession(ExecutorType execType) {
    return openSessionFromDataSource(execType, null, false);
  }

  public SqlSession openSession(TransactionIsolationLevel level) {
    return openSessionFromDataSource(configuration.getDefaultExecutorType(), level, false);
  }

  public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
    return openSessionFromDataSource(execType, level, false);
  }

  public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
    return openSessionFromDataSource(execType, null, autoCommit);
  }

  public SqlSession openSession(Connection connection) {
    return openSessionFromConnection(configuration.getDefaultExecutorType(), connection);
  }

  public SqlSession openSession(ExecutorType execType, Connection connection) {
    return openSessionFromConnection(execType, connection);
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  private SqlSession openSessionFromDataSource(ExecutorType execType, TransactionIsolationLevel level, boolean autoCommit) {
    Connection connection = null;
    try {
      final Environment environment = configuration.getEnvironment();
      final DataSource dataSource = getDataSourceFromEnvironment(environment);
      TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
      connection = dataSource.getConnection();
      if (level != null) {
        connection.setTransactionIsolation(level.getLevel());
      }
      connection = wrapConnection(connection);
      Transaction tx = transactionFactory.newTransaction(connection, autoCommit);
      Executor executor = configuration.newExecutor(tx, execType);
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      closeConnection(connection);
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }

  private SqlSession openSessionFromConnection(ExecutorType execType, Connection connection) {
    try {
      boolean autoCommit;
      try {
        autoCommit = connection.getAutoCommit();
      } catch (SQLException e) {
        // Failover to true, as most poor drivers
        // or databases won't support transactions
        autoCommit = true;
      }
      connection = wrapConnection(connection);
      final Environment environment = configuration.getEnvironment();
      final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
      Transaction tx = transactionFactory.newTransaction(connection, autoCommit);
      Executor executor = configuration.newExecutor(tx, execType);
      return new DefaultSqlSession(configuration, executor, autoCommit);
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error opening session.  Cause: " + e, e);
    } finally {
      ErrorContext.instance().reset();
    }
  }

  private DataSource getDataSourceFromEnvironment(Environment environment) {
    if (environment == null || environment.getDataSource() == null) {
      throw new SqlSessionException("Configuration does not include an environment with a DataSource, so session cannot be created unless a connection is passed in.");
    }
    return environment.getDataSource();
  }

  private TransactionFactory getTransactionFactoryFromEnvironment(Environment environment) {
    if (environment == null || environment.getTransactionFactory() == null) {
      return managedTransactionFactory;
    }
    return environment.getTransactionFactory();
  }

  private Connection wrapConnection(Connection connection) {
    if (log.isDebugEnabled()) {
      return ConnectionLogger.newInstance(connection);
    } else {
      return connection;
    }
  }

  private void closeConnection(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e1) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

}

