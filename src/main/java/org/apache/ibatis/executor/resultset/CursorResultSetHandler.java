/*
 *    Copyright 2009-2013 the original author or authors.
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
package org.apache.ibatis.executor.resultset;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * @author Guillaume Darmont / guillaume@dropinocean.com
 */
public class CursorResultSetHandler extends DefaultResultSetHandler {

  private final FetchType fetchType;
  private Object previousRowValue;

  public CursorResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql, RowBounds rowBounds, FetchType fetchType) {
    super(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    this.fetchType = fetchType;
  }

  @Override
  protected void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
    if (resultHandler == null) {
      List cursorList = getResultList(rsw, resultMap, parentMapping);
      multipleResults.add(cursorList);
    } else {
      throw new IllegalStateException("CursorNestedResultSetHandler cannot be used with external ResultHandler");
    }
  }

  private List getResultList(ResultSetWrapper rsw, ResultMap resultMap, ResultMapping parentMapping) {
    if (fetchType == FetchType.CURSOR) {
      return new CursorList(this, rsw, resultMap, parentMapping);
    } else if (fetchType == FetchType.LAZY) {
      return new LazyList(this, rsw, resultMap, parentMapping);
    } else {
      throw new IllegalArgumentException("FetchType " + fetchType
              + " is not supported by CursorNestedResultSetHandler");
    }
  }

  @Override
  protected void handleRowValuesForNestedResultMap(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
    final DefaultResultContext resultContext = new DefaultResultContext();
    skipRows(rsw.getResultSet(), rowBounds);
    Object rowValue = previousRowValue;
    while (shouldProcessMoreRows(rsw.getResultSet(), resultContext, rowBounds)) {
      final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rsw.getResultSet(), resultMap, null);
      final CacheKey rowKey = createRowKey(discriminatedResultMap, rsw, null);
      Object partialObject = nestedResultObjects.get(rowKey);
      if (mappedStatement.isResultOrdered()) { // issue #577 && #542
        if (partialObject == null && rowValue != null) {
          nestedResultObjects.clear();
          storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
        }
        rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, rowKey, null,
                partialObject);
      } else {
        rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, rowKey, null,
                partialObject);
        if (partialObject == null) {
          storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
        }
      }
    }
    if (rowValue != null && mappedStatement.isResultOrdered() && !resultContext.isStopped()) {
      storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
      previousRowValue = null;
    } else if (rowValue != null) {
      previousRowValue = rowValue;
    }
  }

  protected void callResultHandler(ResultHandler resultHandler, DefaultResultContext resultContext, Object rowValue) {
    super.callResultHandler(resultHandler, resultContext, rowValue);
    resultContext.stop();
  }
}
