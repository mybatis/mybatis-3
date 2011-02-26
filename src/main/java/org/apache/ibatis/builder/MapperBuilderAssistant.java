package org.apache.ibatis.builder;

import org.apache.ibatis.builder.xml.IncompleteStatementException;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.util.*;

public class MapperBuilderAssistant extends BaseBuilder {

  private String currentNamespace;
  private String resource;
  private Cache currentCache;

  public MapperBuilderAssistant(Configuration configuration, String resource) {
    super(configuration);
    ErrorContext.instance().resource(resource);
    this.resource = resource;
  }

  public String getCurrentNamespace() {
    return currentNamespace;
  }

  public void setCurrentNamespace(String currentNamespace) {
    if (currentNamespace == null) {
      throw new BuilderException("The mapper element requires a namespace attribute to be specified.");
    }

    if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
      throw new BuilderException("Wrong namespace. Expected '"
          + this.currentNamespace + "' but found '" + currentNamespace + "'.");
    }

    this.currentNamespace = currentNamespace;
  }

  public String applyCurrentNamespace(String base) {
    if (base == null) return null;
    // is it qualified with this or other namespace yet?
    if (base.contains(".")) return base;
    return currentNamespace + "." + base;
  }
  
  public Cache useCacheRef(String namespace) {
    if (namespace == null) {
      throw new BuilderException("cache-ref element requires a namespace attribute.");
    }
    try {
        Cache cache = configuration.getCache(namespace);
        if (cache == null) {
          throw new IncompleteCacheException("No cache for namespace '" + namespace + "' could be found.");
        }
        currentCache = cache;
        return cache;
    } catch (IllegalArgumentException e) {
    	throw new IncompleteCacheException("No cache for namespace '" + namespace + "' could be found.", e);
    }
  }

  public Cache useNewCache(Class<? extends Cache> typeClass,
                           Class<? extends Cache> evictionClass,
                           Long flushInterval,
                           Integer size,
                           boolean readWrite,
                           Properties props) {
    typeClass = valueOrDefault(typeClass, PerpetualCache.class);
    evictionClass = valueOrDefault(evictionClass, LruCache.class);
    Cache cache = new CacheBuilder(currentNamespace)
        .implementation(typeClass)
        .addDecorator(evictionClass)
        .clearInterval(flushInterval)
        .size(size)
        .readWrite(readWrite)
        .properties(props)
        .build();
    configuration.addCache(cache);
    currentCache = cache;
    return cache;
  }

  public ParameterMap addParameterMap(String id, Class<?> parameterClass, List<ParameterMapping> parameterMappings) {
    id = applyCurrentNamespace(id);
    ParameterMap.Builder parameterMapBuilder = new ParameterMap.Builder(configuration, id, parameterClass, parameterMappings);
    ParameterMap parameterMap = parameterMapBuilder.build();
    configuration.addParameterMap(parameterMap);
    return parameterMap;
  }

  public ParameterMapping buildParameterMapping(
      Class<?> parameterType,
      String property,
      Class<?> javaType,
      JdbcType jdbcType,
      String resultMap,
      ParameterMode parameterMode,
      Class<? extends TypeHandler> typeHandler,
      Integer numericScale) {
    resultMap = applyCurrentNamespace(resultMap);

    // Class parameterType = parameterMapBuilder.type();
    Class<?> javaTypeClass = resolveParameterJavaType(parameterType, property, javaType, jdbcType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);

    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, javaTypeClass);
    builder.jdbcType(jdbcType);
    builder.resultMapId(resultMap);
    builder.mode(parameterMode);
    builder.numericScale(numericScale);
    builder.typeHandler(typeHandlerInstance);
    return builder.build();
  }

  public ResultMap addResultMap(
      String id,
      Class<?> type,
      String extend,
      Discriminator discriminator,
      List<ResultMapping> resultMappings) {
    id = applyCurrentNamespace(id);
    extend = applyCurrentNamespace(extend);

    ResultMap.Builder resultMapBuilder = new ResultMap.Builder(configuration, id, type, resultMappings);
    if (extend != null) {
      ResultMap resultMap = configuration.getResultMap(extend);
      resultMappings.addAll(resultMap.getResultMappings());
    }
    resultMapBuilder.discriminator(discriminator);
    ResultMap resultMap = resultMapBuilder.build();
    configuration.addResultMap(resultMap);
    return resultMap;
  }

  public ResultMapping buildResultMapping(
      Class<?> resultType,
      String property,
      String column,
      Class<?> javaType,
      JdbcType jdbcType,
      String nestedSelect,
      String nestedResultMap,
      Class<? extends TypeHandler> typeHandler,
      List<ResultFlag> flags) {
    ResultMapping resultMapping = assembleResultMapping(
        resultType,
        property,
        column,
        javaType,
        jdbcType,
        nestedSelect,
        nestedResultMap,
        typeHandler,
        flags);
    return resultMapping;
  }


  public Discriminator buildDiscriminator(
      Class<?> resultType,
      String column,
      Class<?> javaType,
      JdbcType jdbcType,
      Class<? extends TypeHandler> typeHandler,
      Map<String, String> discriminatorMap) {
    ResultMapping resultMapping = assembleResultMapping(
        resultType,
        null,
        column,
        javaType,
        jdbcType,
        null,
        null,
        typeHandler,
        new ArrayList<ResultFlag>());
    Map<String, String> namespaceDiscriminatorMap = new HashMap<String, String>();
    for (Map.Entry<String, String> e : discriminatorMap.entrySet()) {
      String resultMap = e.getValue();
      resultMap = applyCurrentNamespace(resultMap);
      namespaceDiscriminatorMap.put(e.getKey(), resultMap);
    }
    Discriminator.Builder discriminatorBuilder = new Discriminator.Builder(configuration, resultMapping, namespaceDiscriminatorMap);
    return discriminatorBuilder.build();
  }

  public MappedStatement addMappedStatement(
      String id,
      SqlSource sqlSource,
      StatementType statementType,
      SqlCommandType sqlCommandType,
      Integer fetchSize,
      Integer timeout,
      String parameterMap,
      Class<?> parameterType,
      String resultMap,
      Class<?> resultType,
      ResultSetType resultSetType,
      boolean flushCache,
      boolean useCache,
      KeyGenerator keyGenerator,
      String keyProperty) {
    id = applyCurrentNamespace(id);
    boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

    MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType);
    statementBuilder.resource(resource);
    statementBuilder.fetchSize(fetchSize);
    statementBuilder.statementType(statementType);
    statementBuilder.keyGenerator(keyGenerator);
    statementBuilder.keyProperty(keyProperty);
    setStatementTimeout(timeout, statementBuilder);

    setStatementParameterMap(parameterMap, parameterType, statementBuilder);
    setStatementResultMap(resultMap, resultType, resultSetType, statementBuilder);
    setStatementCache(isSelect, flushCache, useCache, currentCache, statementBuilder);

    MappedStatement statement = statementBuilder.build();
    configuration.addMappedStatement(statement);
    return statement;
  }

  private <T> T valueOrDefault(T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }

  private void setStatementCache(
      boolean isSelect,
      boolean flushCache,
      boolean useCache,
      Cache cache,
      MappedStatement.Builder statementBuilder) {
    flushCache = valueOrDefault(flushCache, !isSelect);
    useCache = valueOrDefault(useCache, isSelect);
    statementBuilder.flushCacheRequired(flushCache);
    statementBuilder.useCache(useCache);
    statementBuilder.cache(cache);
  }

  private void setStatementParameterMap(
      String parameterMap,
      Class<?> parameterTypeClass,
      MappedStatement.Builder statementBuilder) {
    parameterMap = applyCurrentNamespace(parameterMap);

    if (parameterMap != null) {
      try {
        statementBuilder.parameterMap(configuration.getParameterMap(parameterMap));
      } catch (IllegalArgumentException e) {
      	throw new IncompleteStatementException("Could not find parameter map " + parameterMap, e);
      }
    } else if (parameterTypeClass != null) {
      List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
      ParameterMap.Builder inlineParameterMapBuilder = new ParameterMap.Builder(
          configuration,
          statementBuilder.id() + "-Inline",
          parameterTypeClass,
          parameterMappings);
      statementBuilder.parameterMap(inlineParameterMapBuilder.build());
    }
  }

  private void setStatementResultMap(
      String resultMap,
      Class<?> resultType,
      ResultSetType resultSetType,
      MappedStatement.Builder statementBuilder) {
    resultMap = applyCurrentNamespace(resultMap);

    List<ResultMap> resultMaps = new ArrayList<ResultMap>();
    if (resultMap != null) {
      String[] resultMapNames = resultMap.split(",");
      for (String resultMapName : resultMapNames) {
        try {
          resultMaps.add(configuration.getResultMap(resultMapName.trim()));
        } catch (IllegalArgumentException e) {
          	throw new IncompleteStatementException("Could not find result map " + resultMapName, e);
        }
      }
    } else if (resultType != null) {
      ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
          configuration,
          statementBuilder.id() + "-Inline",
          resultType,
          new ArrayList<ResultMapping>());
      resultMaps.add(inlineResultMapBuilder.build());
    }
    statementBuilder.resultMaps(resultMaps);

    statementBuilder.resultSetType(resultSetType);
  }

  private void setStatementTimeout(Integer timeout, MappedStatement.Builder statementBuilder) {
    if (timeout == null) {
      timeout = configuration.getDefaultStatementTimeout();
    }
    statementBuilder.timeout(timeout);
  }

  private ResultMapping assembleResultMapping(
      Class<?> resultType,
      String property,
      String column,
      Class<?> javaType,
      JdbcType jdbcType,
      String nestedSelect,
      String nestedResultMap,
      Class<? extends TypeHandler> typeHandler,
      List<ResultFlag> flags) {
    // Class resultType = resultMapBuilder.type();
    nestedResultMap = applyCurrentNamespace(nestedResultMap);
    Class<?> javaTypeClass = resolveResultJavaType(resultType, property, javaType);
    TypeHandler typeHandlerInstance = (TypeHandler) resolveInstance(typeHandler);

    List<ResultMapping> composites = parseCompositeColumnName(column);
    if (composites.size() > 0) {
      ResultMapping first = composites.get(0);
      column = first.getColumn();
    }

    ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
    builder.jdbcType(jdbcType);
    builder.nestedQueryId(applyCurrentNamespace(nestedSelect));
    builder.nestedResultMapId(applyCurrentNamespace(nestedResultMap));
    builder.typeHandler(typeHandlerInstance);
    builder.flags(flags == null ? new ArrayList<ResultFlag>() : flags);
    builder.composites(composites);
    
    return builder.build();
  }

  private List<ResultMapping> parseCompositeColumnName(String columnName) {
    List<ResultMapping> composites = new ArrayList<ResultMapping>();
    if (columnName != null) {
      if (columnName.indexOf('=') > -1
          || columnName.indexOf(',') > -1) {
        StringTokenizer parser = new StringTokenizer(columnName, "{}=, ", false);
        while (parser.hasMoreTokens()) {
          String property = parser.nextToken();
          String column = parser.nextToken();
          ResultMapping.Builder complexBuilder = new ResultMapping.Builder(configuration, property, column, configuration.getTypeHandlerRegistry().getUnknownTypeHandler());
          composites.add(complexBuilder.build());
        }
      }
    }
    return composites;
  }

  private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
    if (javaType == null && property != null) {
      try {
        MetaClass metaResultType = MetaClass.forClass(resultType);
        javaType = metaResultType.getSetterType(property);
      } catch (Exception e) {
        //ignore, following null check statement will deal with the situation
      }
    }
    if (javaType == null) {
      javaType = Object.class;
    }
    return javaType;
  }

  private Class<?> resolveParameterJavaType(Class<?> resultType, String property, Class<?> javaType, JdbcType jdbcType) {
    if (javaType == null) {
      if (JdbcType.CURSOR.equals(jdbcType)) {
        javaType = java.sql.ResultSet.class;
      } else if (Map.class.isAssignableFrom(resultType)) {
        javaType = Object.class;
      } else {
        MetaClass metaResultType = MetaClass.forClass(resultType);
        javaType = metaResultType.getGetterType(property);
      }
    }
    if (javaType == null) {
      javaType = Object.class;
    }
    return javaType;
  }

}
