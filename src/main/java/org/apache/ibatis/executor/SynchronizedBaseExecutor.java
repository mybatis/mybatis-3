/*
 * Copyright 2013 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 *
 * @author Franta Mejta
 * @sa.date 2013-07-26T12:26:44+0200
 */
abstract class SynchronizedBaseExecutor extends BaseExecutor {

  private final Object lock = new Object();

  public SynchronizedBaseExecutor(final Configuration configuration, final Transaction transaction) {
    super(configuration, transaction);
  }

  @Override
  public Transaction getTransaction() {
    synchronized (this.lock) {
      return super.getTransaction();
    }
  }

  @Override
  public void close(boolean forceRollback) {
    synchronized (this.lock) {
      super.close(forceRollback);
    }
  }

  @Override
  public boolean isClosed() {
    synchronized (this.lock) {
      return super.isClosed();
    }
  }

  @Override
  public int update(MappedStatement ms, Object parameter) throws SQLException {
    synchronized (this.lock) {
      return super.update(ms, parameter);
    }
  }

  @Override
  public List<BatchResult> flushStatements() throws SQLException {
    synchronized (this.lock) {
      return super.flushStatements();
    }
  }

  @Override
  public List<BatchResult> flushStatements(boolean isRollBack) throws SQLException {
    synchronized (this.lock) {
      return super.flushStatements(isRollBack);
    }
  }

  @Override
  public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
    synchronized (this.lock) {
      return super.query(ms, parameter, rowBounds, resultHandler);
    }
  }

  @Override
  public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    synchronized (this.lock) {
      return super.query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }
  }

  @Override
  public void deferLoad(MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType) {
    synchronized (this.lock) {
      super.deferLoad(ms, resultObject, property, key, targetType);
    }
  }

  @Override
  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    synchronized (this.lock) {
      return super.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }
  }

  @Override
  public boolean isCached(MappedStatement ms, CacheKey key) {
    synchronized (this.lock) {
      return super.isCached(ms, key);
    }
  }

  @Override
  public void commit(boolean required) throws SQLException {
    synchronized (this.lock) {
      super.commit(required);
    }
  }

  @Override
  public void rollback(boolean required) throws SQLException {
    synchronized (this.lock) {
      super.rollback(required);
    }
  }

  @Override
  public void clearLocalCache() {
    synchronized (this.lock) {
      super.clearLocalCache();
    }
  }

  @Override
  protected void closeStatement(Statement statement) {
    synchronized (this.lock) {
      super.closeStatement(statement);
    }
  }

  @Override
  protected Connection getConnection(Log statementLog) throws SQLException {
    synchronized (this.lock) {
      return super.getConnection(statementLog);
    }
  }
}
