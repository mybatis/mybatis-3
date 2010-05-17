package com.ibatis.sqlmap.engine.builder;

import com.ibatis.sqlmap.engine.datasource.DbcpDataSourceFactory;
import com.ibatis.sqlmap.engine.datasource.JndiDataSourceFactory;
import com.ibatis.sqlmap.engine.datasource.SimpleDataSourceFactory;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig;
import com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig;
import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.AutoMappingBehavior;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Ibatis2Configuration extends Configuration {

  private TransactionManager transactionManager;

  private DataSource dataSource;


  private FlushCacheInterceptor flushCachePlugin;

  private Map<String, Boolean> postSelectKeyMap;

  public Ibatis2Configuration() {
    setAutoMappingBehavior(AutoMappingBehavior.FULL);
    setUseGeneratedKeys(false);
    this.flushCachePlugin = new FlushCacheInterceptor();
    this.addInterceptor(flushCachePlugin);
    this.postSelectKeyMap = new HashMap<String, Boolean>();
    registerDefaultTypeAliases();
  }

  private void registerDefaultTypeAliases() {
    // TRANSACTION ALIASES
    this.getTypeAliasRegistry().registerAlias("JDBC", JdbcTransactionConfig.class.getName());
    this.getTypeAliasRegistry().registerAlias("JTA", JtaTransactionConfig.class.getName());
    this.getTypeAliasRegistry().registerAlias("EXTERNAL", ExternalTransactionConfig.class.getName());

    // DATA SOURCE ALIASES
    this.getTypeAliasRegistry().registerAlias("SIMPLE", SimpleDataSourceFactory.class.getName());
    this.getTypeAliasRegistry().registerAlias("DBCP", DbcpDataSourceFactory.class.getName());
    this.getTypeAliasRegistry().registerAlias("JNDI", JndiDataSourceFactory.class.getName());

    // CACHE ALIASES
    this.getTypeAliasRegistry().registerAlias("FIFO", FifoCache.class.getName());
    this.getTypeAliasRegistry().registerAlias("LRU", LruCache.class.getName());
    this.getTypeAliasRegistry().registerAlias("MEMORY", SoftCache.class.getName());
  }

  public TransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public FlushCacheInterceptor getFlushCachePlugin() {
    return flushCachePlugin;
  }

  public void setPostSelectKey(String statement, boolean postSelectKey) {
    this.postSelectKeyMap.put(statement, postSelectKey);
  }

  public boolean isPostSelectKey(String statement) {
    Boolean postSelectKey = postSelectKeyMap.get(statement);
    return postSelectKey == null || postSelectKey;
  }
}
