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
package org.apache.ibatis.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class SqlSourceBuilder extends BaseBuilder {
  
  private static final Set<String> parameterProperties = new HashSet<String>(Arrays.asList(new String[] { 
      "javaType", 
      "jdbcType", 
      "mode", 
      "numericScale", 
      "resultMap",
      "typeHandler", 
      "jdbcTypeName" }));

  public SqlSourceBuilder(Configuration configuration) {
    super(configuration);
  }

  public SqlSource parse(String originalSql, Class<?> parameterType) {
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType);
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    String sql = parser.parse(originalSql);
    return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
  }

  private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

    private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
    private Class<?> parameterType;

    public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType) {
      super(configuration);
      this.parameterType = parameterType;
    }

    public List<ParameterMapping> getParameterMappings() {
      return parameterMappings;
    }

    public String handleToken(String content) {
      parameterMappings.add(buildParameterMapping(content));
      return "?";
    }

    private ParameterMapping buildParameterMapping(String content) {
      Map<String, String> propertiesMap = parseParameterMapping(content);
      String property = propertiesMap.get("property");
      String jdbcType = propertiesMap.get("jdbcType");
      Class<?> propertyType;
      MetaClass metaClass = MetaClass.forClass(parameterType);
      if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
        propertyType = parameterType;
      } else if (JdbcType.CURSOR.name().equals(jdbcType)) {
        propertyType = java.sql.ResultSet.class;
      } else if (metaClass.hasGetter(property)) {
        propertyType = metaClass.getGetterType(property);
      } else {
        propertyType = Object.class;
      }
      ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
      if (jdbcType != null) {
        builder.jdbcType(resolveJdbcType(jdbcType));
      }
      Class<?> javaType = null;
      String typeHandlerAlias = null;
      for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
        String name = entry.getKey();
        String value = entry.getValue();
        if ("javaType".equals(name)) {
          javaType = resolveClass(value);
          builder.javaType(javaType);
        } else if ("jdbcType".equals(name)) {
          builder.jdbcType(resolveJdbcType(value));
        } else if ("mode".equals(name)) {
          builder.mode(resolveParameterMode(value));
        } else if ("numericScale".equals(name)) {
          builder.numericScale(Integer.valueOf(value));
        } else if ("resultMap".equals(name)) {
          builder.resultMapId(value);
        } else if ("typeHandler".equals(name)) {
          typeHandlerAlias = value;
        } else if ("jdbcTypeName".equals(name)) {
          builder.jdbcTypeName(value);
        }
      }
      if (typeHandlerAlias != null) {
        builder.typeHandler((TypeHandler<?>) resolveTypeHandler(javaType, typeHandlerAlias));
      }
      return builder.build();
    }

    private Map<String, String> parseParameterMapping(String content) {
      Map<String, String> map = new HashMap<String, String>();
      StringTokenizer parameterMappingParts = new StringTokenizer(content, ", \n\r\t");
      String propertyWithJdbcType = parameterMappingParts.nextToken();
      String property = extractPropertyName(propertyWithJdbcType);
      map.put("property", property);
      String jdbcType = extractJdbcType(propertyWithJdbcType);
      if (jdbcType != null) map.put("jdbcType", jdbcType); // support old style #{property:TYPE} format
      while (parameterMappingParts.hasMoreTokens()) {
        String attribute = parameterMappingParts.nextToken();
        StringTokenizer attributeParts = new StringTokenizer(attribute, "=");
        if (attributeParts.countTokens() == 2) {
          String name = attributeParts.nextToken();
          String value = attributeParts.nextToken();
          if (parameterProperties.contains(name)) {
            map.put(name, value);
          } else {
            throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content + "}.  Valid properties are " + parameterProperties);
          }
        } else {
          throw new BuilderException("Improper inline parameter map format.  Should be: #{propName,attr1=val1,attr2=val2}");
        }
      }
      return map;
    }

    private String extractPropertyName(String property) {
      if (property.contains(":")) {
        StringTokenizer simpleJdbcTypeParser = new StringTokenizer(property, ": ");
        if (simpleJdbcTypeParser.countTokens() == 2) {
          return simpleJdbcTypeParser.nextToken();
        }
      }
      return property;
    }

    private String extractJdbcType(String property) {
      if (property.contains(":")) {
        StringTokenizer simpleJdbcTypeParser = new StringTokenizer(property, ": ");
        if (simpleJdbcTypeParser.countTokens() == 2) {
          simpleJdbcTypeParser.nextToken();
          return simpleJdbcTypeParser.nextToken();
        }
      }
      return null;
    }

  }
}
