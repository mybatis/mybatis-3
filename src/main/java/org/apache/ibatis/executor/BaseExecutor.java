package org.apache.ibatis.executor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.impl.PerpetualCache;
import static org.apache.ibatis.executor.ExecutionPlaceholder.EXECUTION_PLACEHOLDER;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseExecutor implements Executor {

  protected Transaction transaction;

  protected ConcurrentLinkedQueue<DeferredLoad> deferredLoads;
  protected PerpetualCache localCache;
  protected Configuration configuration;

  protected int queryStack = 0;

  protected List<BatchResult> batchResults = new ArrayList<BatchResult>();
  private boolean closed;

  protected BaseExecutor(Configuration configuration, Transaction transaction) {
    this.transaction = transaction;
    this.deferredLoads = new ConcurrentLinkedQueue<DeferredLoad>();
    this.localCache = new PerpetualCache("LocalCache");
    this.closed = false;
    this.configuration = configuration;
  }

  public Transaction getTransaction() {
    if (closed) throw new ExecutorException("Executor was closed.");
    return transaction;
  }

  public void close(boolean forceRollback) {
    try {
      try {
        rollback(forceRollback);
      } finally {
        if (transaction != null) transaction.close();
      }
    } catch (SQLException e) {
      // Ignore.  There's nothing that can be done at this point.
    } finally {
      transaction = null;
      deferredLoads = null;
      localCache = null;
      batchResults = null;
      closed = true;
    }
  }

  public boolean isClosed() {
    return closed;
  }

  public int update(MappedStatement ms, Object parameter) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
    if (closed) throw new ExecutorException("Executor was closed.");
    clearLocalCache();
    return doUpdate(ms, parameter);
  }

  public List flushStatements() throws SQLException {
    if (closed) throw new ExecutorException("Executor was closed.");
    batchResults.addAll(doFlushStatements());
    return batchResults;
  }

  public List query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing a query").object(ms.getId());
    if (closed) throw new ExecutorException("Executor was closed.");
    List list;
    try {
      queryStack++;
      CacheKey key = createCacheKey(ms, parameter, rowBounds);
      final List cachedList = (List) localCache.getObject(key);
      if (cachedList != null) {
        list = cachedList;
      } else {
        localCache.putObject(key, EXECUTION_PLACEHOLDER);
        try {
          list = doQuery(ms, parameter, rowBounds, resultHandler);
        } finally {
          localCache.removeObject(key);
        }
        localCache.putObject(key, list);
      }
    } finally {
      queryStack--;
    }
    if (queryStack == 0) {
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
    }
    return list;
  }

  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key) {
    if (closed) throw new ExecutorException("Executor was closed.");
    deferredLoads.add(new DeferredLoad(ms, resultObject, property, key));
  }

  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
    if (closed) throw new ExecutorException("Executor was closed.");
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    CacheKey cacheKey = new CacheKey();
    cacheKey.update(ms.getId());
    cacheKey.update(rowBounds.getOffset());
    cacheKey.update(rowBounds.getLimit());
    cacheKey.update(boundSql.getSql());
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    if (parameterMappings.size() > 0 && parameterObject != null) {
      TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
      if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
        cacheKey.update(parameterObject);
      } else {
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        for (ParameterMapping parameterMapping : parameterMappings) {
          String propertyName = parameterMapping.getProperty();
          if (metaObject.hasGetter(propertyName)) {
            cacheKey.update(metaObject.getValue(propertyName));
          } else if (boundSql.hasAdditionalParameter(propertyName)) {
            cacheKey.update(boundSql.getAdditionalParameter(propertyName));
          }
        }
      }
    }
    return cacheKey;
  }

  public boolean isCached(MappedStatement ms, CacheKey key) {
    return localCache.getObject(key) != null;
  }

  public void commit(boolean required) throws SQLException {
    if (closed) {
      throw new ExecutorException("Cannot commit, transaction is already closed");
    }
    clearLocalCache();
    flushStatements();
    if (required) {
      transaction.commit();
    }
  }

  public void rollback(boolean required) throws SQLException {
    if (!closed) {
      try {
        clearLocalCache();
        flushStatements();
      } finally {
        if (required) {
          transaction.rollback();
        }
      }
    }
  }

  public void clearLocalCache() {
    if (!closed) {
      localCache.clear();
    }
  }

  protected abstract int doUpdate(MappedStatement ms, Object parameter)
      throws SQLException;

  protected abstract List<BatchResult> doFlushStatements()
      throws SQLException;

  protected abstract List doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)
      throws SQLException;

  protected void closeStatement(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        // ignore
      }
    }
  }

  private class DeferredLoad {

    MappedStatement mappedStatement;
    private MetaObject resultObject;
    private String property;
    private CacheKey key;

    public DeferredLoad(MappedStatement mappedStatement, MetaObject resultObject, String property, CacheKey key) {
      this.mappedStatement = mappedStatement;
      this.resultObject = resultObject;
      this.property = property;
      this.key = key;
    }

    public void load() {
      Object value = null;
      List list = (List) localCache.getObject(key);
      Class targetType = resultObject.getSetterType(property);
      if (Set.class.isAssignableFrom(targetType)) {
        value = new HashSet(list);
      } else if (Collection.class.isAssignableFrom(targetType)) {
        value = list;
      } else if (targetType.isArray()) {
        Object array = java.lang.reflect.Array.newInstance(targetType.getComponentType(), list.size());
        value = list.toArray((Object[]) array);
      } else {
        if (list != null && list.size() > 1) {
          throw new ExecutorException("Statement returned more than one row, where no more than one was expected.");
        } else if (list != null && list.size() == 1) {
          value = list.get(0);
        }
      }
      resultObject.setValue(property, value);
    }

  }


}
