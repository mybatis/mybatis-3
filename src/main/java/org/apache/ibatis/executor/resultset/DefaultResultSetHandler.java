/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.resultset;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.ibatis.annotations.AutomapConstructor;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.cursor.defaults.DefaultCursor;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.loader.ResultLoader;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.result.DefaultResultContext;
import org.apache.ibatis.executor.result.DefaultResultHandler;
import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * @author Iwao AVE!
 * @author Kazuki Shimizu
 * @author Willie Scholtz
 */
public class DefaultResultSetHandler implements ResultSetHandler {

  private static final Object DEFERRED = new Object();

  private final Executor executor;
  private final Configuration configuration;
  private final MappedStatement mappedStatement;
  private final RowBounds rowBounds;
  private final ParameterHandler parameterHandler;
  private final ResultHandler<?> resultHandler;
  private final BoundSql boundSql;
  private final TypeHandlerRegistry typeHandlerRegistry;
  private final ObjectFactory objectFactory;
  private final ReflectorFactory reflectorFactory;

  // pending creations property tracker
  private final Map<Object, PendingRelation> pendingPccRelations = new IdentityHashMap<>();

  // nested resultmaps
  private final Map<CacheKey, Object> nestedResultObjects = new HashMap<>();
  private final Map<String, Object> ancestorObjects = new HashMap<>();
  private Object previousRowValue;

  // multiple resultsets
  private final Map<String, ResultMapping> nextResultMaps = new HashMap<>();
  private final Map<CacheKey, List<PendingRelation>> pendingRelations = new HashMap<>();

  // Cached Automappings
  private final Map<String, List<UnMappedColumnAutoMapping>> autoMappingsCache = new HashMap<>();
  private final Map<String, List<String>> constructorAutoMappingColumns = new HashMap<>();

  // temporary marking flag that indicate using constructor mapping (use field to reduce memory usage)
  private boolean useConstructorMappings;

  private static class PendingRelation {
    public MetaObject metaObject;
    public ResultMapping propertyMapping;
  }

  private static class UnMappedColumnAutoMapping {
    private final String column;
    private final String property;
    private final TypeHandler<?> typeHandler;
    private final boolean primitive;

    public UnMappedColumnAutoMapping(String column, String property, TypeHandler<?> typeHandler, boolean primitive) {
      this.column = column;
      this.property = property;
      this.typeHandler = typeHandler;
      this.primitive = primitive;
    }
  }

