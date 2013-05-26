/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.ibatis.sqlmap.engine.builder;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.mapping.CacheBuilder;
import org.apache.ibatis.mapping.Discriminator;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import com.ibatis.common.util.NodeEvent;
import com.ibatis.common.util.NodeEventParser;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.client.extensions.TypeHandlerCallback;

public class XmlSqlMapParser {

  private XmlSqlMapConfigParser configParser;
  private Ibatis2Configuration config;
  private Reader reader;
  private NodeEventParser parser;

  private CacheBuilder cacheBuilder;
  private List<String> flushCacheStatements;

  private ResultMap.Builder resultMapBuilder;
  private List<ResultMapping> resultMappingList;
  private Map<String, String> discriminatorSubMap;

  private Discriminator.Builder discriminatorBuilder;

  private ParameterMap.Builder parameterMapBuilder;
  private List<ParameterMapping> parameterMappingList;

  private String namespace;
  private List<String> groupByProperties;

  public XmlSqlMapParser(XmlSqlMapConfigParser configParser, Reader reader) {
    this.configParser = configParser;
    this.config = configParser.getConfiguration();
    this.reader = reader;
    this.parser = new NodeEventParser();
    this.parser.addNodeletHandler(this);
    this.parser.setVariables(config.getVariables());
    this.flushCacheStatements = new ArrayList<String>();
    this.parser.setEntityResolver(new SqlMapEntityResolver());
  }

  public void parse() {
    parser.parse(reader);
  }

  public XmlSqlMapConfigParser getConfigParser() {
    return configParser;
  }

  public String getNamespace() {
    return namespace;
  }

  public String applyNamespace(String id) {

    return id == null ? null : namespace == null ? id : namespace + "." + id;
  }

  @NodeEvent("/sqlMap")
  public void sqlMap(XNode context) throws Exception {
    this.namespace = context.getStringAttribute("namespace");
  }

  @NodeEvent("/sqlMap/typeAlias")
  public void sqlMaptypeAlias(XNode context) throws Exception {
    String alias = context.getStringAttribute("alias");
    String type = context.getStringAttribute("type");
    config.getTypeAliasRegistry().registerAlias(alias, type);
  }

  @NodeEvent("/sqlMap/cacheModel")
  public void sqlMapcacheModel(XNode context) throws Exception {
    String id = applyNamespace(context.getStringAttribute("id"));
    String type = context.getStringAttribute("type");
    Boolean readOnly = context.getBooleanAttribute("readOnly", true);
    Boolean serialize = context.getBooleanAttribute("serialize", true);
    Class clazz = config.getTypeAliasRegistry().resolveAlias(type);
    cacheBuilder = new CacheBuilder(id);
    cacheBuilder.addDecorator(clazz);

    //LOCAL_READ_WRITE (serializable=false, readOnly=false)
    //SHARED_READ_ONLY (serializable=false, readOnly=true)
    //SHARED_READ_WRITE (serializable=true, readOnly=false)
    if (serialize) {
      if (readOnly) {
        cacheBuilder.readWrite(false);
      } else {
        cacheBuilder.readWrite(true);
      }
    } else {
      if (readOnly) {
        cacheBuilder.readWrite(false);
      } else {
        cacheBuilder = null;
      }
    }
  }

  @NodeEvent("/sqlMap/cacheModel/property")
  public void sqlMapcacheModelproperty(XNode context) throws Exception {
    if (cacheBuilder != null) {
      String name = context.getStringAttribute("name");
      String value = context.getStringAttribute("value");
      if ("size".equals(name)) {
        cacheBuilder.size(Integer.parseInt(value));
      }
    }
  }

