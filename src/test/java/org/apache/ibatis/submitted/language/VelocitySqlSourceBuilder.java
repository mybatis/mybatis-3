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
package org.apache.ibatis.submitted.language;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.ParameterExpression;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

/**
 * Just a test case. Not a real Velocity implementation.
 */
public class VelocitySqlSourceBuilder extends BaseBuilder {

  private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

  public VelocitySqlSourceBuilder(Configuration configuration) {
    super(configuration);
  }

  public SqlSource parse(String originalSql, Class<?> parameterType) {
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType);
    GenericTokenParser parser = new GenericTokenParser("@{", "}", handler);
    String sql = parser.parse(originalSql);
    return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
  }

  private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

    private final List<ParameterMapping> parameterMappings = new ArrayList<>();
    private final Class<?> parameterType;

    public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType) {
      super(configuration);
      this.parameterType = parameterType;
    }

    public List<ParameterMapping> getParameterMappings() {
      return parameterMappings;
    }

    @Override
    public String handleToken(String content) {
      parameterMappings.add(buildParameterMapping(content));
      return "?";
    }

    private ParameterMapping buildParameterMapping(String content) {
      Map<String, String> propertiesMap = parseParameterMapping(content);
      String property = propertiesMap.get("property");
      JdbcType jdbcType = resolveJdbcType(propertiesMap.get("jdbcType"));
      Class<?> propertyType;
      if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
        propertyType = parameterType;
      } else if (JdbcType.CURSOR.equals(jdbcType)) {
        propertyType = ResultSet.class;
      } else if (property != null) {
        MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
        if (metaClass.hasGetter(property)) {
          propertyType = metaClass.getGetterType(property);
        } else {
          propertyType = Object.class;
        }
      } else {
        propertyType = Object.class;
      }
      ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
      if (jdbcType != null) {
        builder.jdbcType(jdbcType);
      }
      Class<?> javaType = null;
      String typeHandlerAlias = null;
      for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
        String name = entry.getKey();
        String value = entry.getValue();
        if (name != null) {
          switch (name) {
            case "javaType":
              javaType = resolveClass(value);
              builder.javaType(javaType);
              break;
            case "mode":
              builder.mode(resolveParameterMode(value));
              break;
            case "numericScale":
              builder.numericScale(Integer.valueOf(value));
              break;
            case "resultMap":
              builder.resultMapId(value);
              break;
            case "typeHandler":
              typeHandlerAlias = value;
              break;
            case "jdbcTypeName":
              builder.jdbcTypeName(value);
              break;
            case "maskLog":
              builder.maskLog(value);
              break;
            case "property":
              break;
            case "expression":
              builder.expression(value);
              break;
            default:
              throw new BuilderException("An invalid property '" + name + "' was found in mapping @{" + content
                  + "}.  Valid properties are " + parameterProperties);
          }
        } else {
          throw new BuilderException("An invalid property '" + name + "' was found in mapping @{" + content
              + "}.  Valid properties are " + parameterProperties);
        }
      }
      if (typeHandlerAlias != null) {
        builder.typeHandler(resolveTypeHandler(javaType, propertyType, jdbcType, typeHandlerAlias));
      }
      return builder.build();
    }

    private Map<String, String> parseParameterMapping(String content) {
      try {
        return new ParameterExpression(content);
      } catch (BuilderException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new BuilderException("Parsing error was found in mapping @{" + content
            + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
      }
    }
  }

}
