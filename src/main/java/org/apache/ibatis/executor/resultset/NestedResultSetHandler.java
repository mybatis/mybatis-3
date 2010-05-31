package org.apache.ibatis.executor.resultset;

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
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.type.TypeHandler;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class NestedResultSetHandler extends FastResultSetHandler {

  private final Map<CacheKey, Set<CacheKey>> localRowValueCaches;
  private final Map<CacheKey, Object> globalRowValueCache;

  public NestedResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql, RowBounds rowBounds) {
    super(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
    localRowValueCaches = new HashMap<CacheKey, Set<CacheKey>>();
    globalRowValueCache = new HashMap<CacheKey, Object>();
    ensureNoRowBounds(rowBounds);
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

  protected void handleRowValues(ResultSet rs, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds) throws SQLException {
    final DefaultResultContext resultContext = new DefaultResultContext();
    skipRows(rs, rowBounds);
    while (shouldProcessMoreRows(rs, resultContext, rowBounds)) {
      final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rs, resultMap);
      final CacheKey rowKey = createRowKey(discriminatedResultMap, rs);
      final boolean knownValue = globalRowValueCache.containsKey(rowKey);
      Object rowValue = getRowValue(rs, discriminatedResultMap, rowKey);
      if (!knownValue) {
        resultContext.nextResultObject(rowValue);
        resultHandler.handleResult(resultContext);
      }
    }
  }

  //
  // GET VALUE FROM ROW
  //

  protected Object getRowValue(ResultSet rs, ResultMap resultMap, CacheKey rowKey) throws SQLException {
    if (globalRowValueCache.containsKey(rowKey)) {
      final Object resultObject = globalRowValueCache.get(rowKey);
      final MetaObject metaObject = configuration.newMetaObject(resultObject);
      applyNestedResultMappings(rs, resultMap, metaObject);
      return resultObject;
    } else {
      final List<String> mappedColumnNames = new ArrayList<String>();
      final List<String> unmappedColumnNames = new ArrayList<String>();
      final ResultLoaderMap lazyLoader = instantiateResultLoaderMap();
      Object resultObject = createResultObject(rs, resultMap, lazyLoader);
      if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
        final MetaObject metaObject = configuration.newMetaObject(resultObject);
        loadMappedAndUnmappedColumnNames(rs, resultMap, mappedColumnNames, unmappedColumnNames);
        boolean foundValues = resultMap.getConstructorResultMappings().size() > 0;
        if (AutoMappingBehavior.FULL.equals(configuration.getAutoMappingBehavior())) {
          foundValues = applyAutomaticMappings(rs, unmappedColumnNames, metaObject) || foundValues;
        }
        foundValues = applyPropertyMappings(rs, resultMap, mappedColumnNames, metaObject, lazyLoader) || foundValues;
        foundValues = applyNestedResultMappings(rs, resultMap, metaObject) || foundValues;
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

  private boolean applyNestedResultMappings(ResultSet rs, ResultMap resultMap, MetaObject metaObject) {
    boolean foundValues = false;
    for (ResultMapping resultMapping : resultMap.getPropertyResultMappings()) {
      final String nestedResultMapId = resultMapping.getNestedResultMapId();
      if (nestedResultMapId != null) {
        try {
          final ResultMap nestedResultMap = getNestedResultMap(rs, nestedResultMapId);
          final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);

          final CacheKey parentRowKey = createRowKey(resultMap, rs);
          final CacheKey rowKey = createRowKey(nestedResultMap, rs);
          final Set<CacheKey> localRowValueCache = getRowValueCache(parentRowKey);
          final boolean knownValue = localRowValueCache.contains(rowKey);
          localRowValueCache.add(rowKey);
          Object rowValue = getRowValue(rs, nestedResultMap, rowKey);

          if (rowValue != null) {
            if (collectionProperty != null && collectionProperty instanceof Collection) {
              if (!knownValue) {
                ((Collection) collectionProperty).add(rowValue);
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
    Class type = resultMapping.getJavaType();
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

  private ResultMap getNestedResultMap(ResultSet rs, String nestedResultMapId) throws SQLException {
    ResultMap nestedResultMap = configuration.getResultMap(nestedResultMapId);
    nestedResultMap = resolveDiscriminatedResultMap(rs, nestedResultMap);
    return nestedResultMap;
  }

  //
  // UNIQUE RESULT KEY
  //

  private CacheKey createRowKey(ResultMap resultMap, ResultSet rs) throws SQLException {
    final CacheKey cacheKey = new CacheKey();
    List<ResultMapping> resultMappings = getResultMappingsForRowKey(resultMap);
    cacheKey.update(resultMap.getId());
    if (resultMappings.size() == 0) {
      if (Map.class.isAssignableFrom(resultMap.getType())) {
        createRowKeyForMap(rs, cacheKey);
      } else {
        createRowKeyForUnmappedProperties(resultMap, rs, cacheKey);
      }
    } else {
      createRowKeyForMappedProperties(rs, cacheKey, resultMappings);
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

  private void createRowKeyForMappedProperties(ResultSet rs, CacheKey cacheKey, List<ResultMapping> resultMappings) {
    for (ResultMapping resultMapping : resultMappings) {
      if (resultMapping.getNestedQueryId() == null && resultMapping.getNestedResultMapId() == null) {
        final String column = resultMapping.getColumn();
        final TypeHandler th = resultMapping.getTypeHandler();
        if (column != null) {
          try {
            final Object value = th.getResult(rs, column);
            if (value != null) {
              cacheKey.update(column);
              cacheKey.update(value);
            }
          } catch (Exception e) {
            //ignore
          }
        }
      }
    }
  }

  private void createRowKeyForUnmappedProperties(ResultMap resultMap, ResultSet rs, CacheKey cacheKey) throws SQLException {
    final MetaClass metaType = MetaClass.forClass(resultMap.getType());
    final List<String> mappedColumnNames = new ArrayList<String>();
    final List<String> unmappedColumnNames = new ArrayList<String>();
    loadMappedAndUnmappedColumnNames(rs, resultMap, mappedColumnNames, unmappedColumnNames);
    for (String column : unmappedColumnNames) {
      if (metaType.findProperty(column) != null) {
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

}