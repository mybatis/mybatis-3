package org.apache.ibatis.mapping;

import javax.sql.DataSource;

import org.apache.ibatis.transaction.TransactionFactory;

public final class Environment {
  private final String id;
  private final TransactionFactory transactionFactory;
  private final DataSource dataSource;
  private final String databaseId;
  private final DatabaseIdProvider databaseIdProvider;

  public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
    this(id, transactionFactory, dataSource, null, null);
  }

  public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource, String databaseId) {
    this(id, transactionFactory, dataSource, databaseId, null);
  }

  public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource, DatabaseIdProvider databaseIdProvider) {
    this(id, transactionFactory, dataSource, null, databaseIdProvider);
  }
  
  public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource, String databaseId, DatabaseIdProvider databaseIdProvider) {
    if (id == null) {
      throw new IllegalArgumentException("Parameter 'id' must not be null");
    }
    if (transactionFactory == null) {
      throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
    }
    if (dataSource == null) {
      throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
    }
    this.id = id;
    this.transactionFactory = transactionFactory;
    this.dataSource = dataSource;
    if (databaseIdProvider != null) {      
      this.databaseIdProvider = databaseIdProvider;
    } else {
      this.databaseIdProvider = new DefaultDatabaseIdProvider();
    }
    if (databaseId != null) {
      this.databaseId = databaseId;
    } else {
      this.databaseId = this.databaseIdProvider.getDatabaseId(this.dataSource);
    }
  }

  public static class Builder {
    private String id;
    private TransactionFactory transactionFactory;
    private DataSource dataSource;
    private String databaseId;
    private DatabaseIdProvider databaseIdProvider;

    public Builder(String id) {
      this.id = id;
    }

    public Builder transactionFactory(TransactionFactory transactionFactory) {
      this.transactionFactory = transactionFactory;
      return this;
    }

    public Builder dataSource(DataSource dataSource) {
      this.dataSource = dataSource;
      return this;
    }

    public Builder databaseId(String databaseId) {
      this.databaseId = databaseId;
      return this;
    }

    public Builder databaseIdProvider(DatabaseIdProvider databaseIdProvider) {
      this.databaseIdProvider = databaseIdProvider;
      return this;
    }
    
    public String id() {
      return this.id;
    }

    public Environment build() {
      return new Environment(this.id, this.transactionFactory, this.dataSource, this.databaseId, this.databaseIdProvider);
    }

  }

  public String getId() {
    return this.id;
  }

  public TransactionFactory getTransactionFactory() {
    return this.transactionFactory;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  public String getDatabaseId() {
    return databaseId;
  }

}
