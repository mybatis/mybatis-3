package org.apache.ibatis.mapping;

import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;

public class Environment {
  private String id;
  private TransactionFactory transactionFactory;
  private DataSource dataSource;

  public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
    this.id = id;
    this.transactionFactory = transactionFactory;
    this.dataSource = dataSource;
  }

  private Environment() {
  }

  public static class Builder {
    private Environment environment = new Environment();

    public Builder(String id, TransactionFactory transactionManager, DataSource dataSource) {
      environment.id = id;
      environment.transactionFactory = transactionManager;
      environment.dataSource = dataSource;
    }

    public Builder transactionFactory(TransactionFactory transactionFactory) {
      environment.transactionFactory = transactionFactory;
      return this;
    }

    public Builder dataSource(DataSource dataSource) {
      environment.dataSource = dataSource;
      return this;
    }

    public String id() {
      return environment.id;
    }

    public Environment build() {
      return environment;
    }

  }

  public String getId() {
    return id;
  }

  public TransactionFactory getTransactionFactory() {
    return transactionFactory;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

}
