/**
 *    Copyright 2009-2016 the original author or authors.
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

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.executor.dialect.Dialect;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author liuzenghui
 */
public class PagingExecutor implements Executor {

  private Executor delegate;

  private Dialect dialect;

  private Field additionalParametersField;

  public PagingExecutor(Executor delegate, Dialect dialect) {
    this.delegate = delegate;
    this.dialect = dialect;
    try {
      additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
      additionalParametersField.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Transaction getTransaction() {
    return delegate.getTransaction();
  }

  @Override
  public void close(boolean forceRollback) {
    delegate.close(forceRollback);
  }

  @Override
  public boolean isClosed() {
    return delegate.isClosed();
  }

  @Override
  public int update(MappedStatement ms, Object parameterObject) throws SQLException {
    return delegate.update(ms, parameterObject);
  }

  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    BoundSql boundSql = ms.getBoundSql(parameterObject);
    Map<String, Object> additionalParameters = null;
    try {
      additionalParameters = (Map<String, Object>) additionalParametersField.get(boundSql);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    if(dialect.doCount(ms, parameterObject, rowBounds)){
      //create a Long resultType ms from ms
      MappedStatement countMappedStatement = newCountMappedStatement(ms);
      CacheKey countKey = createCacheKey(countMappedStatement, parameterObject, RowBounds.DEFAULT, boundSql);
      String countSql = dialect.getCountSql(ms, boundSql, parameterObject, rowBounds, countKey);
      BoundSql countBoundSql = new BoundSql(ms.getConfiguration(), countSql, boundSql.getParameterMappings(), parameterObject);
      for (String key : additionalParameters.keySet()) {
        countBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
      }
      List<Long> countList = query(countMappedStatement, parameterObject, RowBounds.DEFAULT, resultHandler, countKey, countBoundSql);
      Long count = countList.get(0);
      dialect.afterCount(count, parameterObject, rowBounds);
      //if count is 0, direct return.
      if (count == 0L) {
        return dialect.afterPage(new ArrayList(), parameterObject, rowBounds);
      }
    }
    if(dialect.doPage(ms, parameterObject, rowBounds)){
      CacheKey pageKey = createCacheKey(ms, parameterObject, rowBounds, boundSql);
      parameterObject = dialect.processParameterObject(ms, parameterObject, boundSql, pageKey);
      String pageSql = dialect.getPageSql(ms, boundSql, parameterObject, rowBounds, pageKey);
      BoundSql pageBoundSql = new BoundSql(ms.getConfiguration(), pageSql, boundSql.getParameterMappings(), parameterObject);
      for (String key : additionalParameters.keySet()) {
        pageBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
      }
      List<E> pageList = query(ms, parameterObject, RowBounds.DEFAULT, resultHandler, pageKey, pageBoundSql);
      return dialect.afterPage(pageList, parameterObject, rowBounds);
    } else {
      CacheKey key = createCacheKey(ms, parameterObject, rowBounds, boundSql);
      List<E> list = query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
      return dialect.afterPage(list, parameterObject, rowBounds);
    }
  }

  @Override
  public <E> Cursor<E> queryCursor(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
    return delegate.queryCursor(ms, parameter, rowBounds);
  }

  @Override
  public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
          throws SQLException {
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
  }

  @Override
  public List<BatchResult> flushStatements() throws SQLException {
    return delegate.flushStatements();
  }

  @Override
  public void commit(boolean required) throws SQLException {
    delegate.commit(required);
  }

  @Override
  public void rollback(boolean required) throws SQLException {
    delegate.rollback(required);
  }

  @Override
  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
  }

  @Override
  public boolean isCached(MappedStatement ms, CacheKey key) {
    return delegate.isCached(ms, key);
  }

  @Override
  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
    delegate.deferLoad(ms, resultObject, property, key, targetType);
  }

  @Override
  public void clearLocalCache() {
    delegate.clearLocalCache();
  }

  @Override
  public void setExecutorWrapper(Executor executor) {
    throw new UnsupportedOperationException("This method should not be called");
  }

  private MappedStatement newCountMappedStatement(MappedStatement ms) {
    MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId() + "_COUNT", ms.getSqlSource(), ms.getSqlCommandType());
    builder.resource(ms.getResource());
    builder.fetchSize(ms.getFetchSize());
    builder.statementType(ms.getStatementType());
    builder.keyGenerator(ms.getKeyGenerator());
    if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
      StringBuilder keyProperties = new StringBuilder();
      for (String keyProperty : ms.getKeyProperties()) {
        keyProperties.append(keyProperty).append(",");
      }
      keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
      builder.keyProperty(keyProperties.toString());
    }
    builder.timeout(ms.getTimeout());
    builder.parameterMap(ms.getParameterMap());
    List<ResultMap> resultMaps = new ArrayList<ResultMap>();
    ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class, new ArrayList<ResultMapping>(0)).build();
    resultMaps.add(resultMap);
    builder.resultMaps(resultMaps);
    builder.resultSetType(ms.getResultSetType());
    builder.cache(ms.getCache());
    builder.flushCacheRequired(ms.isFlushCacheRequired());
    builder.useCache(ms.isUseCache());

    return builder.build();
  }

}