  @NodeEvent("/sqlMap/cacheModel/flushInterval")
  public void sqlMapcacheModelflushInterval(XNode context) throws Exception {
    if (cacheBuilder != null) {
      long clearInterval = 0L;
      clearInterval += context.getIntAttribute("milliseconds", 0);
      clearInterval += context.getIntAttribute("seconds", 0) * 1000L;
      clearInterval += context.getIntAttribute("minutes", 0) * 60L * 1000L;
      clearInterval += context.getIntAttribute("hours", 0) * 60L * 60L * 1000L;
      if (clearInterval < 1L) {
        throw new RuntimeException("A flush interval must specify one or more of milliseconds, seconds, minutes or hours.");
      }
      cacheBuilder.clearInterval(clearInterval);
    }
  }

  @NodeEvent("/sqlMap/cacheModel/flushOnExecute")
  public void sqlMapcacheModelflushOnExecute(XNode context) throws Exception {
    if (cacheBuilder != null) {
      String statement = context.getStringAttribute("statement");
      flushCacheStatements.add(statement);
    }
  }

  @NodeEvent("/sqlMap/cacheModel/end()")
  public void sqlMapcacheModelEnd(XNode context) throws Exception {
    if (cacheBuilder != null) {
      Cache cache = cacheBuilder.build();
      for (String sid : flushCacheStatements) {
        config.getFlushCachePlugin().addFlushOnExecute(sid, cache);
      }
      config.addCache(cache);
      flushCacheStatements = new ArrayList<String>();
    }
  }

  @NodeEvent("/sqlMap/resultMap")
  public void sqlMapresultMap(XNode context) throws Exception {
    String xmlName = context.getStringAttribute("xmlName");
    if (xmlName != null) {
      throw new UnsupportedOperationException("xmlName is not supported by iBATIS 3");
    }

    String id = applyNamespace(context.getStringAttribute("id"));
    String resultClassName = context.getStringAttribute("class");
    String extendedId = applyNamespace(context.getStringAttribute("extends"));

    String groupBy = context.getStringAttribute("groupBy");
    if (groupBy != null) {
      groupByProperties = Arrays.asList(groupBy.split(", "));
    }

    Class resultClass;
    try {
      resultClass = config.getTypeAliasRegistry().resolveAlias(resultClassName);
    } catch (Exception e) {
      throw new RuntimeException("Error configuring Result.  Could not set ResultClass.  Cause: " + e, e);
    }

    resultMappingList = new ArrayList<ResultMapping>();
    resultMapBuilder = new ResultMap.Builder(config, id, resultClass, resultMappingList);

    if (extendedId != null) {
      ResultMap extendedResultMap = config.getResultMap(extendedId);
      for (ResultMapping mapping : extendedResultMap.getResultMappings()) {
        resultMappingList.add(mapping);
      }
      resultMapBuilder.discriminator(extendedResultMap.getDiscriminator());
    }

  }

