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
package org.apache.ibatis.executor.loader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

public class ResultLoader {

  protected final Configuration configuration;
  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  protected final Object parameterObject;
  protected final Class<?> targetType;

  protected boolean loaded;
  protected Object resultObject;

  public ResultLoader(Configuration config, Executor executor, MappedStatement mappedStatement, Object parameterObject, Class<?> targetType) {
    this.configuration = config;
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.parameterObject = parameterObject;
    this.targetType = targetType;
  }

  public <E> Object loadResult() throws SQLException {
    List<E> list = selectList();
    if (targetType != null && targetType.isAssignableFrom(list.getClass())) {
      resultObject = list;
    } else if (targetType != null && Collection.class.isAssignableFrom(targetType)) {
      resultObject = convertToDeclaredCollection(list, targetType);
    } else if (targetType != null && targetType.isArray()) {
      resultObject = listToArray(list, targetType.getComponentType());
    } else {
      if (list.size() > 1) {
        throw new ExecutorException("Statement " + mappedStatement.getId() + " returned more than one row, where no more than one was expected.");
      } else if (list.size() == 1) {
        resultObject = list.get(0);
      }
    }
    return resultObject;
  }

  private <E> List<E> selectList() throws SQLException {
    Executor localExecutor = executor;
    if (localExecutor.isClosed()) {
      localExecutor = newExecutor();
    }
    try {
      return localExecutor.<E> query(mappedStatement, parameterObject, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
    } finally {
      if (localExecutor != executor) {
        localExecutor.close(false);
      }
    }
  }

  private Executor newExecutor() throws SQLException {
    Environment environment = configuration.getEnvironment();
    if (environment == null) throw new ExecutorException("ResultLoader could not load lazily.  Environment was not configured.");
    DataSource ds = environment.getDataSource();
    if (ds == null) throw new ExecutorException("ResultLoader could not load lazily.  DataSource was not configured.");
    final TransactionFactory transactionFactory = environment.getTransactionFactory();
    Transaction tx = transactionFactory.newTransaction(ds, null, false);
    return configuration.newExecutor(tx, ExecutorType.SIMPLE);
  }

  public boolean wasNull() {
    return resultObject == null;
  }

  @SuppressWarnings("unchecked")
  private <E> E[] listToArray(List<E> list, Class<?> type) {
    E[] array = (E[]) java.lang.reflect.Array.newInstance(type, list.size());
    array = list.toArray(array);
    return array;
  }

  @SuppressWarnings("unchecked")
  private <E> Collection<E> convertToDeclaredCollection(List<E> result, Class<?> type) {
    Collection<E> collection = (Collection<E>) configuration.getObjectFactory().create(type);
    collection.addAll(result);
    return collection;
  }

}
