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
package org.apache.ibatis.builder;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * @author Clinton Begin
 */
public class SqlSourceBuilder extends BaseBuilder {

  private static final String PARAMETER_PROPERTIES = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

  public SqlSourceBuilder(Configuration configuration) {
    super(configuration);
  }

  public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
    return parse(originalSql, parameterType, additionalParameters, null);
  }

  public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters,
      ParamNameResolver paramNameResolver) {
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType,
        additionalParameters, paramNameResolver);
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
    String sql;
    if (configuration.isShrinkWhitespacesInSql()) {
      sql = parser.parse(removeExtraWhitespaces(originalSql));
    } else {
      sql = parser.parse(originalSql);
    }
    return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
  }

  public static String removeExtraWhitespaces(String original) {
    StringTokenizer tokenizer = new StringTokenizer(original);
    StringBuilder builder = new StringBuilder();
    boolean hasMoreTokens = tokenizer.hasMoreTokens();
    while (hasMoreTokens) {
      builder.append(tokenizer.nextToken());
      hasMoreTokens = tokenizer.hasMoreTokens();
      if (hasMoreTokens) {
        builder.append(' ');
      }
    }
    return builder.toString();
  }

  private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

    private final List<ParameterMapping> parameterMappings = new ArrayList<>();
    private final Class<?> parameterType;
    private final MetaObject metaParameters;
    private final ParamNameResolver paramNameResolver;

    private Type genericType = null;
    private TypeHandler<?> typeHandler = null;

    public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType,
        Map<String, Object> additionalParameters, ParamNameResolver paramNameResolver) {
      super(configuration);
      this.parameterType = parameterType;
      this.metaParameters = configuration.newMetaObject(additionalParameters);
      this.paramNameResolver = paramNameResolver;
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
      final String property = propertiesMap.remove("property");
      final JdbcType jdbcType = resolveJdbcType(propertiesMap.remove("jdbcType"));
      final String typeHandlerAlias = propertiesMap.remove("typeHandler");
      ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, (Class<?>) null);
      builder.jdbcType(jdbcType);
      final Class<?> javaType = figureOutJavaType(propertiesMap, property, jdbcType);
      builder.javaType(javaType);
      if (genericType == null) {
        genericType = javaType;
      }
      if (typeHandler == null || typeHandlerAlias != null) {
        typeHandler = resolveTypeHandler(parameterType, property, genericType, jdbcType, typeHandlerAlias);
      }
      builder.typeHandler(typeHandler);

      for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
        String name = entry.getKey();
        String value = entry.getValue();
        if ("mode".equals(name)) {
          builder.mode(resolveParameterMode(value));
        } else if ("numericScale".equals(name)) {
          builder.numericScale(Integer.valueOf(value));
        } else if ("resultMap".equals(name)) {
          builder.resultMapId(value);
        } else if ("jdbcTypeName".equals(name)) {
          builder.jdbcTypeName(value);
        } else if ("expression".equals(name)) {
          throw new BuilderException("Expression based parameters are not supported yet");
        } else {
          throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content
              + "}.  Valid properties are " + PARAMETER_PROPERTIES);
        }
      }
      return builder.build();
    }

    private Class<?> figureOutJavaType(Map<String, String> propertiesMap, String property, JdbcType jdbcType) {
      Class<?> javaType = resolveClass(propertiesMap.remove("javaType"));
      if (javaType != null) {
        return javaType;
      }
      if (metaParameters.hasGetter(property)) { // issue #448 get type from additional params
        return metaParameters.getGetterType(property);
      }
      typeHandler = resolveTypeHandler(parameterType, null, null, jdbcType, (Class<? extends TypeHandler<?>>) null);
      if (typeHandler != null) {
        return parameterType;
      }
      if (JdbcType.CURSOR.equals(jdbcType)) {
        return ResultSet.class;
      }
      if (paramNameResolver != null && ParamMap.class.equals(parameterType)) {
        Type actualParamType = paramNameResolver.getType(property);
        if (actualParamType instanceof Type) {
          MetaClass metaClass = MetaClass.forClass(actualParamType, configuration.getReflectorFactory());
          PropertyTokenizer propertyTokenizer = new PropertyTokenizer(property);
          String multiParamsPropertyName;
          if (propertyTokenizer.hasNext()) {
            multiParamsPropertyName = propertyTokenizer.getChildren();
            if (metaClass.hasGetter(multiParamsPropertyName)) {
              Entry<Type, Class<?>> getterType = metaClass.getGenericGetterType(multiParamsPropertyName);
              genericType = getterType.getKey();
              return getterType.getValue();
            }
          } else {
            genericType = actualParamType;
          }
        }
        return Object.class;
      }
      if (Map.class.isAssignableFrom(parameterType)) {
        return Object.class;
      }
      MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
      if (metaClass.hasGetter(property)) {
        Entry<Type, Class<?>> getterType = metaClass.getGenericGetterType(property);
        genericType = getterType.getKey();
        return getterType.getValue();
      }
      return Object.class;
    }

    private Map<String, String> parseParameterMapping(String content) {
      try {
        return new ParameterExpression(content);
      } catch (BuilderException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new BuilderException("Parsing error was found in mapping #{" + content
            + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
      }
    }
  }

}
