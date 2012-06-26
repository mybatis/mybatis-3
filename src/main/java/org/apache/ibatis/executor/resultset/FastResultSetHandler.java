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

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
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

  public FastResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql, RowBounds rowBounds) {
    this.executor = executor;
    this.configuration = mappedStatement.getConfiguration();
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;
    this.parameterHandler = parameterHandler;
    this.boundSql = boundSql;
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();
    this.resultHandler = resultHandler;
    this.proxyFactory = configuration.newProxyFactory();
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
          if (typeHandler == null) {
            throw new ExecutorException("Type handler was null on parameter mapping for property '" + parameterMapping.getProperty() + "'.  " +
                "It was either not specified and/or could not be found for the javaType / jdbcType combination specified.");
          }
          metaParam.setValue(parameterMapping.getProperty(), typeHandler.getResult(cs, i + 1));
        }
      }
    }
  }

  protected void handleRefCursorOutputParameter(ResultSet rs, ParameterMapping parameterMapping, MetaObject metaParam) throws SQLException {
    final String resultMapId = parameterMapping.getResultMapId();
    if (resultMapId != null) {
      final ResultMap resultMap = configuration.getResultMap(resultMapId);
      final DefaultResultHandler resultHandler = new DefaultResultHandler(objectFactory);
      final ResultColumnCache resultColumnCache = new ResultColumnCache(rs.getMetaData(), configuration);
      handleRowValues(rs, resultMap, resultHandler, new RowBounds(), resultColumnCache);
      metaParam.setValue(parameterMapping.getProperty(), resultHandler.getResultList());
    } else {
      throw new ExecutorException("Parameter requires ResultMap for output types of java.sql.ResultSet");
    }
    rs.close();
  }

  //
  // HANDLE RESULT SETS
  //

  public List<Object> handleResultSets(Statement stmt) throws SQLException {
    final List<Object> multipleResults = new ArrayList<Object>();
    final List<ResultMap> resultMaps = mappedStatement.getResultMaps();
    int resultMapCount = resultMaps.size();
    int resultSetCount = 0;
    ResultSet rs = stmt.getResultSet();

    while (rs == null) {
      // move forward to get the first resultset in case the driver
      // doesn't return the resultset as the first result (HSQLDB 2.1)
      if (stmt.getMoreResults()) {
        rs = stmt.getResultSet();
      } else {
        if (stmt.getUpdateCount() == -1) {
          // no more results.  Must be no resultset
          break;
        }
      }
    }

    validateResultMapsCount(rs, resultMapCount);
    while (rs != null && resultMapCount > resultSetCount) {
      final ResultMap resultMap = resultMaps.get(resultSetCount);
      ResultColumnCache resultColumnCache = new ResultColumnCache(rs.getMetaData(), configuration);
      handleResultSet(rs, resultMap, multipleResults, resultColumnCache);
      rs = getNextResultSet(stmt);
      cleanUpAfterHandlingResultSet();
      resultSetCount++;
    }
    return collapseSingleResultList(multipleResults);
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

  protected void validateResultMapsCount(ResultSet rs, int resultMapCount) {
    if (rs != null && resultMapCount < 1) {
      throw new ExecutorException(
          "A query was run and no Result Maps were found for the Mapped Statement '"
              + mappedStatement.getId()
              + "'.  It's likely that neither a Result Type nor a Result Map was specified.");
    }
  }

  protected void handleResultSet(ResultSet rs, ResultMap resultMap, List<Object> multipleResults, ResultColumnCache resultColumnCache) throws SQLException {
    try {
      if (resultHandler == null) {
        DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
        handleRowValues(rs, resultMap, defaultResultHandler, rowBounds, resultColumnCache);
        multipleResults.add(defaultResultHandler.getResultList());
      } else {
        handleRowValues(rs, resultMap, resultHandler, rowBounds, resultColumnCache);
      }
    } finally {
      closeResultSet(rs); // issue #228 (close resultsets)
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

  protected void handleRowValues(ResultSet rs, ResultMap resultMap, ResultHandler resultHandler, RowBounds rowBounds, ResultColumnCache resultColumnCache) throws SQLException {
    final DefaultResultContext resultContext = new DefaultResultContext();
    skipRows(rs, rowBounds);
    while (shouldProcessMoreRows(rs, resultContext, rowBounds)) {
      final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(rs, resultMap, null);
      Object rowValue = getRowValue(rs, discriminatedResultMap, null, resultColumnCache);
      resultContext.nextResultObject(rowValue);
      resultHandler.handleResult(resultContext);
    }
  }

  protected boolean shouldProcessMoreRows(ResultSet rs, ResultContext context, RowBounds rowBounds) throws SQLException {
    return rs.next() && context.getResultCount() < rowBounds.getLimit() && !context.isStopped();
  }

  protected void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
    if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
      if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
        rs.absolute(rowBounds.getOffset());
      }
    } else {
      for (int i = 0; i < rowBounds.getOffset(); i++) rs.next();
    }
  }

  protected ResultSet getNextResultSet(Statement stmt) throws SQLException {
    // Making this method tolerant of bad JDBC drivers
    try {
      if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
        // Crazy Standard JDBC way of determining if there are more results
        if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
          return stmt.getResultSet();
        }
      }
    } catch (Exception e) {
      // Intentionally ignored.
    }
    return null;
  }

  //
  // GET VALUE FROM ROW
  //

  protected Object getRowValue(ResultSet rs, ResultMap resultMap, CacheKey rowKey, ResultColumnCache resultColumnCache) throws SQLException {
    final ResultLoaderMap lazyLoader = instantiateResultLoaderMap();
    Object resultObject = createResultObject(rs, resultMap, lazyLoader, null, resultColumnCache);
    if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
      final MetaObject metaObject = configuration.newMetaObject(resultObject);
      boolean foundValues = resultMap.getConstructorResultMappings().size() > 0;
      if (shouldApplyAutomaticMappings(resultMap, !AutoMappingBehavior.NONE.equals(configuration.getAutoMappingBehavior()))) {
        final List<String> unmappedColumnNames = resultColumnCache.getUnmappedColumnNames(resultMap, null);
        foundValues = applyAutomaticMappings(rs, unmappedColumnNames, metaObject, null, resultColumnCache) || foundValues;
      }
      final List<String> mappedColumnNames = resultColumnCache.getMappedColumnNames(resultMap, null);
      foundValues = applyPropertyMappings(rs, resultMap, mappedColumnNames, metaObject, lazyLoader, null) || foundValues;
      foundValues = (lazyLoader != null && lazyLoader.size() > 0) || foundValues;
      resultObject = foundValues ? resultObject : null;
      return resultObject;
    }
    return resultObject;
  }

  protected boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean def) {
    if (resultMap.getAutoMapping() != null) return resultMap.getAutoMapping();
    return def;
  }
  
  protected ResultLoaderMap instantiateResultLoaderMap() {
    if (configuration.isLazyLoadingEnabled()) {
      return new ResultLoaderMap();
    } else {
      return null;
    }
  }

  //
  // PROPERTY MAPPINGS
  //

  protected boolean applyPropertyMappings(ResultSet rs, ResultMap resultMap, List<String> mappedColumnNames, MetaObject metaObject, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
    boolean foundValues = false;
    final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
    for (ResultMapping propertyMapping : propertyMappings) {
      final String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
      if (propertyMapping.isCompositeResult() || (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH)))) {
        Object value = getPropertyMappingValue(rs, metaObject, propertyMapping, lazyLoader, columnPrefix);
        if (value != null) {
          final String property = propertyMapping.getProperty(); // issue #541 make property optional
          if (property != null) {
            metaObject.setValue(property, value);
            foundValues = true;
          }
        }
      }
    }
    return foundValues;
  }

  protected Object getPropertyMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
    final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
    if (propertyMapping.getNestedQueryId() != null) {
      return getNestedQueryMappingValue(rs, metaResultObject, propertyMapping, lazyLoader, columnPrefix);
    } else if (typeHandler != null) {
      final String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
      return typeHandler.getResult(rs, column);
    }
    return null;
  }

  protected boolean applyAutomaticMappings(ResultSet rs, List<String> unmappedColumnNames, MetaObject metaObject, String columnPrefix, ResultColumnCache resultColumnCache) throws SQLException {
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
      if (property != null) {
        final Class<?> propertyType = metaObject.getSetterType(property);
        if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
          final TypeHandler<?> typeHandler = resultColumnCache.getTypeHandler(propertyType, columnName);
          final Object value = typeHandler.getResult(rs, columnName);
          if (value != null) {
            metaObject.setValue(property, value);
            foundValues = true;
          }
        }
      }
    }
    return foundValues;
  }

  //
  // INSTANTIATION & CONSTRUCTOR MAPPING
  //

  protected Object createResultObject(ResultSet rs, ResultMap resultMap, ResultLoaderMap lazyLoader, String columnPrefix, ResultColumnCache resultColumnCache) throws SQLException {
    final List<Class<?>> constructorArgTypes = new ArrayList<Class<?>>();
    final List<Object> constructorArgs = new ArrayList<Object>();
    final Object resultObject = createResultObject(rs, resultMap, constructorArgTypes, constructorArgs, columnPrefix, resultColumnCache);
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

  protected Object createResultObject(ResultSet rs, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix, ResultColumnCache resultColumnCache)
      throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
    if (typeHandlerRegistry.hasTypeHandler(resultType)) {
      return createPrimitiveResultObject(rs, resultMap, columnPrefix, resultColumnCache);
    } else if (constructorMappings.size() > 0) {
      return createParameterizedResultObject(rs, resultType, constructorMappings, constructorArgTypes, constructorArgs, columnPrefix, resultColumnCache);
    } else {
      return objectFactory.create(resultType);
    }
  }

  protected Object createParameterizedResultObject(ResultSet rs, Class<?> resultType,
                                                   List<ResultMapping> constructorMappings, 
                                                   List<Class<?>> constructorArgTypes, 
                                                   List<Object> constructorArgs, 
                                                   String columnPrefix,
                                                   ResultColumnCache resultColumnCache) throws SQLException {
    boolean foundValues = false;
    for (ResultMapping constructorMapping : constructorMappings) {
      final Class<?> parameterType = constructorMapping.getJavaType();
      final String column = constructorMapping.getColumn();
      final Object value;
      // check for nested query
      if (constructorMapping.getNestedQueryId() != null) {
        value = getNestedQueryConstructorValue(rs, constructorMapping, columnPrefix);
      } else if (constructorMapping.getNestedResultMapId() != null) {
        final ResultMap resultMap = configuration.getResultMap(constructorMapping.getNestedResultMapId());
        final ResultLoaderMap lazyLoader = instantiateResultLoaderMap();
        value = createResultObject(rs, resultMap, lazyLoader, columnPrefix, resultColumnCache);
      } else {
        // get simple result
        final TypeHandler<?> typeHandler = constructorMapping.getTypeHandler();
        if (typeHandler == null) { // avoid NPE issue #475
          throw new ExecutorException("Type handler was null on constructor parameter for column '" + column + "'.  " +
              "It was either not specified and/or could not be found for the javaType / jdbcType combination specified.");
        }
        value = typeHandler.getResult(rs, prependPrefix(column, columnPrefix));
      }
      constructorArgTypes.add(parameterType);
      constructorArgs.add(value);
      foundValues = value != null || foundValues;
    }
    return foundValues ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
  }

  protected Object createPrimitiveResultObject(ResultSet rs, ResultMap resultMap, String columnPrefix, ResultColumnCache resultColumnCache) throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final String columnName;
    if (resultMap.getResultMappings().size() > 0) {
      final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
      final ResultMapping mapping = resultMappingList.get(0);
      columnName = prependPrefix(mapping.getColumn(), columnPrefix);
    } else {
      columnName = resultColumnCache.getColumnNames().get(0);
    }
    final TypeHandler<?> typeHandler = resultColumnCache.getTypeHandler(resultType, columnName);
    return typeHandler.getResult(rs, columnName);
  }

  //
  // NESTED QUERY
  //

  protected Object getNestedQueryConstructorValue(ResultSet rs, ResultMapping constructorMapping, String columnPrefix) throws SQLException {
    final String nestedQueryId = constructorMapping.getNestedQueryId();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
    final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, constructorMapping, nestedQueryParameterType, columnPrefix);
    Object value = null;
    if (nestedQueryParameterObject != null) {
      final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
      final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, constructorMapping.getJavaType(), key, nestedBoundSql);
      value = resultLoader.loadResult();
    }
    return value;
  }

  protected Object getNestedQueryMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping, ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
    final String nestedQueryId = propertyMapping.getNestedQueryId();
    final String property = propertyMapping.getProperty();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
    final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, propertyMapping, nestedQueryParameterType, columnPrefix);
    Object value = null;
    if (nestedQueryParameterObject != null) {
      final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT, nestedBoundSql);
      if (executor.isCached(nestedQuery, key)) {
        executor.deferLoad(nestedQuery, metaResultObject, property, key);
      } else {
        final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery, nestedQueryParameterObject, propertyMapping.getJavaType(), key, nestedBoundSql);
        if (configuration.isLazyLoadingEnabled()) {
          lazyLoader.addLoader(property, metaResultObject, resultLoader);
        } else {
          value = resultLoader.loadResult();
        }
      }
    }
    return value;
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
    if (typeHandler != null) {
      return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
    } else {
      throw new ExecutorException("No type handler could be found to map the property '" + resultMapping.getProperty() + "' to the column '" + resultMapping.getColumn() + "'.  One or both of the types, or the combination of types is not supported.");
    }
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

  protected static class ResultColumnCache {

    private final TypeHandlerRegistry typeHandlerRegistry;
    private final List<String> columnNames = new ArrayList<String>();
    private final List<String> classNames = new ArrayList<String>();
    private final List<JdbcType> jdbcTypes = new ArrayList<JdbcType>();
    private final Map<String, Map<Class<?>, TypeHandler<?>>> typeHandlerMap = new HashMap<String, Map<Class<?>, TypeHandler<?>>>();
    private Map<String, List<String>> mappedColumnNamesMap = new HashMap<String, List<String>>();
    private Map<String, List<String>> unMappedColumnNamesMap = new HashMap<String, List<String>>();

    protected ResultColumnCache(ResultSetMetaData metaData, Configuration configuration) throws SQLException {
      super();
      this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
      final int columnCount = metaData.getColumnCount();
      for (int i = 1; i <= columnCount; i++) {
        columnNames.add(configuration.isUseColumnLabel() ? metaData.getColumnLabel(i) : metaData.getColumnName(i));
        jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(i)));
        classNames.add(metaData.getColumnClassName(i));
      }
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
