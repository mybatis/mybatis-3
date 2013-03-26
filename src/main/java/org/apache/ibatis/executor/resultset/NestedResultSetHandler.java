/*
 *    Copyright 2009-2013 The MyBatis Team
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
import java.sql.SQLException;
import java.util.HashMap;
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

  private final Map<CacheKey, Object> objectCache = new HashMap<CacheKey, Object>();
  private final Map<CacheKey, Object> ancestorCache = new HashMap<CacheKey, Object>();

  public NestedResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql, RowBounds rowBounds) {
    super(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    if (configuration.isSafeRowBoundsEnabled()) {
      ensureNoRowBounds(rowBounds);
    }
  }

  private void ensureNoRowBounds(RowBounds rowBounds) {
    if (rowBounds != null && (rowBounds.getLimit() < RowBounds.NO_ROW_LIMIT || rowBounds.getOffset() > RowBounds.NO_ROW_OFFSET)) {
      throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely constrained by RowBounds. "
          + "Use safeRowBoundsEnabled=false setting to bypass this check.");
    }
  }

  //
  // HANDLE RESULT SETS
  //

  @Override
  protected void handleResultSet(ResultSet rs, ResultMap resultMap, List<Object> multipleResults, ResultColumnCache resultColumnCache) throws SQLException {
    if (resultHandler != null && configuration.isSafeResultHandlerEnabled() && !mappedStatement.isResultOrdered()) {
      throw new ExecutorException("Mapped Statements with nested result mappings cannot be safely used with a custom ResultHandler. "
          + "Use safeResultHandlerEnabled=false setting to bypass this check " 
          + "or ensure your statement returns ordered data and set resultOrdered=true on it.");
    }
    super.handleResultSet(rs, resultMap, multipleResults, resultColumnCache);
  }

  @Override
  protected void cleanUpAfterHandlingResultSet() {
    super.cleanUpAfterHandlingResultSet();
    objectCache.clear();
  }

  //
  // HANDLE ROWS
  //

  @Override
  protected void handleRowValues(ResultSet rs, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds, ResultColumnCache resultColumnCache) throws SQLException {
    final DefaultResultContext resultContext = new DefaultResultContext();
    skipRows(rs, rowBounds);
    Object rowValue = null;
    while (shouldProcessMoreRows(rs, resultContext, rowBounds)) {
      final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rs, resultMap, null);
      final CacheKey rowKey = createRowKey(discriminatedResultMap, rs, null, resultColumnCache);
      Object partialObject = objectCache.get(rowKey);
      if (mappedStatement.isResultOrdered()) { // issue #577 && #542
        if (partialObject == null && rowValue != null) {
          objectCache.clear();
          callResultHandler(resultHandler, resultContext, rowValue);
        }
        rowValue = getRowValue(rs, discriminatedResultMap, rowKey, rowKey, null, resultColumnCache, partialObject);
      } else {
        rowValue = getRowValue(rs, discriminatedResultMap, rowKey, rowKey, null, resultColumnCache, partialObject);
        if (partialObject == null) {
          callResultHandler(resultHandler, resultContext, rowValue);
        }
      }
    }
    if (rowValue != null && mappedStatement.isResultOrdered()) {
      callResultHandler(resultHandler, resultContext, rowValue);
    }
  }
  
  //
  // GET VALUE FROM ROW
  //

  protected Object getRowValue(ResultSet rs, ResultMap resultMap, CacheKey combinedKey, CacheKey absoluteKey, String columnPrefix, ResultColumnCache resultColumnCache, Object partialObject) throws SQLException {
    Object resultObject = partialObject;
    if (resultObject != null) {
      final MetaObject metaObject = configuration.newMetaObject(resultObject);
      ancestorCache.put(absoluteKey, resultObject);
      applyNestedResultMappings(rs, resultMap, metaObject, columnPrefix, resultColumnCache, combinedKey, false);
      ancestorCache.remove(absoluteKey);
    } else {
      final ResultLoaderMap lazyLoader = instantiateResultLoaderMap();
      resultObject = createResultObject(rs, resultMap, lazyLoader, columnPrefix, resultColumnCache);
      if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
        final MetaObject metaObject = configuration.newMetaObject(resultObject);
        boolean foundValues = resultMap.getConstructorResultMappings().size() > 0;
        if (shouldApplyAutomaticMappings(resultMap, AutoMappingBehavior.FULL.equals(configuration.getAutoMappingBehavior()))) {
          final List<String> unmappedColumnNames = resultColumnCache.getUnmappedColumnNames(resultMap, columnPrefix);
          foundValues = applyAutomaticMappings(rs, unmappedColumnNames, metaObject, columnPrefix, resultColumnCache) || foundValues;
        }
        final List<String> mappedColumnNames = resultColumnCache.getMappedColumnNames(resultMap, columnPrefix);
        foundValues = applyPropertyMappings(rs, resultMap, mappedColumnNames, metaObject, lazyLoader, columnPrefix) || foundValues;
        ancestorCache.put(absoluteKey, resultObject);
        foundValues = applyNestedResultMappings(rs, resultMap, metaObject, columnPrefix, resultColumnCache, combinedKey, true) || foundValues;
        ancestorCache.remove(absoluteKey);
        foundValues = (lazyLoader != null && lazyLoader.size() > 0) || foundValues;
        resultObject = foundValues ? resultObject : null;
      }
      if (combinedKey != CacheKey.NULL_CACHE_KEY) objectCache.put(combinedKey, resultObject);
    }
    return resultObject;
  }

  //
  // NESTED RESULT MAP (JOIN MAPPING)
  //

  private boolean applyNestedResultMappings(ResultSet rs, ResultMap resultMap, MetaObject metaObject, String parentPrefix, ResultColumnCache resultColumnCache, CacheKey parentRowKey, boolean newObject) {
    boolean foundValues = false;
    for (ResultMapping resultMapping : resultMap.getPropertyResultMappings()) {
      final String nestedResultMapId = resultMapping.getNestedResultMapId();
      if (nestedResultMapId != null) {
        try {
          final String columnPrefix = getColumnPrefix(parentPrefix, resultMapping);
          final ResultMap nestedResultMap = getNestedResultMap(rs, nestedResultMapId, columnPrefix);
          final CacheKey rowKey = createRowKey(nestedResultMap, rs, columnPrefix, resultColumnCache);
          final Object ancestorObject = ancestorCache.get(rowKey);
          if (ancestorObject != null) { 
            if (newObject) metaObject.setValue(resultMapping.getProperty(), ancestorObject);
          } else {
            final CacheKey combinedKey = combineKeys(rowKey, parentRowKey);            
            Object rowValue = objectCache.get(combinedKey);
            boolean knownValue = (rowValue != null);
            final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);            
            if (anyNotNullColumnHasValue(resultMapping, columnPrefix, rs)) {
              rowValue = getRowValue(rs, nestedResultMap, combinedKey, rowKey, columnPrefix, resultColumnCache, rowValue);
              if (rowValue != null && !knownValue) {
                if (collectionProperty != null) {
                  final MetaObject targetMetaObject = configuration.newMetaObject(collectionProperty);
                  targetMetaObject.add(rowValue);
                } else {
                  metaObject.setValue(resultMapping.getProperty(), rowValue);
                }
                foundValues = true;
              }
            }
          }
        } catch (SQLException e) {
          throw new ExecutorException("Error getting nested result map values for '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
        }
      }
    }
    return foundValues;
  }
  
  private String getColumnPrefix(String parentPrefix, ResultMapping resultMapping) {
    final StringBuilder columnPrefixBuilder = new StringBuilder();
    if (parentPrefix != null) columnPrefixBuilder.append(parentPrefix);
    if (resultMapping.getColumnPrefix() != null) columnPrefixBuilder.append(resultMapping.getColumnPrefix());
    final String columnPrefix = columnPrefixBuilder.length() == 0 ? null : columnPrefixBuilder.toString().toUpperCase(Locale.ENGLISH);
    return columnPrefix;
  }

  private boolean anyNotNullColumnHasValue(ResultMapping resultMapping, String columnPrefix, ResultSet rs) throws SQLException {
    Set<String> notNullColumns = resultMapping.getNotNullColumns();
    boolean anyNotNullColumnHasValue = true;
    if (notNullColumns != null && !notNullColumns.isEmpty()) {
      anyNotNullColumnHasValue = false;
      for (String column: notNullColumns) {
        rs.getObject(prependPrefix(column, columnPrefix));
        if (!rs.wasNull()) {
          anyNotNullColumnHasValue = true;
          break;
        }
      }
    }
    return anyNotNullColumnHasValue;
  }

  private Object instantiateCollectionPropertyIfAppropriate(ResultMapping resultMapping, MetaObject metaObject) {
    final String propertyName = resultMapping.getProperty();
    Object propertyValue = metaObject.getValue(propertyName);
    if (propertyValue == null) {
      Class<?> type = resultMapping.getJavaType();
      if (type == null) {
        type = metaObject.getSetterType(propertyName);
      }
      try {
        if (objectFactory.isCollection(type)) {
          propertyValue = objectFactory.create(type);
          metaObject.setValue(propertyName, propertyValue);
          return propertyValue;
        }
      } catch (Exception e) {
        throw new ExecutorException("Error instantiating collection property for result '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
      }
    } else if (objectFactory.isCollection(propertyValue.getClass())) {
      return propertyValue;
    }
    return null;
  }

  private ResultMap getNestedResultMap(ResultSet rs, String nestedResultMapId, String columnPrefix) throws SQLException {
    ResultMap nestedResultMap = configuration.getResultMap(nestedResultMapId);
    nestedResultMap = resolveDiscriminatedResultMap(rs, nestedResultMap, columnPrefix);
    return nestedResultMap;
  }

  //
  // UNIQUE RESULT KEY
  //

  private CacheKey createRowKey(ResultMap resultMap, ResultSet rs, String columnPrefix, ResultColumnCache resultColumnCache) throws SQLException {
    final CacheKey cacheKey = new CacheKey();
    cacheKey.update(resultMap.getId());
    List<ResultMapping> resultMappings = getResultMappingsForRowKey(resultMap);
    if (resultMappings.size() == 0) {
      if (Map.class.isAssignableFrom(resultMap.getType())) {
        createRowKeyForMap(rs, cacheKey, resultColumnCache);
      } else {
        createRowKeyForUnmappedProperties(resultMap, rs, cacheKey, columnPrefix, resultColumnCache);
      }
    } else {
      createRowKeyForMappedProperties(resultMap, rs, cacheKey, resultMappings, columnPrefix, resultColumnCache);
    }
    return cacheKey;
  }

  private CacheKey combineKeys(CacheKey rowKey, CacheKey parentRowKey) {
    if (rowKey.getUpdateCount() > 1 && parentRowKey.getUpdateCount() > 1) {
      CacheKey combinedKey;
      try {
        combinedKey = rowKey.clone();
      } catch (CloneNotSupportedException e) {
        throw new ExecutorException("Error cloning cache key.  Cause: " + e, e);
      }
      combinedKey.update(parentRowKey);
      return combinedKey;
    }
    return CacheKey.NULL_CACHE_KEY;
  }

  private List<ResultMapping> getResultMappingsForRowKey(ResultMap resultMap) {
    List<ResultMapping> resultMappings = resultMap.getIdResultMappings();
    if (resultMappings.size() == 0) {
      resultMappings = resultMap.getPropertyResultMappings();
    }
    return resultMappings;
  }

  private void createRowKeyForMappedProperties(
      ResultMap resultMap, 
      ResultSet rs, 
      CacheKey cacheKey, 
      List<ResultMapping> resultMappings, 
      String columnPrefix,
      ResultColumnCache resultColumnCache) throws SQLException {
    for (ResultMapping resultMapping : resultMappings) {
      if (resultMapping.getNestedResultMapId() != null) {
        final ResultMap nestedResultMap = configuration.getResultMap(resultMapping.getNestedResultMapId());
        createRowKeyForMappedProperties(nestedResultMap, rs, cacheKey, nestedResultMap.getConstructorResultMappings(),
            prependPrefix(resultMapping.getColumnPrefix(), columnPrefix), resultColumnCache);
      } else if (resultMapping.getNestedQueryId() == null) {
        final String columnName=resultMapping.getColumn();
        final String column = prependPrefix(columnName, columnPrefix);
        List<String> mappedColumnNames = resultColumnCache.getMappedColumnNames(resultMap, columnPrefix);
        if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) { // Issue #114
          TypeHandler<?> th = resultMapping.getTypeHandler();
          if(th==null){
             final Class<?> resultType=resultMap.getType();
             //handle bean property so no prefix needed in columnName
             th=resultColumnCache.getTypeHandler(resultType, columnName);
          }            
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
      if (metaType.findProperty(property, configuration.isMapUnderscoreToCamelCase()) != null) {
        String value = rs.getString(column);
        if (value != null) {
          cacheKey.update(column);
          cacheKey.update(value);
        }
      }
    }
  }

  private void createRowKeyForMap(ResultSet rs, CacheKey cacheKey, ResultColumnCache resultColumnCache) throws SQLException {
    List<String> columnNames = resultColumnCache.getColumnNames();
    for (String columnName : columnNames) {
      final String value = rs.getString(columnName);
      if (value != null) {
        cacheKey.update(columnName);
        cacheKey.update(value);
      }
    }
  }

}
