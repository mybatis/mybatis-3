/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.executor;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public class CachingExecutor implements Executor {

  private Executor delegate;
  private boolean autoCommit; // issue #573. No need to call commit() on autoCommit sessions
  private TransactionalCacheManager tcm = new TransactionalCacheManager();

  private boolean dirty;

  public CachingExecutor(Executor delegate) {
    this(delegate, false);
  }

  public CachingExecutor(Executor delegate, boolean autoCommit) {
    this.delegate = delegate;
    this.autoCommit = autoCommit;
  }

  public Transaction getTransaction() {
    return delegate.getTransaction();
  }

  public void close(boolean forceRollback) {
    try {
      //issue #499. Unresolved session handling
      //issue #573. Autocommit sessions should commit
      if (dirty && !autoCommit) { 
        tcm.rollback();
      } else {
        tcm.commit();
      }
    } finally {
      delegate.close(forceRollback);
    }
  }

  public boolean isClosed() {
    return delegate.isClosed();
  }

  public int update(MappedStatement ms, Object parameterObject) throws SQLException {
    flushCacheIfRequired(ms);
    return delegate.update(ms, parameterObject);
  }

  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
    return query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    Cache cache = ms.getCache();
    if (cache != null) {
      flushCacheIfRequired(ms);
      if (ms.isUseCache() && resultHandler == null) { 
        ensureNoOutParams(ms, key, parameterObject, boundSql);
        cache.getReadWriteLock().readLock().lock();
        try {
          @SuppressWarnings("unchecked")
          List<E> cachedList = dirty ? null : (List<E>) cache.getObject(key);
          if (cachedList != null) {
            return cachedList;
          } else {
            List<E> list = delegate.<E> query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
            tcm.putObject(cache, key, list);
            return list;
          }
        } finally {
          cache.getReadWriteLock().readLock().unlock();
        }
      }
    }
    return delegate.<E>query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

  public List<BatchResult> flushStatements() throws SQLException {
    return delegate.flushStatements();
  }

  public void commit(boolean required) throws SQLException {
    delegate.commit(required);
    tcm.commit();
    dirty = false;
  }

  public void rollback(boolean required) throws SQLException {
    try {
      delegate.rollback(required);
      dirty = false;
    } finally {
      if (required) {
        tcm.rollback();
      }
    }
  }

  private void ensureNoOutParams(MappedStatement ms, CacheKey key, Object parameter, BoundSql boundSql) {
    if (ms.getStatementType() == StatementType.CALLABLE) {
      for (ParameterMapping parameterMapping : ms.getBoundSql(parameter).getParameterMappings()) {
        if (parameterMapping.getMode() != ParameterMode.IN) {
          throw new ExecutorException("Caching stored procedures with OUT params is not supported.  Please configure useCache=false in " + ms.getId() + " statement.");
        }
      }
    }
  }

  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
  }

  public boolean isCached(MappedStatement ms, CacheKey key) {
    throw new UnsupportedOperationException("The CachingExecutor should not be used by result loaders and thus isCached() should never be called.");
  }

  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key) {
    throw new UnsupportedOperationException("The CachingExecutor should not be used by result loaders and thus deferLoad() should never be called.");
  }

  public void clearLocalCache() {
    delegate.clearLocalCache();
  }

  private void flushCacheIfRequired(MappedStatement ms) {
    Cache cache = ms.getCache();
    if (cache != null) {
      if (ms.isFlushCacheRequired()) {
        dirty = true; // issue #524. Disable using cached data for this session
        tcm.clear(cache);
      }
    }
  }

}