  public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler,
      ResultHandler<?> resultHandler, BoundSql boundSql, RowBounds rowBounds) {
    this.executor = executor;
    this.configuration = mappedStatement.getConfiguration();
    this.mappedStatement = mappedStatement;
    this.rowBounds = rowBounds;
    this.parameterHandler = parameterHandler;
    this.boundSql = boundSql;
    this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    this.objectFactory = configuration.getObjectFactory();
    this.reflectorFactory = configuration.getReflectorFactory();
    this.resultHandler = resultHandler;
  }

  //
  // HANDLE OUTPUT PARAMETER
  //

  @Override
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

  private void handleRefCursorOutputParameter(ResultSet rs, ParameterMapping parameterMapping, MetaObject metaParam)
      throws SQLException {
    if (rs == null) {
      return;
    }
    try {
      final String resultMapId = parameterMapping.getResultMapId();
      final ResultMap resultMap = configuration.getResultMap(resultMapId);
      final ResultSetWrapper rsw = new ResultSetWrapper(rs, configuration);
      if (this.resultHandler == null) {
        final DefaultResultHandler resultHandler = new DefaultResultHandler(objectFactory);
        handleRowValues(rsw, resultMap, resultHandler, new RowBounds(), null);
        metaParam.setValue(parameterMapping.getProperty(), resultHandler.getResultList());
      } else {
        handleRowValues(rsw, resultMap, resultHandler, new RowBounds(), null);
      }
    } finally {
      // issue #228 (close resultsets)
      closeResultSet(rs);
    }
  }

  //
  // HANDLE RESULT SETS
  //
  @Override
  public List<Object> handleResultSets(Statement stmt) throws SQLException {
    ErrorContext.instance().activity("handling results").object(mappedStatement.getId());

    final List<Object> multipleResults = new ArrayList<>();

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

    String[] resultSets = mappedStatement.getResultSets();
    if (resultSets != null) {
      while (rsw != null && resultSetCount < resultSets.length) {
        ResultMapping parentMapping = nextResultMaps.get(resultSets[resultSetCount]);
        if (parentMapping != null) {
          String nestedResultMapId = parentMapping.getNestedResultMapId();
          ResultMap resultMap = configuration.getResultMap(nestedResultMapId);
          handleResultSet(rsw, resultMap, null, parentMapping);
        }
        rsw = getNextResultSet(stmt);
        cleanUpAfterHandlingResultSet();
        resultSetCount++;
      }
    }

    return collapseSingleResultList(multipleResults);
  }

  @Override
  public <E> Cursor<E> handleCursorResultSets(Statement stmt) throws SQLException {
    ErrorContext.instance().activity("handling cursor results").object(mappedStatement.getId());

    ResultSetWrapper rsw = getFirstResultSet(stmt);

    List<ResultMap> resultMaps = mappedStatement.getResultMaps();

    int resultMapCount = resultMaps.size();
    validateResultMapsCount(rsw, resultMapCount);
    if (resultMapCount != 1) {
      throw new ExecutorException("Cursor results cannot be mapped to multiple resultMaps");
    }

    ResultMap resultMap = resultMaps.get(0);
    return new DefaultCursor<>(this, resultMap, rsw, rowBounds);
  }

  private ResultSetWrapper getFirstResultSet(Statement stmt) throws SQLException {
    ResultSet rs = null;
    SQLException e1 = null;

    try {
      rs = stmt.getResultSet();
    } catch (SQLException e) {
      // Oracle throws ORA-17283 for implicit cursor
      e1 = e;
    }

    try {
      while (rs == null) {
        // move forward to get the first resultset in case the driver
        // doesn't return the resultset as the first result (HSQLDB)
        if (stmt.getMoreResults()) {
          rs = stmt.getResultSet();
        } else if (stmt.getUpdateCount() == -1) {
          // no more results. Must be no resultset
          break;
        }
      }
    } catch (SQLException e) {
      throw e1 != null ? e1 : e;
    }

    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
  }

  private ResultSetWrapper getNextResultSet(Statement stmt) {
    // Making this method tolerant of bad JDBC drivers
    try {
      // We stopped checking DatabaseMetaData#supportsMultipleResultSets()
      // because Oracle driver (incorrectly) returns false

      // Crazy Standard JDBC way of determining if there are more results
      // DO NOT try to 'improve' the condition even if IDE tells you to!
      // It's important that getUpdateCount() is called here.
      if (!(!stmt.getMoreResults() && stmt.getUpdateCount() == -1)) {
        ResultSet rs = stmt.getResultSet();
        if (rs == null) {
          return getNextResultSet(stmt);
        } else {
          return new ResultSetWrapper(rs, configuration);
        }
      }
    } catch (Exception e) {
      // Intentionally ignored.
    }
    return null;
  }

  private void closeResultSet(ResultSet rs) {
    try {
      if (rs != null) {
        rs.close();
      }
    } catch (SQLException e) {
      // ignore
    }
  }

  private void cleanUpAfterHandlingResultSet() {
    nestedResultObjects.clear();
  }

  private void validateResultMapsCount(ResultSetWrapper rsw, int resultMapCount) {
    if (rsw != null && resultMapCount < 1) {
      throw new ExecutorException(
          "A query was run and no Result Maps were found for the Mapped Statement '" + mappedStatement.getId()
              + "'. 'resultType' or 'resultMap' must be specified when there is no corresponding method.");
    }
  }

  private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults,
      ResultMapping parentMapping) throws SQLException {
    try {
      if (parentMapping != null) {
        handleRowValues(rsw, resultMap, null, RowBounds.DEFAULT, parentMapping);
      } else if (resultHandler == null) {
        DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
        handleRowValues(rsw, resultMap, defaultResultHandler, rowBounds, null);
        multipleResults.add(defaultResultHandler.getResultList());
      } else {
        handleRowValues(rsw, resultMap, resultHandler, rowBounds, null);
      }
    } finally {
      // issue #228 (close resultsets)
      closeResultSet(rsw.getResultSet());
    }
  }

  @SuppressWarnings("unchecked")
  private List<Object> collapseSingleResultList(List<Object> multipleResults) {
    return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;
  }

  //
  // HANDLE ROWS FOR SIMPLE RESULTMAP
  //

  public void handleRowValues(ResultSetWrapper rsw, ResultMap resultMap, ResultHandler<?> resultHandler,
      RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
    if (resultMap.hasNestedResultMaps()) {
      ensureNoRowBounds();
      checkResultHandler();
      handleRowValuesForNestedResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
    } else {
      handleRowValuesForSimpleResultMap(rsw, resultMap, resultHandler, rowBounds, parentMapping);
    }
  }

  private void ensureNoRowBounds() {
    if (configuration.isSafeRowBoundsEnabled() && rowBounds != null
        && (rowBounds.getLimit() < RowBounds.NO_ROW_LIMIT || rowBounds.getOffset() > RowBounds.NO_ROW_OFFSET)) {
      throw new ExecutorException(
          "Mapped Statements with nested result mappings cannot be safely constrained by RowBounds. "
              + "Use safeRowBoundsEnabled=false setting to bypass this check.");
    }
  }

  protected void checkResultHandler() {
    if (resultHandler != null && configuration.isSafeResultHandlerEnabled() && !mappedStatement.isResultOrdered()) {
      throw new ExecutorException(
          "Mapped Statements with nested result mappings cannot be safely used with a custom ResultHandler. "
              + "Use safeResultHandlerEnabled=false setting to bypass this check "
              + "or ensure your statement returns ordered data and set resultOrdered=true on it.");
    }
  }

  private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap,
      ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
    final boolean useCollectionConstructorInjection = resultMap.hasResultMapsUsingConstructorCollection();

    DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
    ResultSet resultSet = rsw.getResultSet();
    skipRows(resultSet, rowBounds);
    while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed() && resultSet.next()) {
      ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(resultSet, resultMap, null);
      Object rowValue = getRowValue(rsw, discriminatedResultMap, null, null);
      if (!useCollectionConstructorInjection) {
        storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
      } else {
        if (!(rowValue instanceof PendingConstructorCreation)) {
          throw new ExecutorException("Expected result object to be a pending constructor creation!");
        }

        createAndStorePendingCreation(resultHandler, resultSet, resultContext, (PendingConstructorCreation) rowValue);
      }
    }
  }

  private void storeObject(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext, Object rowValue,
      ResultMapping parentMapping, ResultSet rs) throws SQLException {
    if (parentMapping != null) {
      linkToParents(rs, parentMapping, rowValue);
      return;
    }

    if (pendingPccRelations.containsKey(rowValue)) {
      createPendingConstructorCreations(rowValue);
    }

    callResultHandler(resultHandler, resultContext, rowValue);
  }

  @SuppressWarnings("unchecked" /* because ResultHandler<?> is always ResultHandler<Object> */)
  private void callResultHandler(ResultHandler<?> resultHandler, DefaultResultContext<Object> resultContext,
      Object rowValue) {
    resultContext.nextResultObject(rowValue);
    ((ResultHandler<Object>) resultHandler).handleResult(resultContext);
  }

  private boolean shouldProcessMoreRows(ResultContext<?> context, RowBounds rowBounds) {
    return !context.isStopped() && context.getResultCount() < rowBounds.getLimit();
  }

  private void skipRows(ResultSet rs, RowBounds rowBounds) throws SQLException {
    if (rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
      if (rowBounds.getOffset() != RowBounds.NO_ROW_OFFSET) {
        rs.absolute(rowBounds.getOffset());
      }
    } else {
      for (int i = 0; i < rowBounds.getOffset(); i++) {
        if (!rs.next()) {
          break;
        }
      }
    }
  }

  //
  // GET VALUE FROM ROW FOR SIMPLE RESULT MAP
  //

  private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix, CacheKey parentRowKey)
      throws SQLException {
    final ResultLoaderMap lazyLoader = new ResultLoaderMap();
    Object rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix, parentRowKey);
    if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
      final MetaObject metaObject = configuration.newMetaObject(rowValue);
      boolean foundValues = this.useConstructorMappings;
      if (shouldApplyAutomaticMappings(resultMap, false)) {
        foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues;
      }
      foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix) || foundValues;
      foundValues = lazyLoader.size() > 0 || foundValues;
      rowValue = foundValues || configuration.isReturnInstanceForEmptyRow() ? rowValue : null;
    }

    if (parentRowKey != null) {
      // found a simple object/primitive in pending constructor creation that will need linking later
      final CacheKey rowKey = createRowKey(resultMap, rsw, columnPrefix);
      final CacheKey combinedKey = combineKeys(rowKey, parentRowKey);

      if (combinedKey != CacheKey.NULL_CACHE_KEY) {
        nestedResultObjects.put(combinedKey, rowValue);
      }
    }

    return rowValue;
  }

  //
  // GET VALUE FROM ROW FOR NESTED RESULT MAP
  //

  private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap, CacheKey combinedKey, String columnPrefix,
      Object partialObject) throws SQLException {
    final String resultMapId = resultMap.getId();
    Object rowValue = partialObject;
    if (rowValue != null) {
      final MetaObject metaObject = configuration.newMetaObject(rowValue);
      putAncestor(rowValue, resultMapId);
      applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combinedKey, false);
      ancestorObjects.remove(resultMapId);
    } else {
      final ResultLoaderMap lazyLoader = new ResultLoaderMap();
      rowValue = createResultObject(rsw, resultMap, lazyLoader, columnPrefix, combinedKey);
      if (rowValue != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
        final MetaObject metaObject = configuration.newMetaObject(rowValue);
        boolean foundValues = this.useConstructorMappings;
        if (shouldApplyAutomaticMappings(resultMap, true)) {
          foundValues = applyAutomaticMappings(rsw, resultMap, metaObject, columnPrefix) || foundValues;
        }
        foundValues = applyPropertyMappings(rsw, resultMap, metaObject, lazyLoader, columnPrefix) || foundValues;
        putAncestor(rowValue, resultMapId);
        foundValues = applyNestedResultMappings(rsw, resultMap, metaObject, columnPrefix, combinedKey, true)
            || foundValues;
        ancestorObjects.remove(resultMapId);
        foundValues = lazyLoader.size() > 0 || foundValues;
        rowValue = foundValues || configuration.isReturnInstanceForEmptyRow() ? rowValue : null;
      }
      if (combinedKey != CacheKey.NULL_CACHE_KEY) {
        nestedResultObjects.put(combinedKey, rowValue);
      }
    }
    return rowValue;
  }

  private void putAncestor(Object resultObject, String resultMapId) {
    ancestorObjects.put(resultMapId, resultObject);
  }

  private boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean isNested) {
    if (resultMap.getAutoMapping() != null) {
      return resultMap.getAutoMapping();
    }
    if (isNested) {
      return AutoMappingBehavior.FULL == configuration.getAutoMappingBehavior();
    } else {
      return AutoMappingBehavior.NONE != configuration.getAutoMappingBehavior();
    }
  }

  //
  // PROPERTY MAPPINGS
  //

  private boolean applyPropertyMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject,
      ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
    final Set<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
    boolean foundValues = false;
    final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
    for (ResultMapping propertyMapping : propertyMappings) {
      String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
      if (propertyMapping.getNestedResultMapId() != null && !JdbcType.CURSOR.equals(propertyMapping.getJdbcType())) {
        // the user added a column attribute to a nested result map, ignore it
        column = null;
      }
      if (propertyMapping.isCompositeResult()
          || column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))
          || propertyMapping.getResultSet() != null) {
        Object value = getPropertyMappingValue(rsw.getResultSet(), metaObject, propertyMapping, lazyLoader,
            columnPrefix);
        // issue #541 make property optional
        final String property = propertyMapping.getProperty();
        if (property == null) {
          continue;
        }
        if (value == DEFERRED) {
          foundValues = true;
          continue;
        }
        if (value != null) {
          foundValues = true;
        }
        if (value != null
            || configuration.isCallSettersOnNulls() && !metaObject.getSetterType(property).isPrimitive()) {
          // gcode issue #377, call setter on nulls (value is not 'found')
          metaObject.setValue(property, value);
        }
      }
    }
    return foundValues;
  }

  private Object getPropertyMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping,
      ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
    if (propertyMapping.getNestedQueryId() != null) {
      return getNestedQueryMappingValue(rs, metaResultObject, propertyMapping, lazyLoader, columnPrefix);
    }
    if (JdbcType.CURSOR.equals(propertyMapping.getJdbcType())) {
      List<Object> results = getNestedCursorValue(rs, propertyMapping, columnPrefix);
      linkObjects(metaResultObject, propertyMapping, results.get(0), true);
      return metaResultObject.getValue(propertyMapping.getProperty());
    }
    if (propertyMapping.getResultSet() != null) {
      addPendingChildRelation(rs, metaResultObject, propertyMapping); // TODO is that OK?
      return DEFERRED;
    } else {
      final TypeHandler<?> typeHandler = propertyMapping.getTypeHandler();
      final String column = prependPrefix(propertyMapping.getColumn(), columnPrefix);
      return typeHandler.getResult(rs, column);
    }
  }

  private List<Object> getNestedCursorValue(ResultSet rs, ResultMapping propertyMapping, String parentColumnPrefix)
      throws SQLException {
    final String column = prependPrefix(propertyMapping.getColumn(), parentColumnPrefix);
    ResultMap nestedResultMap = resolveDiscriminatedResultMap(rs,
        configuration.getResultMap(propertyMapping.getNestedResultMapId()),
        getColumnPrefix(parentColumnPrefix, propertyMapping));
    ResultSetWrapper rsw = new ResultSetWrapper(rs.getObject(column, ResultSet.class), configuration);
    List<Object> results = new ArrayList<>();
    handleResultSet(rsw, nestedResultMap, results, null);
    return results;
  }

  private List<UnMappedColumnAutoMapping> createAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap,
      MetaObject metaObject, String columnPrefix) throws SQLException {
    final String mapKey = resultMap.getId() + ":" + columnPrefix;
    List<UnMappedColumnAutoMapping> autoMapping = autoMappingsCache.get(mapKey);
    if (autoMapping == null) {
      autoMapping = new ArrayList<>();
      final List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
      // Remove the entry to release the memory
      List<String> mappedInConstructorAutoMapping = constructorAutoMappingColumns.remove(mapKey);
      if (mappedInConstructorAutoMapping != null) {
        unmappedColumnNames.removeAll(mappedInConstructorAutoMapping);
      }
      for (String columnName : unmappedColumnNames) {
        String propertyName = columnName;
        if (columnPrefix != null && !columnPrefix.isEmpty()) {
          // When columnPrefix is specified,
          // ignore columns without the prefix.
          if (!columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
            continue;
          }
          propertyName = columnName.substring(columnPrefix.length());
        }
        final String property = metaObject.findProperty(propertyName, configuration.isMapUnderscoreToCamelCase());
        if (property != null && metaObject.hasSetter(property)) {
          if (resultMap.getMappedProperties().contains(property)) {
            continue;
          }
          final Class<?> propertyType = metaObject.getSetterType(property);
          if (typeHandlerRegistry.hasTypeHandler(propertyType, rsw.getJdbcType(columnName))) {
            final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
            autoMapping
                .add(new UnMappedColumnAutoMapping(columnName, property, typeHandler, propertyType.isPrimitive()));
          } else {
            configuration.getAutoMappingUnknownColumnBehavior().doAction(mappedStatement, columnName, property,
                propertyType);
          }
        } else {
          configuration.getAutoMappingUnknownColumnBehavior().doAction(mappedStatement, columnName,
              property != null ? property : propertyName, null);
        }
      }
      autoMappingsCache.put(mapKey, autoMapping);
    }
    return autoMapping;
  }

  private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject,
      String columnPrefix) throws SQLException {
    List<UnMappedColumnAutoMapping> autoMapping = createAutomaticMappings(rsw, resultMap, metaObject, columnPrefix);
    boolean foundValues = false;
    if (!autoMapping.isEmpty()) {
      for (UnMappedColumnAutoMapping mapping : autoMapping) {
        final Object value = mapping.typeHandler.getResult(rsw.getResultSet(), mapping.column);
        if (value != null) {
          foundValues = true;
        }
        if (value != null || configuration.isCallSettersOnNulls() && !mapping.primitive) {
          // gcode issue #377, call setter on nulls (value is not 'found')
          metaObject.setValue(mapping.property, value);
        }
      }
    }
    return foundValues;
  }

  // MULTIPLE RESULT SETS

  private void linkToParents(ResultSet rs, ResultMapping parentMapping, Object rowValue) throws SQLException {
    CacheKey parentKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn(),
        parentMapping.getForeignColumn());
    List<PendingRelation> parents = pendingRelations.get(parentKey);
    if (parents != null) {
      for (PendingRelation parent : parents) {
        if (parent != null && rowValue != null) {
          linkObjects(parent.metaObject, parent.propertyMapping, rowValue);
        }
      }
    }
  }

  private void addPendingChildRelation(ResultSet rs, MetaObject metaResultObject, ResultMapping parentMapping)
      throws SQLException {
    CacheKey cacheKey = createKeyForMultipleResults(rs, parentMapping, parentMapping.getColumn(),
        parentMapping.getColumn());
    PendingRelation deferLoad = new PendingRelation();
    deferLoad.metaObject = metaResultObject;
    deferLoad.propertyMapping = parentMapping;
    List<PendingRelation> relations = pendingRelations.computeIfAbsent(cacheKey, k -> new ArrayList<>());
    // issue #255
    relations.add(deferLoad);
    ResultMapping previous = nextResultMaps.get(parentMapping.getResultSet());
    if (previous == null) {
      nextResultMaps.put(parentMapping.getResultSet(), parentMapping);
    } else if (!previous.equals(parentMapping)) {
      throw new ExecutorException("Two different properties are mapped to the same resultSet");
    }
  }

  private CacheKey createKeyForMultipleResults(ResultSet rs, ResultMapping resultMapping, String names, String columns)
      throws SQLException {
    CacheKey cacheKey = new CacheKey();
    cacheKey.update(resultMapping);
    if (columns != null && names != null) {
      String[] columnsArray = columns.split(",");
      String[] namesArray = names.split(",");
      for (int i = 0; i < columnsArray.length; i++) {
        Object value = rs.getString(columnsArray[i]);
        if (value != null) {
          cacheKey.update(namesArray[i]);
          cacheKey.update(value);
        }
      }
    }
    return cacheKey;
  }

  //
  // INSTANTIATION & CONSTRUCTOR MAPPING
  //

  private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, ResultLoaderMap lazyLoader,
      String columnPrefix, CacheKey parentRowKey) throws SQLException {
    this.useConstructorMappings = false; // reset previous mapping result
    final List<Class<?>> constructorArgTypes = new ArrayList<>();
    final List<Object> constructorArgs = new ArrayList<>();

    Object resultObject = createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix,
        parentRowKey);
    if (resultObject != null && !hasTypeHandlerForResultObject(rsw, resultMap.getType())) {
      final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
      for (ResultMapping propertyMapping : propertyMappings) {
        // issue gcode #109 && issue #149
        if (propertyMapping.getNestedQueryId() != null && propertyMapping.isLazy()) {
          resultObject = configuration.getProxyFactory().createProxy(resultObject, lazyLoader, configuration,
              objectFactory, constructorArgTypes, constructorArgs);
          break;
        }
      }

      // (issue #101)
      if (resultMap.hasResultMapsUsingConstructorCollection() && resultObject instanceof PendingConstructorCreation) {
        linkNestedPendingCreations(rsw, resultMap, columnPrefix, parentRowKey,
            (PendingConstructorCreation) resultObject, constructorArgs);
      }
    }

    this.useConstructorMappings = resultObject != null && !constructorArgTypes.isEmpty(); // set current mapping result
    return resultObject;
  }

  private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes,
      List<Object> constructorArgs, String columnPrefix, CacheKey parentRowKey) throws SQLException {

    final Class<?> resultType = resultMap.getType();
    final MetaClass metaType = MetaClass.forClass(resultType, reflectorFactory);
    final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
    if (hasTypeHandlerForResultObject(rsw, resultType)) {
      return createPrimitiveResultObject(rsw, resultMap, columnPrefix);
    }
    if (!constructorMappings.isEmpty()) {
      return createParameterizedResultObject(rsw, resultType, constructorMappings, constructorArgTypes, constructorArgs,
          columnPrefix, resultMap.hasResultMapsUsingConstructorCollection(), parentRowKey);
    } else if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
      return objectFactory.create(resultType);
    } else if (shouldApplyAutomaticMappings(resultMap, false)) {
      return createByConstructorSignature(rsw, resultMap, columnPrefix, resultType, constructorArgTypes,
          constructorArgs);
    }
    throw new ExecutorException("Do not know how to create an instance of " + resultType);
  }

  Object createParameterizedResultObject(ResultSetWrapper rsw, Class<?> resultType,
      List<ResultMapping> constructorMappings, List<Class<?>> constructorArgTypes, List<Object> constructorArgs,
      String columnPrefix, boolean useCollectionConstructorInjection, CacheKey parentRowKey) {
    boolean foundValues = false;

    for (ResultMapping constructorMapping : constructorMappings) {
      final Class<?> parameterType = constructorMapping.getJavaType();
      final String column = constructorMapping.getColumn();
      final Object value;
      try {
        if (constructorMapping.getNestedQueryId() != null) {
          value = getNestedQueryConstructorValue(rsw.getResultSet(), constructorMapping, columnPrefix);
        } else if (JdbcType.CURSOR.equals(constructorMapping.getJdbcType())) {
          List<?> result = (List<?>) getNestedCursorValue(rsw.getResultSet(), constructorMapping, columnPrefix).get(0);
          if (objectFactory.isCollection(parameterType)) {
            MetaObject collection = configuration.newMetaObject(objectFactory.create(parameterType));
            collection.addAll((List<?>) result);
            value = collection.getOriginalObject();
          } else {
            value = toSingleObj(result);
          }
        } else if (constructorMapping.getNestedResultMapId() != null) {
          final String constructorColumnPrefix = getColumnPrefix(columnPrefix, constructorMapping);
          final ResultMap resultMap = resolveDiscriminatedResultMap(rsw.getResultSet(),
              configuration.getResultMap(constructorMapping.getNestedResultMapId()), constructorColumnPrefix);
          value = getRowValue(rsw, resultMap, constructorColumnPrefix,
              useCollectionConstructorInjection ? parentRowKey : null);
        } else {
          final TypeHandler<?> typeHandler = constructorMapping.getTypeHandler();
          value = typeHandler.getResult(rsw.getResultSet(), prependPrefix(column, columnPrefix));
        }
      } catch (ResultMapException | SQLException e) {
        throw new ExecutorException("Could not process result for mapping: " + constructorMapping, e);
      }

      constructorArgTypes.add(parameterType);
      constructorArgs.add(value);

      foundValues = value != null || foundValues;
    }

    if (!foundValues) {
      return null;
    }

    if (useCollectionConstructorInjection) {
      // at least one of the nestedResultMaps contained a collection, we have to defer until later
      return new PendingConstructorCreation(resultType, constructorArgTypes, constructorArgs);
    }

    return objectFactory.create(resultType, constructorArgTypes, constructorArgs);
  }

  private Object createByConstructorSignature(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix,
      Class<?> resultType, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) throws SQLException {
    return applyConstructorAutomapping(rsw, resultMap, columnPrefix, resultType, constructorArgTypes, constructorArgs,
        findConstructorForAutomapping(resultType, rsw).orElseThrow(() -> new ExecutorException(
            "No constructor found in " + resultType.getName() + " matching " + rsw.getClassNames())));
  }

  private Optional<Constructor<?>> findConstructorForAutomapping(final Class<?> resultType, ResultSetWrapper rsw) {
    Constructor<?>[] constructors = resultType.getDeclaredConstructors();
    if (constructors.length == 1) {
      return Optional.of(constructors[0]);
    }
    Optional<Constructor<?>> annotated = Arrays.stream(constructors)
        .filter(x -> x.isAnnotationPresent(AutomapConstructor.class)).reduce((x, y) -> {
          throw new ExecutorException("@AutomapConstructor should be used in only one constructor.");
        });
    if (annotated.isPresent()) {
      return annotated;
    }
    if (configuration.isArgNameBasedConstructorAutoMapping()) {
      // Finding-best-match type implementation is possible,
      // but using @AutomapConstructor seems sufficient.
      throw new ExecutorException(MessageFormat.format(
          "'argNameBasedConstructorAutoMapping' is enabled and the class ''{0}'' has multiple constructors, so @AutomapConstructor must be added to one of the constructors.",
          resultType.getName()));
    } else {
      return Arrays.stream(constructors).filter(x -> findUsableConstructorByArgTypes(x, rsw.getJdbcTypes())).findAny();
    }
  }

  private boolean findUsableConstructorByArgTypes(final Constructor<?> constructor, final List<JdbcType> jdbcTypes) {
    final Class<?>[] parameterTypes = constructor.getParameterTypes();
    if (parameterTypes.length != jdbcTypes.size()) {
      return false;
    }
    for (int i = 0; i < parameterTypes.length; i++) {
      if (!typeHandlerRegistry.hasTypeHandler(parameterTypes[i], jdbcTypes.get(i))) {
        return false;
      }
    }
    return true;
  }

  private Object applyConstructorAutomapping(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix,
      Class<?> resultType, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor)
      throws SQLException {
    boolean foundValues = false;
    if (configuration.isArgNameBasedConstructorAutoMapping()) {
      foundValues = applyArgNameBasedConstructorAutoMapping(rsw, resultMap, columnPrefix, constructorArgTypes,
          constructorArgs, constructor, foundValues);
    } else {
      foundValues = applyColumnOrderBasedConstructorAutomapping(rsw, constructorArgTypes, constructorArgs, constructor,
          foundValues);
    }
    return foundValues || configuration.isReturnInstanceForEmptyRow()
        ? objectFactory.create(resultType, constructorArgTypes, constructorArgs) : null;
  }

  private boolean applyColumnOrderBasedConstructorAutomapping(ResultSetWrapper rsw, List<Class<?>> constructorArgTypes,
      List<Object> constructorArgs, Constructor<?> constructor, boolean foundValues) throws SQLException {
    Class<?>[] parameterTypes = constructor.getParameterTypes();

    if (parameterTypes.length > rsw.getClassNames().size()) {
      throw new ExecutorException(MessageFormat.format(
          "Constructor auto-mapping of ''{0}'' failed. The constructor takes ''{1}'' arguments, but there are only ''{2}'' columns in the result set.",
          constructor, parameterTypes.length, rsw.getClassNames().size()));
    }

    for (int i = 0; i < parameterTypes.length; i++) {
      Class<?> parameterType = parameterTypes[i];
      String columnName = rsw.getColumnNames().get(i);
      TypeHandler<?> typeHandler = rsw.getTypeHandler(parameterType, columnName);
      Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
      constructorArgTypes.add(parameterType);
      constructorArgs.add(value);
      foundValues = value != null || foundValues;
    }
    return foundValues;
  }

  private boolean applyArgNameBasedConstructorAutoMapping(ResultSetWrapper rsw, ResultMap resultMap,
      String columnPrefix, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, Constructor<?> constructor,
      boolean foundValues) throws SQLException {
    List<String> missingArgs = null;
    Parameter[] params = constructor.getParameters();
    for (Parameter param : params) {
      boolean columnNotFound = true;
      Param paramAnno = param.getAnnotation(Param.class);
      String paramName = paramAnno == null ? param.getName() : paramAnno.value();
      for (String columnName : rsw.getColumnNames()) {
        if (columnMatchesParam(columnName, paramName, columnPrefix)) {
          Class<?> paramType = param.getType();
          TypeHandler<?> typeHandler = rsw.getTypeHandler(paramType, columnName);
          Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
          constructorArgTypes.add(paramType);
          constructorArgs.add(value);
          final String mapKey = resultMap.getId() + ":" + columnPrefix;
          if (!autoMappingsCache.containsKey(mapKey)) {
            constructorAutoMappingColumns.computeIfAbsent(mapKey, k -> new ArrayList<>()).add(columnName);
          }
          columnNotFound = false;
          foundValues = value != null || foundValues;
        }
      }
      if (columnNotFound) {
        if (missingArgs == null) {
          missingArgs = new ArrayList<>();
        }
        missingArgs.add(paramName);
      }
    }
    if (foundValues && constructorArgs.size() < params.length) {
      throw new ExecutorException(MessageFormat.format(
          "Constructor auto-mapping of ''{1}'' failed " + "because ''{0}'' were not found in the result set; "
              + "Available columns are ''{2}'' and mapUnderscoreToCamelCase is ''{3}''.",
          missingArgs, constructor, rsw.getColumnNames(), configuration.isMapUnderscoreToCamelCase()));
    }
    return foundValues;
  }

  private boolean columnMatchesParam(String columnName, String paramName, String columnPrefix) {
    if (columnPrefix != null) {
      if (!columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
        return false;
      }
      columnName = columnName.substring(columnPrefix.length());
    }
    return paramName
        .equalsIgnoreCase(configuration.isMapUnderscoreToCamelCase() ? columnName.replace("_", "") : columnName);
  }

  private Object createPrimitiveResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix)
      throws SQLException {
    final Class<?> resultType = resultMap.getType();
    final String columnName;
    if (!resultMap.getResultMappings().isEmpty()) {
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

  private Object getNestedQueryConstructorValue(ResultSet rs, ResultMapping constructorMapping, String columnPrefix)
      throws SQLException {
    final String nestedQueryId = constructorMapping.getNestedQueryId();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
    final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, constructorMapping,
        nestedQueryParameterType, columnPrefix);
    Object value = null;
    if (nestedQueryParameterObject != null) {
      final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT,
          nestedBoundSql);
      final Class<?> targetType = constructorMapping.getJavaType();
      final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery,
          nestedQueryParameterObject, targetType, key, nestedBoundSql);
      value = resultLoader.loadResult();
    }
    return value;
  }

  private Object getNestedQueryMappingValue(ResultSet rs, MetaObject metaResultObject, ResultMapping propertyMapping,
      ResultLoaderMap lazyLoader, String columnPrefix) throws SQLException {
    final String nestedQueryId = propertyMapping.getNestedQueryId();
    final String property = propertyMapping.getProperty();
    final MappedStatement nestedQuery = configuration.getMappedStatement(nestedQueryId);
    final Class<?> nestedQueryParameterType = nestedQuery.getParameterMap().getType();
    final Object nestedQueryParameterObject = prepareParameterForNestedQuery(rs, propertyMapping,
        nestedQueryParameterType, columnPrefix);
    Object value = null;
    if (nestedQueryParameterObject != null) {
      final BoundSql nestedBoundSql = nestedQuery.getBoundSql(nestedQueryParameterObject);
      final CacheKey key = executor.createCacheKey(nestedQuery, nestedQueryParameterObject, RowBounds.DEFAULT,
          nestedBoundSql);
      final Class<?> targetType = propertyMapping.getJavaType();
      if (executor.isCached(nestedQuery, key)) {
        executor.deferLoad(nestedQuery, metaResultObject, property, key, targetType);
        value = DEFERRED;
      } else {
        final ResultLoader resultLoader = new ResultLoader(configuration, executor, nestedQuery,
            nestedQueryParameterObject, targetType, key, nestedBoundSql);
        if (propertyMapping.isLazy()) {
          lazyLoader.addLoader(property, metaResultObject, resultLoader);
          value = DEFERRED;
        } else {
          value = resultLoader.loadResult();
        }
      }
    }
    return value;
  }

  private Object prepareParameterForNestedQuery(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType,
      String columnPrefix) throws SQLException {
    if (resultMapping.isCompositeResult()) {
      return prepareCompositeKeyParameter(rs, resultMapping, parameterType, columnPrefix);
    }
    return prepareSimpleKeyParameter(rs, resultMapping, parameterType, columnPrefix);
  }

  private Object prepareSimpleKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType,
      String columnPrefix) throws SQLException {
    final TypeHandler<?> typeHandler;
    if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
      typeHandler = typeHandlerRegistry.getTypeHandler(parameterType);
    } else {
      typeHandler = typeHandlerRegistry.getUnknownTypeHandler();
    }
    return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
  }

  private Object prepareCompositeKeyParameter(ResultSet rs, ResultMapping resultMapping, Class<?> parameterType,
      String columnPrefix) throws SQLException {
    final Object parameterObject = instantiateParameterObject(parameterType);
    final MetaObject metaObject = configuration.newMetaObject(parameterObject);
    boolean foundValues = false;
    for (ResultMapping innerResultMapping : resultMapping.getComposites()) {
      final Class<?> propType = metaObject.getSetterType(innerResultMapping.getProperty());
      final TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(propType);
      final Object propValue = typeHandler.getResult(rs, prependPrefix(innerResultMapping.getColumn(), columnPrefix));
      // issue #353 & #560 do not execute nested query if key is null
      if (propValue != null) {
        metaObject.setValue(innerResultMapping.getProperty(), propValue);
        foundValues = true;
      }
    }
    return foundValues ? parameterObject : null;
  }

  private Object instantiateParameterObject(Class<?> parameterType) {
    if (parameterType == null) {
      return new HashMap<>();
    }
    if (ParamMap.class.equals(parameterType)) {
      return new HashMap<>(); // issue #649
    } else {
      return objectFactory.create(parameterType);
    }
  }

  //
  // DISCRIMINATOR
  //

  public ResultMap resolveDiscriminatedResultMap(ResultSet rs, ResultMap resultMap, String columnPrefix)
      throws SQLException {
    Set<String> pastDiscriminators = new HashSet<>();
    Discriminator discriminator = resultMap.getDiscriminator();
    while (discriminator != null) {
      final Object value = getDiscriminatorValue(rs, discriminator, columnPrefix);
      final String discriminatedMapId = discriminator.getMapIdFor(String.valueOf(value));
      if (!configuration.hasResultMap(discriminatedMapId)) {
        break;
      }
      resultMap = configuration.getResultMap(discriminatedMapId);
      Discriminator lastDiscriminator = discriminator;
      discriminator = resultMap.getDiscriminator();
      if (discriminator == lastDiscriminator || !pastDiscriminators.add(discriminatedMapId)) {
        break;
      }
    }
    return resultMap;
  }

  private Object getDiscriminatorValue(ResultSet rs, Discriminator discriminator, String columnPrefix)
      throws SQLException {
    final ResultMapping resultMapping = discriminator.getResultMapping();
    final TypeHandler<?> typeHandler = resultMapping.getTypeHandler();
    return typeHandler.getResult(rs, prependPrefix(resultMapping.getColumn(), columnPrefix));
  }

  private String prependPrefix(String columnName, String prefix) {
    if (columnName == null || columnName.length() == 0 || prefix == null || prefix.length() == 0) {
      return columnName;
    }
    return prefix + columnName;
  }

  //
  // HANDLE NESTED RESULT MAPS
  //

  private void handleRowValuesForNestedResultMap(ResultSetWrapper rsw, ResultMap resultMap,
      ResultHandler<?> resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
    final boolean useCollectionConstructorInjection = resultMap.hasResultMapsUsingConstructorCollection();
    PendingConstructorCreation lastHandledCreation = null;
    if (useCollectionConstructorInjection) {
      verifyPendingCreationPreconditions(parentMapping);
    }

    final DefaultResultContext<Object> resultContext = new DefaultResultContext<>();
    ResultSet resultSet = rsw.getResultSet();
    skipRows(resultSet, rowBounds);
    Object rowValue = previousRowValue;

    while (shouldProcessMoreRows(resultContext, rowBounds) && !resultSet.isClosed() && resultSet.next()) {
      final ResultMap discriminatedResultMap = resolveDiscriminatedResultMap(resultSet, resultMap, null);
      final CacheKey rowKey = createRowKey(discriminatedResultMap, rsw, null);

      final Object partialObject = nestedResultObjects.get(rowKey);
      final boolean foundNewUniqueRow = partialObject == null;

      // issue #577, #542 && #101
      if (useCollectionConstructorInjection) {
        if (foundNewUniqueRow && lastHandledCreation != null) {
          createAndStorePendingCreation(resultHandler, resultSet, resultContext, lastHandledCreation);
          lastHandledCreation = null;
        }

        rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
        if (rowValue instanceof PendingConstructorCreation) {
          lastHandledCreation = (PendingConstructorCreation) rowValue;
        }
      } else if (mappedStatement.isResultOrdered()) {
        if (foundNewUniqueRow && rowValue != null) {
          nestedResultObjects.clear();
          storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
        }
        rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
      } else {
        rowValue = getRowValue(rsw, discriminatedResultMap, rowKey, null, partialObject);
        if (foundNewUniqueRow) {
          storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
        }
      }
    }

    if (useCollectionConstructorInjection && lastHandledCreation != null) {
      createAndStorePendingCreation(resultHandler, resultSet, resultContext, lastHandledCreation);
    } else if (rowValue != null && mappedStatement.isResultOrdered()
        && shouldProcessMoreRows(resultContext, rowBounds)) {
      storeObject(resultHandler, resultContext, rowValue, parentMapping, resultSet);
      previousRowValue = null;
    } else if (rowValue != null) {
      previousRowValue = rowValue;
    }
  }

  //
  // NESTED RESULT MAP (PENDING CONSTRUCTOR CREATIONS)
  //
  private void linkNestedPendingCreations(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix,
      CacheKey parentRowKey, PendingConstructorCreation pendingCreation, List<Object> constructorArgs)
      throws SQLException {
    if (parentRowKey == null) {
      // nothing to link, possibly due to simple (non-nested) result map
      return;
    }

    final CacheKey rowKey = createRowKey(resultMap, rsw, columnPrefix);
    final CacheKey combinedKey = combineKeys(rowKey, parentRowKey);

    if (combinedKey != CacheKey.NULL_CACHE_KEY) {
      nestedResultObjects.put(combinedKey, pendingCreation);
    }

    final List<ResultMapping> constructorMappings = resultMap.getConstructorResultMappings();
    for (int index = 0; index < constructorMappings.size(); index++) {
      final ResultMapping constructorMapping = constructorMappings.get(index);
      final String nestedResultMapId = constructorMapping.getNestedResultMapId();

      if (nestedResultMapId == null) {
        continue;
      }

      final Class<?> javaType = constructorMapping.getJavaType();
      if (javaType == null || !objectFactory.isCollection(javaType)) {
        continue;
      }

      final String constructorColumnPrefix = getColumnPrefix(columnPrefix, constructorMapping);
      final ResultMap nestedResultMap = resolveDiscriminatedResultMap(rsw.getResultSet(),
          configuration.getResultMap(constructorMapping.getNestedResultMapId()), constructorColumnPrefix);

      final Object actualValue = constructorArgs.get(index);
      final boolean hasValue = actualValue != null;
      final boolean isInnerCreation = actualValue instanceof PendingConstructorCreation;
      final boolean alreadyCreatedCollection = hasValue && objectFactory.isCollection(actualValue.getClass());

      if (!isInnerCreation) {
        final Collection<Object> value = pendingCreation.initializeCollectionForResultMapping(objectFactory,
            nestedResultMap, constructorMapping, index);
        if (!alreadyCreatedCollection) {
          // override values with empty collection
          constructorArgs.set(index, value);
        }

        // since we are linking a new value, we need to let nested objects know we did that
        final CacheKey nestedRowKey = createRowKey(nestedResultMap, rsw, constructorColumnPrefix);
        final CacheKey nestedCombinedKey = combineKeys(nestedRowKey, combinedKey);

        if (nestedCombinedKey != CacheKey.NULL_CACHE_KEY) {
          nestedResultObjects.put(nestedCombinedKey, pendingCreation);
        }

        if (hasValue) {
          pendingCreation.linkCollectionValue(constructorMapping, actualValue);
        }
      } else {
        final PendingConstructorCreation innerCreation = (PendingConstructorCreation) actualValue;
        final Collection<Object> value = pendingCreation.initializeCollectionForResultMapping(objectFactory,
            nestedResultMap, constructorMapping, index);
        // we will fill this collection when building the final object
        constructorArgs.set(index, value);
        // link the creation for building later
        pendingCreation.linkCreation(constructorMapping, innerCreation);
      }
    }
  }

  private boolean applyNestedPendingConstructorCreations(ResultSetWrapper rsw, ResultMap resultMap,
      MetaObject metaObject, String parentPrefix, CacheKey parentRowKey, boolean newObject, boolean foundValues) {
    if (newObject) {
      // new objects are linked by createResultObject
      return false;
    }

    for (ResultMapping constructorMapping : resultMap.getConstructorResultMappings()) {
      final String nestedResultMapId = constructorMapping.getNestedResultMapId();
      final Class<?> parameterType = constructorMapping.getJavaType();
      if (nestedResultMapId == null || constructorMapping.getResultSet() != null || parameterType == null
          || !objectFactory.isCollection(parameterType)) {
        continue;
      }

      try {
        final String columnPrefix = getColumnPrefix(parentPrefix, constructorMapping);
        final ResultMap nestedResultMap = getNestedResultMap(rsw.getResultSet(), nestedResultMapId, columnPrefix);

        final CacheKey rowKey = createRowKey(nestedResultMap, rsw, columnPrefix);
        final CacheKey combinedKey = combineKeys(rowKey, parentRowKey);

        // should have inserted already as a nested result object
        Object rowValue = nestedResultObjects.get(combinedKey);

        PendingConstructorCreation pendingConstructorCreation = null;
        if (rowValue instanceof PendingConstructorCreation) {
          pendingConstructorCreation = (PendingConstructorCreation) rowValue;
        } else if (rowValue != null) {
          // found a simple object that was already linked/handled
          continue;
        }

        final boolean newValueForNestedResultMap = pendingConstructorCreation == null;
        if (newValueForNestedResultMap) {
          final Object parentObject = metaObject.getOriginalObject();
          if (!(parentObject instanceof PendingConstructorCreation)) {
            throw new ExecutorException(
                "parentObject is not a pending creation, cannot continue linking! MyBatis internal error!");
          }

          pendingConstructorCreation = (PendingConstructorCreation) parentObject;
        }

        rowValue = getRowValue(rsw, nestedResultMap, combinedKey, columnPrefix,
            newValueForNestedResultMap ? null : pendingConstructorCreation);

        if (rowValue == null) {
          continue;
        }

        if (rowValue instanceof PendingConstructorCreation) {
          if (newValueForNestedResultMap) {
            // we created a brand new pcc. this is a new collection value
            pendingConstructorCreation.linkCreation(constructorMapping, (PendingConstructorCreation) rowValue);
            foundValues = true;
          }
        } else {
          pendingConstructorCreation.linkCollectionValue(constructorMapping, rowValue);
          foundValues = true;

          if (combinedKey != CacheKey.NULL_CACHE_KEY) {
            nestedResultObjects.put(combinedKey, pendingConstructorCreation);
          }
        }
      } catch (SQLException e) {
        throw new ExecutorException("Error getting constructor collection nested result map values for '"
            + constructorMapping.getProperty() + "'.  Cause: " + e, e);
      }
    }

    return foundValues;
  }

  private void createPendingConstructorCreations(Object rowValue) {
    // handle possible pending creations within this object
    // by now, the property mapping has been completely built, we can reconstruct it
    final PendingRelation pendingRelation = pendingPccRelations.remove(rowValue);
    final MetaObject metaObject = pendingRelation.metaObject;
    final ResultMapping resultMapping = pendingRelation.propertyMapping;

    // get the list to be built
    Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);
    if (collectionProperty != null) {
      // we expect pending creations now
      final Collection<Object> pendingCreations = (Collection<Object>) collectionProperty;

      // remove the link to the old collection
      metaObject.setValue(resultMapping.getProperty(), null);

      // create new collection property
      collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);
      final MetaObject targetMetaObject = configuration.newMetaObject(collectionProperty);

      // create the pending objects
      for (Object pendingCreation : pendingCreations) {
        if (pendingCreation instanceof PendingConstructorCreation) {
          final PendingConstructorCreation pendingConstructorCreation = (PendingConstructorCreation) pendingCreation;
          targetMetaObject.add(pendingConstructorCreation.create(objectFactory));
        }
      }
    }
  }

  private void verifyPendingCreationPreconditions(ResultMapping parentMapping) {
    if (parentMapping != null) {
      throw new ExecutorException(
          "Cannot construct objects with collections in constructors using multiple result sets yet!");
    }

    if (!mappedStatement.isResultOrdered()) {
      throw new ExecutorException("Cannot reliably construct result if we are not sure the results are ordered "
          + "so that no new previous rows would occur, set resultOrdered on your mapped statement if you have verified this");
    }
  }

  private void createAndStorePendingCreation(ResultHandler<?> resultHandler, ResultSet resultSet,
      DefaultResultContext<Object> resultContext, PendingConstructorCreation pendingCreation) throws SQLException {
    final Object result = pendingCreation.create(objectFactory);
    storeObject(resultHandler, resultContext, result, null, resultSet);
    nestedResultObjects.clear();
  }

  //
  // NESTED RESULT MAP (JOIN MAPPING)
  //

  private boolean applyNestedResultMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject,
      String parentPrefix, CacheKey parentRowKey, boolean newObject) {
    boolean foundValues = false;
    for (ResultMapping resultMapping : resultMap.getPropertyResultMappings()) {
      final String nestedResultMapId = resultMapping.getNestedResultMapId();
      if (nestedResultMapId != null && resultMapping.getResultSet() == null) {
        try {
          final String columnPrefix = getColumnPrefix(parentPrefix, resultMapping);
          final ResultMap nestedResultMap = getNestedResultMap(rsw.getResultSet(), nestedResultMapId, columnPrefix);
          if (resultMapping.getColumnPrefix() == null) {
            // try to fill circular reference only when columnPrefix
            // is not specified for the nested result map (issue #215)
            Object ancestorObject = ancestorObjects.get(nestedResultMapId);
            if (ancestorObject != null) {
              if (newObject) {
                linkObjects(metaObject, resultMapping, ancestorObject); // issue #385
              }
              continue;
            }
          }
          final CacheKey rowKey = createRowKey(nestedResultMap, rsw, columnPrefix);
          final CacheKey combinedKey = combineKeys(rowKey, parentRowKey);
          Object rowValue = nestedResultObjects.get(combinedKey);
          boolean knownValue = rowValue != null;
          instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject); // mandatory
          if (anyNotNullColumnHasValue(resultMapping, columnPrefix, rsw)) {
            rowValue = getRowValue(rsw, nestedResultMap, combinedKey, columnPrefix, rowValue);
            if (rowValue != null && !knownValue) {
              linkObjects(metaObject, resultMapping, rowValue);
              foundValues = true;
            }
          }
        } catch (SQLException e) {
          throw new ExecutorException(
              "Error getting nested result map values for '" + resultMapping.getProperty() + "'.  Cause: " + e, e);
        }
      }
    }

    // (issue #101)
    if (resultMap.hasResultMapsUsingConstructorCollection()) {
      foundValues = applyNestedPendingConstructorCreations(rsw, resultMap, metaObject, parentPrefix, parentRowKey,
          newObject, foundValues);
    }

    return foundValues;
  }

  private String getColumnPrefix(String parentPrefix, ResultMapping resultMapping) {
    final StringBuilder columnPrefixBuilder = new StringBuilder();
    if (parentPrefix != null) {
      columnPrefixBuilder.append(parentPrefix);
    }
    if (resultMapping.getColumnPrefix() != null) {
      columnPrefixBuilder.append(resultMapping.getColumnPrefix());
    }
    return columnPrefixBuilder.length() == 0 ? null : columnPrefixBuilder.toString().toUpperCase(Locale.ENGLISH);
  }

  private boolean anyNotNullColumnHasValue(ResultMapping resultMapping, String columnPrefix, ResultSetWrapper rsw)
      throws SQLException {
    Set<String> notNullColumns = resultMapping.getNotNullColumns();
    if (notNullColumns != null && !notNullColumns.isEmpty()) {
      ResultSet rs = rsw.getResultSet();
      for (String column : notNullColumns) {
        rs.getObject(prependPrefix(column, columnPrefix));
        if (!rs.wasNull()) {
          return true;
        }
      }
      return false;
    }
    if (columnPrefix != null) {
      for (String columnName : rsw.getColumnNames()) {
        if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix.toUpperCase(Locale.ENGLISH))) {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  private ResultMap getNestedResultMap(ResultSet rs, String nestedResultMapId, String columnPrefix)
      throws SQLException {
    ResultMap nestedResultMap = configuration.getResultMap(nestedResultMapId);
    return resolveDiscriminatedResultMap(rs, nestedResultMap, columnPrefix);
  }

  //
  // UNIQUE RESULT KEY
  //

  private CacheKey createRowKey(ResultMap resultMap, ResultSetWrapper rsw, String columnPrefix) throws SQLException {
    final CacheKey cacheKey = new CacheKey();
    cacheKey.update(resultMap.getId());
    List<ResultMapping> resultMappings = getResultMappingsForRowKey(resultMap);
    if (resultMappings.isEmpty()) {
      if (Map.class.isAssignableFrom(resultMap.getType())) {
        createRowKeyForMap(rsw, cacheKey);
      } else {
        createRowKeyForUnmappedProperties(resultMap, rsw, cacheKey, columnPrefix);
      }
    } else {
      createRowKeyForMappedProperties(resultMap, rsw, cacheKey, resultMappings, columnPrefix);
    }
    if (cacheKey.getUpdateCount() < 2) {
      return CacheKey.NULL_CACHE_KEY;
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
    if (resultMappings.isEmpty()) {
      resultMappings = resultMap.getPropertyResultMappings();
    }
    return resultMappings;
  }

  private void createRowKeyForMappedProperties(ResultMap resultMap, ResultSetWrapper rsw, CacheKey cacheKey,
      List<ResultMapping> resultMappings, String columnPrefix) throws SQLException {
    for (ResultMapping resultMapping : resultMappings) {
      if (resultMapping.isSimple()) {
        final String column = prependPrefix(resultMapping.getColumn(), columnPrefix);
        final TypeHandler<?> th = resultMapping.getTypeHandler();
        Set<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
        // Issue #114
        if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
          final Object value = th.getResult(rsw.getResultSet(), column);
          if (value != null || configuration.isReturnInstanceForEmptyRow()) {
            cacheKey.update(column);
            cacheKey.update(value);
          }
        }
      }
    }
  }

  private void createRowKeyForUnmappedProperties(ResultMap resultMap, ResultSetWrapper rsw, CacheKey cacheKey,
      String columnPrefix) throws SQLException {
    final MetaClass metaType = MetaClass.forClass(resultMap.getType(), reflectorFactory);
    List<String> unmappedColumnNames = rsw.getUnmappedColumnNames(resultMap, columnPrefix);
    for (String column : unmappedColumnNames) {
      String property = column;
      if (columnPrefix != null && !columnPrefix.isEmpty()) {
        // When columnPrefix is specified, ignore columns without the prefix.
        if (!column.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
          continue;
        }
        property = column.substring(columnPrefix.length());
      }
      if (metaType.findProperty(property, configuration.isMapUnderscoreToCamelCase()) != null) {
        String value = rsw.getResultSet().getString(column);
        if (value != null) {
          cacheKey.update(column);
          cacheKey.update(value);
        }
      }
    }
  }

  private void createRowKeyForMap(ResultSetWrapper rsw, CacheKey cacheKey) throws SQLException {
    List<String> columnNames = rsw.getColumnNames();
    for (String columnName : columnNames) {
      final String value = rsw.getResultSet().getString(columnName);
      if (value != null) {
        cacheKey.update(columnName);
        cacheKey.update(value);
      }
    }
  }

  private void linkObjects(MetaObject metaObject, ResultMapping resultMapping, Object rowValue) {
    linkObjects(metaObject, resultMapping, rowValue, false);
  }

  private void linkObjects(MetaObject metaObject, ResultMapping resultMapping, Object rowValue,
      boolean isNestedCursorResult) {
    final Object collectionProperty = instantiateCollectionPropertyIfAppropriate(resultMapping, metaObject);
    if (collectionProperty != null) {
      final MetaObject targetMetaObject = configuration.newMetaObject(collectionProperty);
      if (isNestedCursorResult) {
        targetMetaObject.addAll((List<?>) rowValue);
      } else {
        targetMetaObject.add(rowValue);
      }

      // it is possible for pending creations to get set via property mappings,
      // keep track of these, so we can rebuild them.
      final Object originalObject = metaObject.getOriginalObject();
      if (rowValue instanceof PendingConstructorCreation && !pendingPccRelations.containsKey(originalObject)) {
        PendingRelation pendingRelation = new PendingRelation();
        pendingRelation.propertyMapping = resultMapping;
        pendingRelation.metaObject = metaObject;

        pendingPccRelations.put(originalObject, pendingRelation);
      }
    } else {
      metaObject.setValue(resultMapping.getProperty(),
          isNestedCursorResult ? toSingleObj((List<?>) rowValue) : rowValue);
    }
  }

  private Object toSingleObj(List<?> list) {
    // Even if there are multiple elements, silently returns the first one.
    return list.isEmpty() ? null : list.get(0);
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
        throw new ExecutorException(
            "Error instantiating collection property for result '" + resultMapping.getProperty() + "'.  Cause: " + e,
            e);
      }
    } else if (objectFactory.isCollection(propertyValue.getClass())) {
      return propertyValue;
    }
    return null;
  }

  private boolean hasTypeHandlerForResultObject(ResultSetWrapper rsw, Class<?> resultType) {
    if (rsw.getColumnNames().size() == 1) {
      return typeHandlerRegistry.hasTypeHandler(resultType, rsw.getJdbcType(rsw.getColumnNames().get(0)));
    }
    return typeHandlerRegistry.hasTypeHandler(resultType);
  }

}
