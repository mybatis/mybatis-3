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

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.ResultExtractor;
import org.apache.ibatis.executor.loader.ProxyFactory;
import org.apache.ibatis.executor.loader.ResultLoader;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.ObjectTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;

public class FastResultSetHandler implements ResultSetHandler {

  protected final Executor executor;
  protected final Configuration configuration;
  protected final MappedStatement mappedStatement;
  protected final RowBounds rowBounds;
  protected final ParameterHandler parameterHandler;
  protected final ResultHandler resultHandler;
  protected final BoundSql boundSql;
  protected final TypeHandlerRegistry typeHandlerRegistry;
  protected final ObjectFactory objectFactory;
  protected final ProxyFactory proxyFactory;
  protected final ResultExtractor resultExtractor;
  protected final Object OMIT = new Object();
  protected final Map<Integer, ResultMapping> pendingRelationships = new HashMap<Integer, ResultMapping>();
  protected final Map<CacheKey, DeferLoad> interResultSetCache = new HashMap<CacheKey, DeferLoad>();

  protected static class DeferLoad {
    public MetaObject metaObject;
    public ResultMapping propertyMapping;
  }

  public FastResultSetHandler(Executor executor, 
                              MappedStatement mappedStatement, 
                              ParameterHandler parameterHandler, 
                              ResultHandler resultHandler, 
                              BoundSql boundSql,
                              RowBounds rowBounds) {
    this.executor = executor;
    this.configuration = mappedStatement.getConfiguration();
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;
    this.parameterHandler = parameterHandler;
    this.boundSql = boundSql;
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();
    this.resultHandler = resultHandler;
    this.proxyFactory = configuration.getProxyFactory();
    this.resultExtractor = new ResultExtractor(configuration, objectFactory);
  }

  //
  // HANDLE OUTPUT PARAMETER
  //

  public void handleOutputParameters(CallableStatement cs) throws SQLException {
    final Object parameterObject = parameterHandler.getParameterObject();
    final MetaObject metaParam = configuration.newMetaObject(parameterObject);
    final List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    for (int i = 0; i < parameterMappings.size(); i++) {
      final ParameterMapping parameterMapping = parameterMappings.get(i);
      if (parameterMapping.getMode() == ParameterMode.OUT || parameterMapping.getMode() == ParameterMode.INOUT) {
        if (ResultSet.class.equals(parameterMapping.getJavaType())) {
          handleRefCursorOutputParameter((ResultSet) cs.getObject(i + 1), parameterMapping, metaParam);
        } else {
          final TypeHandler<?> typeHandler = parameterMapping.getTypeHandler();
          metaParam.setValue(parameterMapping.getProperty(), typeHandler.getResult(cs, i + 1));
        }
      }
    }
  }

  protected void handleRefCursorOutputParameter(ResultSet rs, ParameterMapping parameterMapping, MetaObject metaParam) throws SQLException {
    try {
      final String resultMapId = parameterMapping.getResultMapId();
      final ResultMap resultMap = configuration.getResultMap(resultMapId);
      final DefaultResultHandler resultHandler = new DefaultResultHandler(objectFactory);
      final ResultSetWrapper rsw = new ResultSetWrapper(rs, configuration);
      handleRowValues(rsw, resultMap, resultHandler, new RowBounds(), null);
      metaParam.setValue(parameterMapping.getProperty(), resultHandler.getResultList());
    } finally {
      closeResultSet(rs); // issue #228 (close resultsets)
    }
  }

  //
  // HANDLE RESULT SETS
  //