  @NodeEvent("/sqlMap/resultMap/discriminator")
  public void sqlMapresultMapdiscriminator(XNode context) throws Exception {
    String nullValue = context.getStringAttribute("nullValue");
    if (nullValue != null) {
      throw new UnsupportedOperationException("Null value subsitution is not supported by iBATIS 3.");
    }
    String columnIndexProp = context.getStringAttribute("columnIndex");
    if (columnIndexProp != null) {
      throw new UnsupportedOperationException("Numerical column indices are not supported.  Use the column name instead.");
    }

    String jdbcType = context.getStringAttribute("jdbcType");
    String javaType = context.getStringAttribute("javaType");
    String columnName = context.getStringAttribute("column");
    String callback = context.getStringAttribute("typeHandler");

    Class javaClass = null;
    try {
      if (javaType != null && javaType.length() > 0) {
        javaClass = config.getTypeAliasRegistry().resolveAlias(javaType);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error setting java type on result discriminator mapping.  Cause: " + e);
    }

    JdbcType jdbcTypeEnum = null;
    if (jdbcType != null) {
      jdbcTypeEnum = JdbcType.valueOf(jdbcType);
    }

    TypeHandler typeHandler = null;
    if (javaClass != null) {
      typeHandler = config.getTypeHandlerRegistry().getTypeHandler(javaClass, jdbcTypeEnum);
    }
    try {
      if (callback != null && callback.length() > 0) {
        typeHandler = (TypeHandler) config.getTypeAliasRegistry().resolveAlias(callback).newInstance();
      }
    } catch (Exception e) {
      throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
    }


    ResultMapping.Builder resultMappingBuilder = new ResultMapping.Builder(config, columnName, columnName, typeHandler);
    resultMappingBuilder.javaType(javaClass);
    resultMappingBuilder.jdbcType(jdbcTypeEnum);
    ResultMapping resultMapping = resultMappingBuilder.build();

    discriminatorSubMap = new HashMap<String, String>();
    discriminatorBuilder = new Discriminator.Builder(config, resultMapping, discriminatorSubMap);

  }

  @NodeEvent("/sqlMap/resultMap/discriminator/subMap")
  public void sqlMapresultMapdiscriminatorsubMap(XNode context) throws Exception {
    String value = context.getStringAttribute("value");
    String resultMap = context.getStringAttribute("resultMap");
    resultMap = applyNamespace(resultMap);
    discriminatorSubMap.put(value, resultMap);
  }

  @NodeEvent("/sqlMap/resultMap/discriminator/end()")
  public void sqlMapresultMapdiscriminatorEnd(XNode context) throws Exception {
    resultMapBuilder.discriminator(discriminatorBuilder.build());
  }


  @NodeEvent("/sqlMap/resultMap/result")
  public void sqlMapresultMapresult(XNode context) throws Exception {
    String nullValue = context.getStringAttribute("nullValue");
    if (nullValue != null) {
      throw new UnsupportedOperationException("Null value subsitution is not supported by iBATIS 3.");
    }
    String columnIndexProp = context.getStringAttribute("columnIndex");
    if (columnIndexProp != null) {
      throw new UnsupportedOperationException("Numerical column indices are not supported.  Use the column name instead.");
    }

    String propertyName = context.getStringAttribute("property");
    String jdbcType = context.getStringAttribute("jdbcType");
    String javaType = context.getStringAttribute("javaType");
    String columnName = context.getStringAttribute("column");

    String statementName = context.getStringAttribute("select");
    String resultMapName = context.getStringAttribute("resultMap");
    String callback = context.getStringAttribute("typeHandler");

    Class javaClass = null;
    try {
      if (javaType != null && javaType.length() > 0) {
        javaClass = config.getTypeAliasRegistry().resolveAlias(javaType);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error setting java type on result discriminator mapping.  Cause: " + e);
    }
    if (javaClass == null
        && !Map.class.isAssignableFrom(resultMapBuilder.type())
        && !config.getTypeHandlerRegistry().hasTypeHandler(resultMapBuilder.type())) {
      javaClass = MetaClass.forClass(resultMapBuilder.type()).getSetterType(propertyName);
    }
    if (javaClass == null && statementName != null) {
      javaClass = List.class;
    }

    JdbcType jdbcTypeEnum = null;
    if (jdbcType != null) {
      jdbcTypeEnum = JdbcType.valueOf(jdbcType);
    }

    TypeHandler typeHandler = null;
    if (javaClass != null) {
      typeHandler = config.getTypeHandlerRegistry().getTypeHandler(javaClass, jdbcTypeEnum);
    }
    try {
      if (callback != null && callback.length() > 0) {
        Object o = config.getTypeAliasRegistry().resolveAlias(callback).newInstance();
        if (o instanceof TypeHandlerCallback) {
          typeHandler = new TypeHandlerCallbackAdapter((TypeHandlerCallback) o);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Error occurred during custom type handler configuration.  Cause: " + e, e);
    }
    if (typeHandler == null && config.getTypeHandlerRegistry().hasTypeHandler(resultMapBuilder.type())) {
      typeHandler = config.getTypeHandlerRegistry().getTypeHandler(resultMapBuilder.type());
    }

    if (typeHandler == null) {
      Class resultClass = resultMapBuilder.type();
      if (resultClass != null && !Map.class.isAssignableFrom(resultClass)) {
        MetaClass metaResultClass = MetaClass.forClass(resultClass);
        Class resultType = null;
        if (metaResultClass.hasGetter(propertyName)) {
          resultType = metaResultClass.getGetterType(propertyName);
        } else if (metaResultClass.hasSetter(propertyName)) {
          resultType = metaResultClass.getSetterType(propertyName);
        }
        if (resultType != null) {
          typeHandler = config.getTypeHandlerRegistry().getTypeHandler(resultType);
        }
      } else {
        typeHandler = config.getTypeHandlerRegistry().getUnknownTypeHandler();
      }
    }

    List<ResultMapping> composites = parseCompositeColumnName(columnName);
    if (composites.size() > 0) {
      ResultMapping first = composites.get(0);
      columnName = first.getColumn();
    }

    ResultMapping.Builder resultMappingBuilder = new ResultMapping.Builder(config, propertyName, columnName, typeHandler);
    resultMappingBuilder.javaType(javaClass);
    resultMappingBuilder.nestedQueryId(statementName);
    resultMappingBuilder.nestedResultMapId(resultMapName);
    resultMappingBuilder.jdbcType(jdbcTypeEnum);
    resultMappingBuilder.composites(composites);
    if (groupByProperties != null && groupByProperties.contains(propertyName)) {
      List<ResultFlag> flags = new ArrayList<ResultFlag>();
      resultMappingBuilder.flags(flags);
    }
    resultMappingList.add(resultMappingBuilder.build());
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
          ResultMapping.Builder complexBuilder = new ResultMapping.Builder(config, property, column, config.getTypeHandlerRegistry().getUnknownTypeHandler());
          composites.add(complexBuilder.build());
        }
      }
    }
    return composites;
  }

  @NodeEvent("/sqlMap/resultMap/end()")
  public void sqlMapresultMapend(XNode context) throws Exception {
    config.addResultMap(resultMapBuilder.build());
  }

  @NodeEvent("/sqlMap/parameterMap")
  public void sqlMapparameterMap(XNode context) throws Exception {
    String id = applyNamespace(context.getStringAttribute("id"));
    String parameterClassName = context.getStringAttribute("class");
    Class parameterClass = config.getTypeAliasRegistry().resolveAlias(parameterClassName);
    parameterMappingList = new ArrayList<ParameterMapping>();
    parameterMapBuilder = new ParameterMap.Builder(config, id, parameterClass, parameterMappingList);
  }

  @NodeEvent("/sqlMap/parameterMap/parameter")
  public void sqlMapparameterMapparameter(XNode context) throws Exception {
    String nullValue = context.getStringAttribute("nullValue");
    if (nullValue != null) {
      throw new UnsupportedOperationException("Null value subsitution is not supported by iBATIS 3.");
    }

    String propertyName = context.getStringAttribute("property");
    String jdbcType = context.getStringAttribute("jdbcType");
    String javaType = context.getStringAttribute("javaType");
    String resultMap = context.getStringAttribute("resultMap");
    String mode = context.getStringAttribute("mode");
    String callback = context.getStringAttribute("typeHandler");
    String numericScaleProp = context.getStringAttribute("numericScale");

    Class javaClass = null;
    try {
      if (javaType != null && javaType.length() > 0) {
        javaClass = config.getTypeAliasRegistry().resolveAlias(javaType);
      }
    } catch (Exception e) {
      throw new RuntimeException("Error setting javaType on parameter mapping.  Cause: " + e);
    }

    JdbcType jdbcTypeEnum = null;
    if (jdbcType != null) {
      jdbcTypeEnum = JdbcType.valueOf(jdbcType);
    }

    TypeHandler typeHandler = null;
    if (javaClass != null) {
      typeHandler = config.getTypeHandlerRegistry().getTypeHandler(javaClass, jdbcTypeEnum);
    }
    if (callback != null) {
      Object o = config.getTypeAliasRegistry().resolveAlias(callback).newInstance();
      if (o instanceof TypeHandlerCallback) {
        typeHandler = new TypeHandlerCallbackAdapter((TypeHandlerCallback) o);
      }
    }
    if (typeHandler == null && config.getTypeHandlerRegistry().hasTypeHandler(parameterMapBuilder.type())) {
      typeHandler = config.getTypeHandlerRegistry().getTypeHandler(parameterMapBuilder.type());
    }
    if (typeHandler == null) {
      Class parameterClass = parameterMapBuilder.type();
      if (parameterClass != null && !Map.class.isAssignableFrom(parameterClass)) {
        MetaClass metaParamClass = MetaClass.forClass(parameterClass);
        Class paramType = null;
        if (metaParamClass.hasGetter(propertyName)) {
          paramType = metaParamClass.getGetterType(propertyName);
        } else if (metaParamClass.hasSetter(propertyName)) {
          paramType = metaParamClass.getSetterType(propertyName);
        }
        if (paramType != null) {
          typeHandler = config.getTypeHandlerRegistry().getTypeHandler(paramType);
        }
      } else {
        typeHandler = config.getTypeHandlerRegistry().getUnknownTypeHandler();
      }
    }

    ParameterMode paramModeEnum = ParameterMode.IN;
    if (mode != null) {
      paramModeEnum = ParameterMode.valueOf(mode);
    }

    Integer numericScale = null;
    if (numericScaleProp != null) {
      numericScale = new Integer(numericScaleProp);
    }

    ParameterMapping.Builder parameterMappingBuilder = new ParameterMapping.Builder(config, propertyName, typeHandler);
    parameterMappingBuilder.javaType(javaClass);
    parameterMappingBuilder.jdbcType(jdbcTypeEnum);
    parameterMappingBuilder.mode(paramModeEnum);
    parameterMappingBuilder.numericScale(numericScale);
    parameterMappingBuilder.resultMapId(resultMap);

    parameterMappingList.add(parameterMappingBuilder.build());
  }


  @NodeEvent("/sqlMap/parameterMap/end()")
  public void sqlMapparameterMapend(XNode context) throws Exception {
    config.addParameterMap(parameterMapBuilder.build());
  }

  @NodeEvent("/sqlMap/sql")
  public void sqlMapsql(XNode context) throws Exception {
    String id = context.getStringAttribute("id");
    if (configParser.isUseStatementNamespaces()) {
      id = applyNamespace(id);
    }
    if (configParser.hasSqlFragment(id)) {
      throw new SqlMapException("Duplicate <sql>-include '" + id + "' found.");
    } else {
      configParser.addSqlFragment(id, context);
    }
  }

  @NodeEvent("/sqlMap/statement")
  public void sqlMapstatement(XNode context) throws Exception {
    new XmlSqlStatementParser(this).parseGeneralStatement(context);
  }

  @NodeEvent("/sqlMap/select")
  public void sqlMapselect(XNode context) throws Exception {
    new XmlSqlStatementParser(this).parseGeneralStatement(context);
  }

  @NodeEvent("/sqlMap/insert")
  public void sqlMapinsert(XNode context) throws Exception {
    new XmlSqlStatementParser(this).parseGeneralStatement(context);
  }

  @NodeEvent("/sqlMap/update")
  public void sqlMapupdate(XNode context) throws Exception {
    new XmlSqlStatementParser(this).parseGeneralStatement(context);
  }

  @NodeEvent("/sqlMap/delete")
  public void sqlMapdelete(XNode context) throws Exception {
    new XmlSqlStatementParser(this).parseGeneralStatement(context);
  }

  @NodeEvent("/sqlMap/procedure")
  public void sqlMapprocedure(XNode context) throws Exception {
    new XmlSqlStatementParser(this).parseGeneralStatement(context);
  }

}
