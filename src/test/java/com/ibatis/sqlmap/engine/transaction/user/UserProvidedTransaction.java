package com.ibatis.sqlmap.engine.transaction.user;

import com.ibatis.sqlmap.engine.transaction.BaseTransaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;

import java.sql.Connection;
import java.sql.SQLException;

public class UserProvidedTransaction extends BaseTransaction {

  private Executor executor;

  public UserProvidedTransaction(Configuration configuration, Connection connection) {
    this.executor = configuration.newExecutor(new JdbcTransaction(connection, false));
  }

  public void commit(boolean required) throws SQLException, TransactionException {
    executor.commit(required);
  }

  public void rollback(boolean required) throws SQLException, TransactionException {
    executor.rollback(required);
  }

  public void close() throws SQLException, TransactionException {
  }

  public Executor getExecutor() throws SQLException, TransactionException {
    return executor;
  }

}
