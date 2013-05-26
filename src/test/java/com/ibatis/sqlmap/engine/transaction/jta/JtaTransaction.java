/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.ibatis.sqlmap.engine.transaction.jta;

import com.ibatis.sqlmap.engine.transaction.BaseTransaction;
import com.ibatis.sqlmap.engine.transaction.IsolationLevel;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.SQLException;

public class JtaTransaction extends BaseTransaction {

  private UserTransaction userTransaction;
  private DataSource dataSource;
  private Configuration configuration;
  private Executor executor;
  private IsolationLevel isolationLevel = new IsolationLevel();

  private boolean commmitted = false;
  private boolean newTransaction = false;

  public JtaTransaction(Configuration configuration, UserTransaction utx, DataSource ds, int isolationLevel) throws TransactionException {
    // Check parameters
    this.configuration = configuration;
    userTransaction = utx;
    dataSource = ds;
    if (userTransaction == null) {
      throw new TransactionException("JtaTransaction initialization failed.  UserTransaction was null.");
    }
    if (dataSource == null) {
      throw new TransactionException("JtaTransaction initialization failed.  DataSource was null.");
    }
    this.isolationLevel.setIsolationLevel(isolationLevel);
  }

  private void init() throws TransactionException, SQLException {
    // Start JTA Transaction
    try {
      newTransaction = userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION;
      if (newTransaction) {
        userTransaction.begin();
      }
    } catch (Exception e) {
      throw new TransactionException("JtaTransaction could not start transaction.  Cause: ", e);
    }

    // Open JDBC Connection
    Connection connection = dataSource.getConnection();
    if (connection == null) {
      throw new TransactionException("JtaTransaction could not start transaction.  Cause: The DataSource returned a null connection.");
    }
    // Isolation Level
    isolationLevel.applyIsolationLevel(connection);
    // AutoCommit
    if (connection.getAutoCommit()) {
      connection.setAutoCommit(false);
    }
    executor = configuration.newExecutor(new JdbcTransaction(connection));
  }

  public void commit(boolean required) throws SQLException, TransactionException {
    if (required) {
      if (executor != null) {
        if (commmitted) {
          throw new TransactionException("JtaTransaction could not commit because this transaction has already been committed.");
        }
        try {
          if (newTransaction) {
            userTransaction.commit();
          }
        } catch (Exception e) {
          throw new TransactionException("JtaTransaction could not commit.  Cause: ", e);
        }
        commmitted = true;
      }
    }
  }

  public void rollback(boolean required) throws SQLException, TransactionException {
    if (required) {
      if (executor != null) {
        if (!commmitted) {
          try {
            if (userTransaction != null) {
              if (newTransaction) {
                userTransaction.rollback();
              } else {
                userTransaction.setRollbackOnly();
              }
            }
          } catch (Exception e) {
            throw new TransactionException("JtaTransaction could not rollback.  Cause: ", e);
          }
        }
      }
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