  public List<Object> handleResultSets(Statement stmt) throws SQLException {
    final List<Object> multipleResults = new ArrayList<Object>();

    int resultSetCount = 0;
    ResultSetWrapper rsw = getFirstResultSet(stmt);

    List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    int resultMapCount = resultMaps.size();
    validateResultMapsCount(rsw, resultMapCount);
    while (rsw != null && resultMapCount > resultSetCount) {
      ResultMap resultMap = resultMaps.get(resultSetCount);
      handleResultSet(rsw, resultMap, multipleResults, null);
      rsw = getNextResultSet(stmt);
      cleanUpAfterHandlingResultSet();
      resultSetCount++;
    }

    while (rsw != null) {
      ResultMapping parentMapping = pendingRelationships.get(resultSetCount);
      if (parentMapping != null) {
        String nestedResultMapId = parentMapping.getNestedResultMapId();
        ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
        handleResultSet(rsw, resultMap, null, parentMapping);
      }
      rsw = getNextResultSet(stmt);
      cleanUpAfterHandlingResultSet();
      resultSetCount++;
    }

    return collapseSingleResultList(multipleResults);
  }

  private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
    ResultSet rs = stmt.getResultSet();
    while (rs == null) {
      // move forward to get the first resultset in case the driver
      // doesn't return the resultset as the first result (HSQLDB 2.1)
      if (stmt.getMoreResults()) {
        rs = stmt.getResultSet();
      } else {
        if (stmt.getUpdateCount() == -1) {
          // no more results. Must be no resultset
          break;
        }
      }
    }
    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
  }

  protected ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
    // Making this method tolerant of bad JDBC drivers
    try {
      if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
        // Crazy Standard JDBC way of determining if there are more results
        if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
          ResultSet rs = stmt.getResultSet();
          return rs != null ? new ResultSetWrapper(rs, configuration) : null;
        }
      }
    } catch (Exception e) {
      // Intentionally ignored.
    }
    return null;
  }

  protected void closeResultSet(ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException e) {
      // ignore
    }
  }

  protected void cleanUpAfterHandlingResultSet() {
  }

  protected void validateResultMapsCount(ResultSetWrapper rsw, int resultMapCount) {
    if (rsw != null && resultMapCount < 1) {
      throw new ExecutorException("A query was run and no Result Maps were found for the Mapped Statement '" + mappedStatement.getId()
          + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
    }
  }

  protected void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping parentMapping) throws SQLException {
    try {
      if (parentMapping != null) {
        handleRowValues(rsw, resultMap, null, rowBounds, parentMapping);
      } else {
        if (resultHandler == null) {
          DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
          handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
          multipleResults.add(defaultResultHandler.getResultList());
        } else {
          handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
        }
      }
    } finally {
      closeResultSet(rsw.getResultSet()); // issue #228 (close resultsets)
    }
  }

  protected List<Object> collapseSingleResultList(List<Object> multipleResults) {
    if (multipleResults.size() == 1) {
      @SuppressWarnings("unchecked")
      List<Object> returned = (List<Object>) multipleResults.get(0);
      return returned;
    }
    return multipleResults;
  }

  //
  // HANDLE ROWS
  //

  protected void handleRowValues(ResultSetWrapper rsw, 
                                 ResultMap resultMap, 
                                 ResultHandler resultHandler, 
                                 RowBounds rowBounds, 
                                 ResultMapping parentMapping) throws SQLException {
    DefaultResultContext resultContext = new DefaultResultContext();
    skipRows(rsw.getResultSet(), rowBounds);
    while (shouldProcessMoreRows(rsw.getResultSet(), resultContext, rowBounds)) {
      ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rsw.getResultSet(), resultMap, null);
      Object rowValue = getRowValue(rsw, discriminatedResultMap, null);
      storeObject(resultHandler, resultContext, rowValue, parentMapping, rsw.getResultSet());
    }
  }

  protected void storeObject(ResultHandler resultHandler, 
                             DefaultResultContext resultContext, 
                             Object rowValue, 
                             ResultMapping parentMapping, 
                             ResultSet rs) throws SQLException {
    if (parentMapping != null) {
      linkToParent(rs, parentMapping, rowValue);
    } else {
      callResultHandler(resultHandler, resultContext, rowValue);
    }
  }

  protected void callResultHandler(ResultHandler resultHandler, DefaultResultContext resultContext, Object rowValue) {
    resultContext.nextResultObject(rowValue);
    resultHandler.handleResult(resultContext);
  }

  protected void linkToParent(ResultSet rs, ResultMapping parentMapping, Object rowValue) throws SQLException {
    CacheKey parentKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getForeignColumn());
    DeferLoad parent = interResultSetCache.get(parentKey);
    final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(parent.propertyMapping, parent.metaObject);
    if (rowValue != null) {
      if (collectionProperty != null) {
        final MetaObject targetMetaObject = configuration.newMetaObject(collectionProperty);
        targetMetaObject.add(rowValue);
      } else {
        parent.metaObject.setValue(parent.propertyMapping.getProperty(), rowValue);
      }
    }
  }

  protected Object instantiateCollectionPropertyIfAppropriate(ResultMapping resultMapping, MetaObject metaObject) {
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

  protected boolean shouldProcessMoreRows(ResultSet rs, ResultContext context, RowBounds rowBounds) throws SQLException {
    return !context.isStopped() && rs.next() && context.getResultCount() < rowBounds.getLimit();
  }

  protected void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
    if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
      if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
        rs.absolute(rowBounds.getOffset());
      }
    } else {
      for (int i = 0; i < rowBounds.getOffset(); i++)
        rs.next();
    }
  }

  //
  // GET VALUE FROM ROW
  //

  protected Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap, CacheKey rowKey) throws SQLException {
    final ResultLoaderMap lazyLoader = instantiateResultLoaderMap();
    Object resultObject = createResultObject(rsw, resultMap, lazyLoader, null);
    if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
      final MetaObject metaObject = configuration.newMetaObject(resultObject);
      boolean foundValues = resultMap.getConstructorResultMappings().size() > 0;
      if (shouldApplyAutomaticMappings(resultMap, !AutoMappingBehavior.NONE.equals(configuration.getAutoMappingBehavior()))) {
        final List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, null);
        foundValues = applyAutomaticMappings(rsw, unmappedColumnNames, metaObject, null) || foundValues;
      }
      final List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, null);
      foundValues = applyPropertyMappings(rsw.getResultSet(), resultMap, mappedColumnNames, metaObject, lazyLoader, null) || foundValues;
      foundValues = (lazyLoader != null && lazyLoader.size() > 0) || foundValues;
      resultObject = foundValues ? resultObject : null;
      return resultObject;
    }
    return resultObject;
  }

  protected boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean def) {
    return resultMap.getAutoMapping() != null ? resultMap.getAutoMapping() : def;
  }

  protected ResultLoaderMap instantiateResultLoaderMap() {
    return configuration.isLazyLoadingEnabled() ? new ResultLoaderMap() : null;
  }

  //
  // PROPERTY MAPPINGS
  //

  protected boolean applyPropertyMappings(ResultSet rs, 
                                          ResultMap resultMap, 
                                          List<String> mappedColumnNames, 
                                          MetaObject metaObject, 
                                          ResultLoaderMap lazyLoader, 
                                          String columnPrefix)
                                          throws SQLException {
    boolean foundValues = false;
    final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
    for (ResultMapping propertyMapping : propertyMappings) {
      final String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
      if (propertyMapping.isCompositeResult() 
          || (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) 
          || propertyMapping.getResultSetIndex() != 0) {
        Object value = getPropertyMappingValue(rs, metaObject, propertyMapping, lazyLoader, columnPrefix);
        final String property = propertyMapping.getProperty(); // issue #541 make property optional
        if (value != OMIT && property != null && (value != null || isCallSettersOnNulls(metaObject.getSetterType(property)))) { // issue #377, call setter on nulls
          metaObject.setValue(property, value);
          foundValues = (value != null) || foundValues;
        }
      }
    }
    return foundValues;
  }

  protected boolean isCallSettersOnNulls(Class<?> propertyType) {
    return configuration.isCallSettersOnNulls() && !propertyType.isPrimitive();
  }

  protected Object getPropertyMappingValue(ResultSet rs, 
                                           MetaObject metaResultObject, 
                                           ResultMapping propertyMapping, 
                                           ResultLoaderMap lazyLoader, 
                                           String columnPrefix)
                                           throws SQLException {
    if (propertyMapping.getNestedQueryId() != null) {
      return getNestedQueryMappingValue(rs, metaResultObject, propertyMapping, lazyLoader, columnPrefix);
    } else if (propertyMapping.getResultSetIndex() != 0) {
      addPendingChildRelationship(rs, metaResultObject, propertyMapping);
      return OMIT;
    } else if (propertyMapping.getNestedResultMapId() != null) {
      // the user added a column attribute to a nested result map, ignore it
      return OMIT;
    } else {
      final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
      final String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
      return typeHandler.getResult(rs, column);
    }
  }

  protected boolean applyAutomaticMappings(ResultSetWrapper rsw, List<String> unmappedColumnNames, MetaObject metaObject, String columnPrefix) throws SQLException {
    boolean foundValues = false;
    for (String columnName : unmappedColumnNames) {
      String propertyName = columnName;
      if (columnPrefix != null && columnPrefix.length() > 0) {
        // When columnPrefix is specified,
        // ignore columns without the prefix.
        if (columnName.startsWith(columnPrefix)) {
          propertyName = columnName.substring(columnPrefix.length());
        } else {
          continue;
        }
      }
      final String property = metaObject.findProperty(propertyName, configuration.isMapUnderscoreToCamelCase());
      if (property != null && metaObject.hasSetter(property)) {
        final Class<?> propertyType = metaObject.getSetterType(property);
        if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
          final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
          final Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
          if (value != null || isCallSettersOnNulls(propertyType)) { // issue #377, call setter on nulls
            metaObject.setValue(property, value);
            foundValues = (value != null) || foundValues;
          }
        }
      }
    }
    return foundValues;
  }

  // MULTIPLE RESULT SETS
  
  private void addPendingChildRelationship(ResultSet rs, MetaObject metaResultObject, ResultMapping parentMapping) throws SQLException {
    CacheKey cacheKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn());
    DeferLoad deferLoad = new DeferLoad();
    deferLoad.metaObject = metaResultObject;
    deferLoad.propertyMapping = parentMapping;
    interResultSetCache.put(cacheKey, deferLoad);
    if (!pendingRelationships.containsKey(parentMapping.getResultSetIndex())) {
      pendingRelationships.put(parentMapping.getResultSetIndex(), parentMapping);
    }
  }

  private CacheKey createKeyForMultipleResults(ResultSet rs, ResultMapping resultMapping, String columns) throws SQLException {
    CacheKey cacheKey = new CacheKey();
    cacheKey.update(resultMapping);
    if (columns != null) {
      for (String column : columns.split(",")) {
        Object value = rs.getString(column);
        if (value != null) {
          cacheKey.update(column);
          cacheKey.update(value);
        }
      }
    }
    return cacheKey;
  }
  
  //
  // INSTANTIATION & CONSTRUCTOR MAPPING
  //

  protected Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
    final List<Class<?>> constructorArgTypes = new ArrayList<Class<?>>();
    final List<Object> constructorArgs = new ArrayList<Object>();
    final Object resultObject = createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
    if (resultObject != null && configuration.isLazyLoadingEnabled() && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
      final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
      for (ResultMapping propertyMapping : propertyMappings) {
        if (propertyMapping.getNestedQueryId() != null) { // issue #109 (avoid creating proxies for leaf objects)
          return proxyFactory.createProxy(resultObject, lazyLoader, configuration, objectFactory, constructorArgTypes, constructorArgs);
        }
      }
    }
    return resultObject;
  }

  protected Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix)
      throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
    if (typeHandlerRegistry.hasTypeHandler(resultType)) {
      return createPrimitiveResultObject(rsw, resultMap, columnPrefix);
    } else if (constructorMappings.size() > 0) {
      return createParameterizedResultObject(rsw, resultType, constructorMappings, constructorArgTypes, constructorArgs, columnPrefix);
    } else {
      return objectFactory.create(resultType);
    }
  }

  protected Object createParameterizedResultObject(ResultSetWrapper rsw, Class<?> resultType, List<ResultMapping> constructorMappings, List<Class<?>> constructorArgTypes,
      List<Object> constructorArgs, String columnPrefix) throws SQLException {
    boolean foundValues = false;
    for (ResultMapping constructorMapping : constructorMappings) {
      final Class<?> parameterType = constructorMapping.getJavaType();
      final String column = constructorMapping.getColumn();
      final Object value;
      if (constructorMapping.getNestedQueryId() != null) {
        value = getNestedQueryConstructorValue(rsw.getResultSet(), constructorMapping, columnPrefix);
      } else if (constructorMapping.getNestedResultMapId() != null) {
        final ResultMap resultMap = configuration.getResultMap(constructorMapping.getNestedResultMapId());
        final ResultLoaderMap lazyLoader = instantiateResultLoaderMap();
        value = createResultObject(rsw, resultMap, lazyLoader, columnPrefix);
      } else {
        final TypeHandler<?> typeHandler = constructorMapping.getTypeHandler();
        value = typeHandler.getResult(rsw.getResultSet(), prependPrefix(column, columnPrefix));
      }
      constructorArgTypes.add(parameterType);
      constructorArgs.add(value);
      foundValues = value != null || foundValues;
    }
    return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
  }

  protected Object createPrimitiveResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final String columnName;
    if (resultMap.getResultMappings().size() > 0) {
      final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
      final ResultMapping mapping = resultMappingList.get(0);
      columnName = prependPrefix(mapping.getColumn(), columnPrefix);
    } else {
      columnName = rsw.getColumnNames().get(0);
    }
    final TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName);
    return typeHandler.getResult(rsw.getResultSet(), columnName);
  }

  //
  // NESTED QUERY
  //

  @SuppressWarnings("unchecked")
  protected Object getNestedQueryConstructorValue(ResultSet rs, ResultMapping constructorMapping, String columnPrefix) throws SQLException {
    final String nestedQueryId = constructorMapping.getNestedQueryId();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
    final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, constructorMapping, nestedQueryParameterType, columnPrefix);
    Object value = null;
    if (nestedQueryParameterObject != null) {
      final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
      final Class<?> targetType = constructorMapping.getJavaType();
      final Object nestedQueryCacheObject = getNestedQueryCacheObject(nestedQuery, key);
      if (nestedQueryCacheObject != null && nestedQueryCacheObject instanceof List) {
        value = resultExtractor.extractObjectFromList((List<Object>) nestedQueryCacheObject, targetType);
      } else {
        final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, targetType, key, nestedBoundSql);
        value = resultLoader.loadResult();
      }
    }
    return value;
  }

  @SuppressWarnings("unchecked")
  protected Object getNestedQueryMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix)
      throws SQLException {
    final String nestedQueryId = propertyMapping.getNestedQueryId();
    final String property = propertyMapping.getProperty();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
    final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, propertyMapping, nestedQueryParameterType, columnPrefix);
    Object value = OMIT;
    if (nestedQueryParameterObject != null) {
      final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
      final Class<?> targetType = propertyMapping.getJavaType();
      final Object nestedQueryCacheObject = getNestedQueryCacheObject(nestedQuery, key);
      if (nestedQueryCacheObject != null && nestedQueryCacheObject instanceof List) {
        value = resultExtractor.extractObjectFromList((List<Object>) nestedQueryCacheObject, targetType);
      } else if (executor.isCached(nestedQuery, key)) {
        executor.deferLoad(nestedQuery, metaResultObject, property, key, targetType);
      } else {
        final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, targetType, key, nestedBoundSql);
        if (configuration.isLazyLoadingEnabled()) {
          lazyLoader.addLoader(property, metaResultObject, resultLoader);
        } else {
          value = resultLoader.loadResult();
        }
      }
    }
    return value;
  }

  private Object getNestedQueryCacheObject(MappedStatement nestedQuery, CacheKey key) {
    final Cache nestedQueryCache = nestedQuery.getCache();
    return nestedQueryCache != null ? nestedQueryCache.getObject(key) : null;
  }

  protected Object prepareParameterForNestedQuery(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
    if (resultMapping.isCompositeResult()) {
      return prepareCompositeKeyParameter(rs, resultMapping, parameterType, columnPrefix);
    } else {
      return prepareSimpleKeyParameter(rs, resultMapping, parameterType, columnPrefix);
    }
  }

  protected Object prepareSimpleKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
    final TypeHandler<?> typeHandler;
    if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
      typeHandler = typeHandlerRegistry.getTypeHandler(parameterType);
    } else {
      typeHandler = typeHandlerRegistry.getUnknownTypeHandler();
    }
    return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
  }

  protected Object prepareCompositeKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType, String columnPrefix) throws SQLException {
    final Object parameterObject = instantiateParameterObject(parameterType);
    final MetaObject metaObject = configuration.newMetaObject(parameterObject);
    boolean foundValues = false;
    for (ResultMapping innerResultMapping : resultMapping.getComposites()) {
      final Class<?> propType = metaObject.getSetterType(innerResultMapping.getProperty());
      final TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(propType);
      final Object propValue = typeHandler.getResult(rs, prependPrefix(innerResultMapping.getColumn(), columnPrefix));
      if (propValue != null) { // issue #353 & #560 do not execute nested query if key is null
        metaObject.setValue(innerResultMapping.getProperty(), propValue);
        foundValues = true;
      }
    }
    return foundValues ? parameterObject : null;
  }

  protected Object instantiateParameterObject(Class<?> parameterType) {
    if (parameterType == null) {
      return new HashMap<Object, Object>();
    } else {
      return objectFactory.create(parameterType);
    }
  }

  //
  // DISCRIMINATOR
  //

  public ResultMap resolveDiscriminatedResultMap(ResultSet rs, ResultMap resultMap, String columnPrefix) throws SQLException {
    Set<String> pastDiscriminators = new HashSet<String>();
    Discriminator discriminator = resultMap.getDiscriminator();
    while (discriminator != null) {
      final Object value = getDiscriminatorValue(rs, discriminator, columnPrefix);
      final String discriminatedMapId = discriminator.getMapIdFor(String.valueOf(value));
      if (configuration.hasResultMap(discriminatedMapId)) {
        resultMap = configuration.getResultMap(discriminatedMapId);
        Discriminator lastDiscriminator = discriminator;
        discriminator = resultMap.getDiscriminator();
        if (discriminator == lastDiscriminator || !pastDiscriminators.add(discriminatedMapId)) {
          break;
        }
      } else {
        break;
      }
    }
    return resultMap;
  }

  protected Object getDiscriminatorValue(ResultSet rs, Discriminator discriminator, String columnPrefix) throws SQLException {
    final ResultMapping resultMapping = discriminator.getResultMapping();
    final TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
    return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
  }

  protected static Set<String> prependPrefixes(Set<String> columnNames, String prefix) {
    if (columnNames == null || columnNames.isEmpty() || prefix == null || prefix.length() == 0) {
      return columnNames;
    }
    final Set<String> prefixed = new HashSet<String>();
    for (String columnName : columnNames) {
      prefixed.add(prependPrefix(columnName, prefix));
    }
    return prefixed;
  }

  protected static String prependPrefix(String columnName, String prefix) {
    if (columnName == null || columnName.length() == 0) {
      return columnName;
    }
    if (prefix == null || prefix.length() == 0) {
      return columnName;
    }
    return (prefix + columnName);
  }

  // RESULT SET HANDLING
  
  protected static class ResultSetWrapper {

    private final ResultSet resultSet;
    private final TypeHandlerRegistry typeHandlerRegistry;
    private final List<String> columnNames = new ArrayList<String>();
    private final List<String> classNames = new ArrayList<String>();
    private final List<JdbcType> jdbcTypes = new ArrayList<JdbcType>();
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<String, Map<Class<?>, TypeHandler<?>>>();
    private Map<String, List<String>> mappedColumnNamesMap = new HashMap<String, List<String>>();
    private Map<String, List<String>> unMappedColumnNamesMap = new HashMap<String, List<String>>();

    protected ResultSetWrapper(ResultSet rs, Configuration configuration) throws SQLException {
      super();
      this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
      this.resultSet = rs;
      final ResultSetMetaData metaData = rs.getMetaData();
      final int columnCount = metaData.getColumnCount();
      for (int i = 1; i <= columnCount; i++) {
        columnNames.add(configuration.isUseColumnLabel() ? metaData.getColumnLabel(i) : metaData.getColumnName(i));
        jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
        classNames.add(metaData.getColumnClassName(i));
      }
    }

    public ResultSet getResultSet() {
      return resultSet;
    }

    protected List<String> getColumnNames() {
      return this.columnNames;
    }

    protected JdbcType getJdbcType(String columnName) {
      final int index = columnNames.indexOf(columnName);
      return jdbcTypes.get(index);
    }

    protected TypeHandler<?> getTypeHandler(Class<?> propertyType, String columnName) {
      TypeHandler<?> handler = null;
      Map<Class<?>, TypeHandler<?>> columnHandlers = typeHandlerMap.get(columnName);
      if (columnHandlers == null) {
        columnHandlers = new HashMap<Class<?>, TypeHandler<?>>();
        typeHandlerMap.put(columnName, columnHandlers);
      } else {
        handler = columnHandlers.get(propertyType);
      }
      if (handler == null) {
        handler = typeHandlerRegistry.getTypeHandler(propertyType);
        // Replicate logic of UnknownTypeHandler#resolveTypeHandler
        // See issue #59 comment 10
        if (handler == null || handler instanceof UnknownTypeHandler) {
          final int index = columnNames.indexOf(columnName);
          final JdbcType jdbcType = jdbcTypes.get(index);
          final Class<?> javaType = resolveClass(classNames.get(index));
          if (javaType != null && jdbcType != null) {
            handler = typeHandlerRegistry.getTypeHandler(javaType, jdbcType);
          } else if (javaType != null) {
            handler = typeHandlerRegistry.getTypeHandler(javaType);
          } else if (jdbcType != null) {
            handler = typeHandlerRegistry.getTypeHandler(jdbcType);
          }
        }
        if (handler == null || handler instanceof UnknownTypeHandler) {
          handler = new ObjectTypeHandler();
        }
        columnHandlers.put(propertyType, handler);
      }
      return handler;
    }

    private Class<?> resolveClass(String className) {
      try {
        final Class<?> clazz = Resources.classForName(className);
        return clazz;
      } catch (ClassNotFoundException e) {
        return null;
      }
    }

    private void loadMappedAndUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
      List<String> mappedColumnNames = new ArrayList<String>();
      List<String> unmappedColumnNames = new ArrayList<String>();
      final String upperColumnPrefix = columnPrefix == null ? null : columnPrefix.toUpperCase(Locale.ENGLISH);
      final Set<String> mappedColumns = prependPrefixes(resultMap.getMappedColumns(), upperColumnPrefix);
      for (String columnName : columnNames) {
        final String upperColumnName = columnName.toUpperCase(Locale.ENGLISH);
        if (mappedColumns.contains(upperColumnName)) {
          mappedColumnNames.add(upperColumnName);
        } else {
          unmappedColumnNames.add(columnName);
        }
      }
      mappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), mappedColumnNames);
      unMappedColumnNamesMap.put(getMapKey(resultMap, columnPrefix), unmappedColumnNames);
    }

    protected List<String> getMappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
      List<String> mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
      if (mappedColumnNames == null) {
        loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
        mappedColumnNames = mappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
      }
      return mappedColumnNames;
    }

    protected List<String> getUnmappedColumnNames(ResultMap resultMap, String columnPrefix) throws SQLException {
      List<String> unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
      if (unMappedColumnNames == null) {
        loadMappedAndUnmappedColumnNames(resultMap, columnPrefix);
        unMappedColumnNames = unMappedColumnNamesMap.get(getMapKey(resultMap, columnPrefix));
      }
      return unMappedColumnNames;
    }

    private String getMapKey(ResultMap resultMap, String columnPrefix) {
      return resultMap.getId() + ":" + columnPrefix;
    }

  }

}
