package org.apache.ibatis.executor;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.TransactionalCacheManager;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public class CachingExecutor implements Executor {

  private Executor delegate;
  private TransactionalCacheManager tcm = new TransactionalCacheManager();

  public CachingExecutor(Executor delegate) {
    this.delegate = delegate;
  }

  public Transaction getTransaction() {
    return delegate.getTransaction();
  }

  public void close(boolean forceRollback) {
    try {
      tcm.commit();
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


  public List query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    if (ms != null) {
      Cache cache = ms.getCache();
      if (cache != null) {
        flushCacheIfRequired(ms);
        cache.getReadWriteLock().readLock().lock();
        try {
          if (ms.isUseCache() && resultHandler == null) {
            CacheKey key = createCacheKey(ms, parameterObject, rowBounds);
            final List cachedList = (List) cache.getObject(key);
            if (cachedList != null) {
              return cachedList;
            } else {
              List list = delegate.query(ms, parameterObject, rowBounds, resultHandler);
              tcm.putObject(cache, key, list);
              return list;
            }
          } else {
            return delegate.query(ms, parameterObject, rowBounds, resultHandler);
          }
        } finally {
          cache.getReadWriteLock().readLock().unlock();
        }
      }
    }
    return delegate.query(ms, parameterObject, rowBounds, resultHandler);
  }

  public List flushStatements() throws SQLException {
    return delegate.flushStatements();
  }

  public void commit(boolean required) throws SQLException {
    delegate.commit(required);
    tcm.commit();
  }

  public void rollback(boolean required) throws SQLException {
    try {
      delegate.rollback(required);
    } finally {
      if (required) {
        tcm.rollback();
      }
    }
  }

  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
    return delegate.createCacheKey(ms, parameterObject, rowBounds);
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
        tcm.clear(cache);
      }
    }
  }

}
