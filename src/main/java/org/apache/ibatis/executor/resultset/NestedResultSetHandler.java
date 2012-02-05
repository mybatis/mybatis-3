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
package org.apache.ibatis.executor.resultset;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;

public class NestedResultSetHandler extends FastResultSetHandler {

  private final Map<CacheKey, Set<CacheKey>> localRowValueCaches;
  private final Map<CacheKey, Object> globalRowValueCache;

  public NestedResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql, RowBounds rowBounds) {
    super(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    localRowValueCaches = new HashMap<CacheKey, Set<CacheKey>>();
    globalRowValueCache = new HashMap<CacheKey, Object>();
    if (configuration.isSafeRowBoundsEnabled()) {
      ensureNoRowBounds(rowBounds);
    }
  }

  private void ensureNoRowBounds(RowBounds rowBounds) {
    if (rowBounds != null
        && (rowBounds.getLimit() < RowBounds.NO_ROW_LIMIT
        || rowBounds.getOffset() > RowBounds.NO_ROW_OFFSET)) {
      throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely constrained by RowBounds.");
    }
  }

  //
  // HANDLE RESULT SETS
  //

  protected void cleanUpAfterHandlingResultSet() {
    super.cleanUpAfterHandlingResultSet();
    globalRowValueCache.clear();
  }

  //
  // HANDLE ROWS
  //

  protected void handleRowValues(ResultSet rs, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds, ResultColumnCache resultColumnCache) throws SQLException {
    final DefaultResultContext resultContext = new DefaultResultContext();
    skipRows(rs, rowBounds);
    while (shouldProcessMoreRows(rs, resultContext, rowBounds)) {
      final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rs, resultMap, null);
      final CacheKey rowKey = createRowKey(discriminatedResultMap, rs, null, resultColumnCache, null);
      final boolean knownValue = globalRowValueCache.containsKey(rowKey);
      Object rowValue = getRowValue(rs, discriminatedResultMap, rowKey, resultColumnCache);
      if (!knownValue) {
        resultContext.nextResultObject(rowValue);
        resultHandler.handleResult(resultContext);
      }
    }
  }

  //
  // GET VALUE FROM ROW
  //

  protected Object getRowValue(ResultSet rs, ResultMap resultMap, CacheKey rowKey, ResultColumnCache resultColumnCache) throws SQLException {
    return getRowValue(rs, resultMap, rowKey, null, resultColumnCache);
  }

  protected Object getRowValue(ResultSet rs, ResultMap resultMap, CacheKey rowKey, String columnPrefix, ResultColumnCache resultColumnCache) throws SQLException {
    if (globalRowValueCache.containsKey(rowKey)) {
      final Object resultObject = globalRowValueCache.get(rowKey);
      final MetaObject metaObject = configuration.newMetaObject(resultObject);
      applyNestedResultMappings(rs, resultMap, metaObject, columnPrefix, resultColumnCache, rowKey);
      return resultObject;
    } else {
      List<String> mappedColumnNames = new ArrayList<String>();
      List<String> unmappedColumnNames = new ArrayList<String>();
      final ResultLoaderMap lazyLoader = instantiateResultLoaderMap();
      Object resultObject = createResultObject(rs, resultMap, lazyLoader, columnPrefix, resultColumnCache);
      if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
        final MetaObject metaObject = configuration.newMetaObject(resultObject);
        mappedColumnNames = resultColumnCache.getMappedColumnNames(resultMap, columnPrefix);
        unmappedColumnNames = resultColumnCache.getUnmappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = resultMap.getConstructorResultMappings().size() > 0;
        if (AutoMappingBehavior.FULL.equals(configuration.getAutoMappingBehavior())) {
          foundValues = applyAutomaticMappings(rs, unmappedColumnNames, metaObject, columnPrefix, resultColumnCache) || foundValues;
        }
        foundValues = applyPropertyMappings(rs, resultMap, mappedColumnNames, metaObject, lazyLoader, columnPrefix) || foundValues;
        foundValues = applyNestedResultMappings(rs, resultMap, metaObject, columnPrefix, resultColumnCache, rowKey) || foundValues;
        resultObject = foundValues ? resultObject : null;
      }
      if (rowKey != CacheKey.NULL_CACHE_KEY) {
        globalRowValueCache.put(rowKey, resultObject);
      }
      return resultObject;
    }
  }


  //
  // NESTED RESULT MAP (JOIN MAPPING)
  //

  @SuppressWarnings("unchecked")
  private boolean applyNestedResultMappings(ResultSet rs, ResultMap resultMap, MetaObject metaObject, String parentPrefix, ResultColumnCache resultColumnCache, CacheKey parentRowKey) {
    boolean foundValues = false;
    for (ResultMapping resultMapping : resultMap.getPropertyResultMappings()) {
      final String nestedResultMapId = resultMapping.getNestedResultMapId();
      if (nestedResultMapId != null) {
        try {
          final StringBuilder columnPrefixBuilder = new StringBuilder();
          if (parentPrefix != null) columnPrefixBuilder.append(parentPrefix);
          if (resultMapping.getColumnPrefix()!= null) columnPrefixBuilder.append(resultMapping.getColumnPrefix());
          final String columnPrefix = columnPrefixBuilder.length() == 0 ? null : columnPrefixBuilder.toString().toUpperCase(Locale.ENGLISH);
          final ResultMap nestedResultMap = getNestedResultMap(rs, nestedResultMapId, columnPrefix);
          final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);
          final CacheKey rowKey = createRowKey(nestedResultMap, rs, columnPrefix, resultColumnCache, parentRowKey);
          final Set<CacheKey> localRowValueCache = getRowValueCache(parentRowKey);
          final boolean knownValue = localRowValueCache.contains(rowKey);
          localRowValueCache.add(rowKey);
          Object rowValue = getRowValue(rs, nestedResultMap, rowKey, columnPrefix, resultColumnCache);
          Set<String> notNullColumns = resultMapping.getNotNullColumns();
          boolean anyNotNullColumnIsNotNull = true;
          if (notNullColumns != null && !notNullColumns.isEmpty()) {
            anyNotNullColumnIsNotNull = false;
            for (String column: notNullColumns) {
              rs.getObject(prependPrefix(column, columnPrefix));
              if (!rs.wasNull()) {
                anyNotNullColumnIsNotNull = true;
              }
            }
          }

          if (rowValue != null && anyNotNullColumnIsNotNull) {
            if (collectionProperty != null && collectionProperty instanceof Collection) {
              if (!knownValue) {
                ((Collection<Object>) collectionProperty).add(rowValue);
              }
            } else {
              metaObject.setValue(resultMapping.getProperty(), rowValue);
            }
            foundValues = true;
          }

        } catch (Exception e) {
          throw new ExecutorException("Error getting nested result map values for '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
        }
      }
    }
    return foundValues;
  }

  private Set<CacheKey> getRowValueCache(CacheKey rowKey) {
    Set<CacheKey> cache = localRowValueCaches.get(rowKey);
    if (cache == null) {
      cache = new HashSet<CacheKey>();
      localRowValueCaches.put(rowKey, cache);
    }
    return cache;
  }

  private Object instantiateCollectionPropertyIfAppropriate(ResultMapping resultMapping, MetaObject metaObject) {
    final String propertyName = resultMapping.getProperty();
    Class<?> type = resultMapping.getJavaType();
    Object propertyValue = metaObject.getValue(propertyName);
    if (propertyValue == null) {
      if (type == null) {
        type = metaObject.getSetterType(propertyName);
      }
      try {
        if (Collection.class.isAssignableFrom(type)) {
          propertyValue = objectFactory.create(type);
          metaObject.setValue(propertyName, propertyValue);
        }
      } catch (Exception e) {
        throw new ExecutorException("Error instantiating collection property for result '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
      }
    }
    return propertyValue;
  }

  private ResultMap getNestedResultMap(ResultSet rs, String nestedResultMapId, String columnPrefix) throws SQLException {
    ResultMap nestedResultMap = configuration.getResultMap(nestedResultMapId);
    nestedResultMap = resolveDiscriminatedResultMap(rs, nestedResultMap, columnPrefix);
    return nestedResultMap;
  }

  //
  // UNIQUE RESULT KEY
  //

  private CacheKey createRowKey(ResultMap resultMap, ResultSet rs, String columnPrefix, ResultColumnCache resultColumnCache, CacheKey parent) throws SQLException {
    final CacheKey cacheKey = new CacheKey();
    List<ResultMapping> resultMappings = getResultMappingsForRowKey(resultMap);
    cacheKey.update(resultMap.getId());
    if (parent != null) cacheKey.update(parent);
    if (resultMappings.size() == 0) {
      if (Map.class.isAssignableFrom(resultMap.getType())) {
        createRowKeyForMap(rs, cacheKey);
      } else {
        createRowKeyForUnmappedProperties(resultMap, rs, cacheKey, columnPrefix, resultColumnCache);
      }
    } else {
      createRowKeyForMappedProperties(rs, cacheKey, resultMappings, columnPrefix);
    }
    if (cacheKey.getUpdateCount() < 2) {
      return CacheKey.NULL_CACHE_KEY;
    }
    return cacheKey;
  }

  private List<ResultMapping> getResultMappingsForRowKey(ResultMap resultMap) {
    List<ResultMapping> resultMappings = resultMap.getIdResultMappings();
    if (resultMappings.size() == 0) {
      resultMappings = resultMap.getPropertyResultMappings();
    }
    return resultMappings;
  }

  private void createRowKeyForMappedProperties(ResultSet rs, CacheKey cacheKey, List<ResultMapping> resultMappings, String columnPrefix) throws SQLException {
    for (ResultMapping resultMapping : resultMappings) {
      if (resultMapping.getNestedResultMapId() != null) {
        final ResultMap myResultMap = configuration.getResultMap(resultMapping.getNestedResultMapId());
        createRowKeyForMappedProperties(rs, cacheKey, myResultMap.getConstructorResultMappings(),
            prependPrefix(resultMapping.getColumnPrefix(), columnPrefix));
      } else if (resultMapping.getNestedQueryId() == null) {
        final String column = prependPrefix(resultMapping.getColumn(), columnPrefix);
        final TypeHandler<?> th = resultMapping.getTypeHandler();
        if (column != null && resultSetHasColumn(rs, column)) { // issue #114
          final Object value = th.getResult(rs, column);
          if (value != null) {
            cacheKey.update(column);
            cacheKey.update(value);
          }
        }
      }
    }
  }

  private void createRowKeyForUnmappedProperties(ResultMap resultMap, ResultSet rs, CacheKey cacheKey, String columnPrefix, ResultColumnCache resultColumnCache) throws SQLException {
    final MetaClass metaType = MetaClass.forClass(resultMap.getType());
    List<String> unmappedColumnNames = resultColumnCache.getUnmappedColumnNames(resultMap, columnPrefix);
    for (String column : unmappedColumnNames) {
      String property = column;
      if (columnPrefix != null && columnPrefix.length() > 0) {
        // When columnPrefix is specified,
        // ignore columns without the prefix.
        if (column.startsWith(columnPrefix)) {
          property = column.substring(columnPrefix.length());
        } else {
          continue;
        }
      }
      if (metaType.findProperty(property) != null) {
        String value = rs.getString(column);
        if (value != null) {
          cacheKey.update(column);
          cacheKey.update(value);
        }
      }
    }
  }

  private void createRowKeyForMap(ResultSet rs, CacheKey cacheKey) throws SQLException {
    final ResultSetMetaData rsmd = rs.getMetaData();
    final int columnCount = rsmd.getColumnCount();
    for (int i = 1; i <= columnCount; i++) {
      final String columnName = configuration.isUseColumnLabel() ? rsmd.getColumnLabel(i) : rsmd.getColumnName(i);
      final String value = rs.getString(columnName);
      if (value != null) {
        cacheKey.update(columnName);
        cacheKey.update(value);
      }
    }
  }

  protected boolean resultSetHasColumn(final ResultSet rs, final String column) {
    try {
      final ResultSetMetaData rsmd = rs.getMetaData();
      final int columnCount = rsmd.getColumnCount();
      for (int i = 1; i <= columnCount; i++) {
        final String label = rsmd.getColumnLabel(i);
        if (column.equalsIgnoreCase(label)) {
          return true;
        }
      }
    } catch (final SQLException e) {
      // ignore
    }
    return false;
  }

}
