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

import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.impl.SqlMapSessionImpl;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.parsing.XNode;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class XmlSqlStatementParser {

  private XmlSqlMapParser mapParser;
  private Ibatis2Configuration configuration;

  public XmlSqlStatementParser(XmlSqlMapParser mapParser) {
    this.mapParser = mapParser;
    this.configuration = mapParser.getConfigParser().getConfiguration();
  }

  public void parseGeneralStatement(XNode context) {
    // get attributes
    String id = context.getStringAttribute("id");
    String parameterMapName = context.getStringAttribute("parameterMap");
    String parameterClassName = context.getStringAttribute("parameterClass");
    String resultMapName = context.getStringAttribute("resultMap");
    String resultClassName = context.getStringAttribute("resultClass");
    String cacheModelName = context.getStringAttribute("cacheModel");
    String resultSetType = context.getStringAttribute("resultSetType");
    String fetchSize = context.getStringAttribute("fetchSize");
    String timeout = context.getStringAttribute("timeout");
    // 2.x -- String allowRemapping = context.getStringAttribute("remapResults");

    if (context.getStringAttribute("xmlResultName") != null) {
      throw new UnsupportedOperationException("xmlResultName is not supported by iBATIS 3");
    }

    if (mapParser.getConfigParser().isUseStatementNamespaces()) {
      id = mapParser.applyNamespace(id);
    }

    String[] additionalResultMapNames = null;
    if (resultMapName != null) {
      additionalResultMapNames = getAllButFirstToken(resultMapName);
      resultMapName = getFirstToken(resultMapName);
      resultMapName = mapParser.applyNamespace(resultMapName);
      for (int i = 0; i < additionalResultMapNames.length; i++) {
        additionalResultMapNames[i] = mapParser.applyNamespace(additionalResultMapNames[i]);
      }
    }

    String[] additionalResultClassNames = null;
    if (resultClassName != null) {
      additionalResultClassNames = getAllButFirstToken(resultClassName);
      resultClassName = getFirstToken(resultClassName);
    }
    Class[] additionalResultClasses = null;
    if (additionalResultClassNames != null) {
      additionalResultClasses = new Class[additionalResultClassNames.length];
      for (int i = 0; i < additionalResultClassNames.length; i++) {
        additionalResultClasses[i] = resolveClass(additionalResultClassNames[i]);
      }
    }

    Integer timeoutInt = timeout == null ? null : new Integer(timeout);
    Integer fetchSizeInt = fetchSize == null ? null : new Integer(fetchSize);

    // 2.x -- boolean allowRemappingBool = "true".equals(allowRemapping);

    SqlSource sqlSource = new SqlSourceFactory(mapParser).newSqlSourceIntance(mapParser, context);

    String nodeName = context.getNode().getNodeName();
    SqlCommandType sqlCommandType;
    try {
      sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase());
    } catch (Exception e) {
      sqlCommandType = SqlCommandType.UNKNOWN; 
    }


    MappedStatement.Builder builder = new MappedStatement.Builder(configuration, id, sqlSource,sqlCommandType);

    builder.useCache(true);
    if (!"select".equals(context.getNode().getNodeName())) {
      builder.flushCacheRequired(true);
    }

    if (parameterMapName != null) {
      parameterMapName = mapParser.applyNamespace(parameterMapName);
      builder.parameterMap(configuration.getParameterMap(parameterMapName));
    } else if (parameterClassName != null) {
      Class parameterClass = resolveClass(parameterClassName);
      List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
      if (sqlSource instanceof SimpleSqlSource) {
        parameterMappings = sqlSource.getBoundSql(null).getParameterMappings();
      }
      ParameterMap.Builder parameterMapBuilder = new ParameterMap.Builder(configuration, id + "-ParameterMap", parameterClass, parameterMappings);
      builder.parameterMap(parameterMapBuilder.build());
    }

    List<ResultMap> resultMaps = new ArrayList<ResultMap>();
    if (resultMapName != null) {
      resultMaps.add(configuration.getResultMap(resultMapName));
      if (additionalResultMapNames != null) {
        for (String additionalResultMapName : additionalResultMapNames) {
          resultMaps.add(configuration.getResultMap(additionalResultMapName));
        }
      }
    } else if (resultClassName != null) {
      Class resultClass = resolveClass(resultClassName);
      ResultMap.Builder resultMapBuilder = new ResultMap.Builder(configuration, id + "-ResultMap", resultClass, new ArrayList<ResultMapping>());
      resultMaps.add(resultMapBuilder.build());
      if (additionalResultClasses != null) {
        for (Class additionalResultClass : additionalResultClasses) {
          resultMapBuilder = new ResultMap.Builder(configuration, id + "-ResultMap", additionalResultClass, new ArrayList<ResultMapping>());
          resultMaps.add(resultMapBuilder.build());
        }
      }
    }
    builder.resultMaps(resultMaps);

    builder.fetchSize(fetchSizeInt);

    builder.timeout(timeoutInt);

    if (cacheModelName != null) {
      cacheModelName = mapParser.applyNamespace(cacheModelName);
      Cache cache = configuration.getCache(cacheModelName);
      builder.cache(cache);
    }

    if (resultSetType != null) {
      builder.resultSetType(ResultSetType.valueOf(resultSetType));
    }

    // allowRemappingBool -- silently ignored

    findAndParseSelectKey(id, context);

    configuration.addMappedStatement(builder.build());
  }

  private Class resolveClass(String resultClassName) {
    try {
      if (resultClassName != null) {
        return configuration.getTypeAliasRegistry().resolveAlias(resultClassName);
      } else {
        return null;
      }
    } catch (Exception e) {
      throw new SqlMapException("Error.  Could not initialize class.  Cause: " + e, e);
    }
  }

  private void findAndParseSelectKey(String parentId, XNode context) {
    try {
      boolean runStatementFirst = false;
      NodeList children = context.getNode().getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (child.getNodeType() == Node.CDATA_SECTION_NODE
            || child.getNodeType() == Node.TEXT_NODE) {
          String data = ((CharacterData) child).getData();
          if (data.trim().length() > 0) {
            runStatementFirst = true;
          }
        } else if (child.getNodeType() == Node.ELEMENT_NODE
            && "selectKey".equals(child.getNodeName())) {
          buildSelectKeyStatement(parentId, context.newXNode(child), runStatementFirst);

          break;
        }
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error loading result class.  Cause: " + e, e);
    }
  }

  public String getFirstToken(String s) {
    return new StringTokenizer(s, ", ", false).nextToken();
  }

  public String[] getAllButFirstToken(String s) {
    List strings = new ArrayList();
    StringTokenizer parser = new StringTokenizer(s, ", ", false);
    parser.nextToken();
    while (parser.hasMoreTokens()) {
      strings.add(parser.nextToken());
    }
    return (String[]) strings.toArray(new String[strings.size()]);
  }

  private void buildSelectKeyStatement(String parentId, XNode context, boolean runStatementFirstParam) throws ClassNotFoundException {
    final String keyPropName = context.getStringAttribute("keyProperty");
    String resultClassName = context.getStringAttribute("resultClass");
    final SimpleSqlSource source = new SimpleSqlSource(mapParser, context);

    final boolean runStatementFirst = "post".equalsIgnoreCase(context.getStringAttribute("type", runStatementFirstParam ? "post" : "pre"));
    final String keyStatementId = SqlMapSessionImpl.selectKeyIdFor(parentId);
    TypeHandler typeHandler = configuration.getTypeHandlerRegistry().getUnknownTypeHandler();
    if (resultClassName != null) {
      final Class resultClass = configuration.getTypeAliasRegistry().resolveAlias(resultClassName);
      typeHandler = configuration.getTypeHandlerRegistry().getTypeHandler(resultClass);
    }

    ResultMapping.Builder mappingBuilder = new ResultMapping.Builder(configuration, keyPropName, keyPropName, typeHandler);
    ArrayList<ResultMapping> resultMappingArrayList = new ArrayList<ResultMapping>();
    resultMappingArrayList.add(mappingBuilder.build());

    ResultMap.Builder resultMapBuilder = new ResultMap.Builder(configuration, keyStatementId + "ResultMap", HashMap.class, resultMappingArrayList);
    ArrayList<ResultMap> resultMapList = new ArrayList<ResultMap>();
    resultMapList.add(resultMapBuilder.build());

    MappedStatement.Builder builder = new MappedStatement.Builder(configuration, keyStatementId, source, SqlCommandType.SELECT);
    builder.resultMaps(resultMapList);

    configuration.setPostSelectKey(keyStatementId, runStatementFirst);
    configuration.addMappedStatement(builder.build());
  }

}
