package com.ibatis.sqlmap.engine.transaction;

import javax.sql.DataSource;

public abstract class BaseTransactionConfig implements TransactionConfig {

  protected DataSource dataSource;
  protected boolean forceCommit;

  public boolean isForceCommit() {
    return forceCommit;
  }

  public void setForceCommit(boolean forceCommit) {
    this.forceCommit = forceCommit;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource ds) {
    this.dataSource = ds;
  }

}
