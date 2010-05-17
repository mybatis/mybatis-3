package com.ibatis.sqlmap.engine.transaction;

import org.apache.ibatis.executor.Executor;

import java.sql.SQLException;

public interface Transaction {

  public void commit(boolean required) throws SQLException, TransactionException;

  public void rollback(boolean required) throws SQLException, TransactionException;

  public void close() throws SQLException, TransactionException;

  public Executor getExecutor() throws SQLException, TransactionException;

  public boolean isCommitRequired();

  public void setCommitRequired(boolean commitRequired);

}
