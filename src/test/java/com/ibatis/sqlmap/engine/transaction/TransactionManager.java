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
package com.ibatis.sqlmap.engine.transaction;

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.builder.Ibatis2Configuration;
import com.ibatis.sqlmap.engine.transaction.user.UserProvidedTransaction;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.Configuration;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

  private ThreadLocal<Transaction> localTransaction = new ThreadLocal<Transaction>();

  private TransactionConfig trasactionConfig;
  private Configuration configuration;

  public TransactionManager(Ibatis2Configuration config, TransactionConfig transactionConfig) {
    this.trasactionConfig = transactionConfig;
    this.configuration = config;
  }

  public void begin() throws SQLException, TransactionException {
    begin(IsolationLevel.UNSET_ISOLATION_LEVEL);
  }

  public void begin(Connection connection) {
    Transaction transaction = localTransaction.get();
    if (transaction != null) {
      throw new TransactionException("TransactionManager could not start a new transaction. " +
          "A transaction is already started.");
    }
    transaction = new UserProvidedTransaction(configuration, connection);
    transaction.setCommitRequired(false);
    localTransaction.set(transaction);
  }

  public void begin(int transactionIsolation) throws SQLException, TransactionException {
    Transaction transaction = localTransaction.get();
    if (transaction != null) {
      throw new TransactionException("TransactionManager could not start a new transaction.  " +
          "A transaction is already started.");
    }
    transaction = trasactionConfig.newTransaction(configuration, transactionIsolation);
    transaction.setCommitRequired(false);
    localTransaction.set(transaction);
  }

  public void commit() throws SQLException, TransactionException {
    Transaction trans = localTransaction.get();
    if (trans == null) {
      throw new TransactionException("TransactionManager could not commit.  No transaction is started.");
    }
    boolean required = trans.isCommitRequired() || trasactionConfig.isForceCommit();
    trans.commit(required);
    trans.setCommitRequired(false);
  }

  public void end() throws SQLException, TransactionException {
    Transaction transaction = localTransaction.get();
    if (transaction != null) {
      try {
        try {
          boolean required = transaction.isCommitRequired() || trasactionConfig.isForceCommit();
          transaction.rollback(required);
        } finally {
          transaction.setCommitRequired(false);
          transaction.close();
        }
      } finally {
        localTransaction.set(null);
      }
    }
  }

  public Connection getCurrentConnection() throws SQLException {
    return getCurrentExecutor().getTransaction().getConnection();
  }

  public Executor getCurrentExecutor() throws SQLException {
    return getCurrentTransaction().getExecutor();
  }

  public Transaction getCurrentTransaction() throws SQLException {
    Transaction transaction = localTransaction.get();
    if (transaction != null) {
      try {
        return transaction;
      } catch (TransactionException e) {
        throw new SqlMapException("Could not get transaction.  Cause: " + e, e);
      }
    }
    throw new SQLException("Could not get current transaction, because there was no transaction started.");
  }

  public TransactionConfig getTrasactionConfig() {
    return trasactionConfig;
  }

  public boolean isInTransaction() {
    return this.localTransaction.get() != null;
  }

  public Object doInTransaction(TransactionScope transactionScope) throws SQLException {
    Transaction transaction = this.localTransaction.get();
    if (transaction == null) {
      try {
        begin();
        transaction = this.localTransaction.get();
        Object result = transactionScope.execute(transaction);
        commit();
        return result;
      } finally {
        end();
      }
    } else {
      return transactionScope.execute(transaction);
    }

  }

}