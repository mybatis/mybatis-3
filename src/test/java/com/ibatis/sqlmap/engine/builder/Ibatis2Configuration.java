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
    // changed to JDBC2 because it collides with the default typealias defined in mb3
    this.getTypeAliasRegistry().registerAlias("JDBC2", JdbcTransactionConfig.class.getName());
    this.getTypeAliasRegistry().registerAlias("JTA", JtaTransactionConfig.class.getName());
    this.getTypeAliasRegistry().registerAlias("EXTERNAL", ExternalTransactionConfig.class.getName());

    // DATA SOURCE ALIASES
    this.getTypeAliasRegistry().registerAlias("SIMPLE", SimpleDataSourceFactory.class.getName());
    this.getTypeAliasRegistry().registerAlias("DBCP", DbcpDataSourceFactory.class.getName());
//    this.getTypeAliasRegistry().registerAlias("JNDI", JndiDataSourceFactory.class.getName());

    // CACHE ALIASES
//    this.getTypeAliasRegistry().registerAlias("FIFO", FifoCache.class.getName());
//    this.getTypeAliasRegistry().registerAlias("LRU", LruCache.class.getName());
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
