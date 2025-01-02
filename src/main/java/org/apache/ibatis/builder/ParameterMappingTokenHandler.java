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

import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

public class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

  private static final String PARAMETER_PROPERTIES = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";
  private final List<ParameterMapping> parameterMappings;
  private final Class<?> parameterType;
  private final MetaObject metaParameters;
  private final Object parameterObject;
  private final boolean paramExists;

  public ParameterMappingTokenHandler(List<ParameterMapping> parameterMappings, Configuration configuration,
      Object parameterObject, Class<?> parameterType, Map<String, Object> additionalParameters, boolean paramExists) {
    super(configuration);
    this.parameterType = parameterObject == null ? (parameterType == null ? Object.class : parameterType)
        : parameterObject.getClass();
    this.metaParameters = configuration.newMetaObject(additionalParameters);
    this.parameterObject = parameterObject;
    this.paramExists = paramExists;
    this.parameterMappings = parameterMappings;
  }

  public ParameterMappingTokenHandler(List<ParameterMapping> parameterMappings, Configuration configuration,
      Class<?> parameterType, Map<String, Object> additionalParameters) {
    super(configuration);
    this.parameterType = parameterType;
    this.metaParameters = configuration.newMetaObject(additionalParameters);
    this.parameterObject = null;
    this.paramExists = false;
    this.parameterMappings = parameterMappings;
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
    PropertyTokenizer propertyTokenizer = new PropertyTokenizer(property);
    Class<?> propertyType;
    if (metaParameters.hasGetter(propertyTokenizer.getName())) { // issue #448 get type from additional params
      propertyType = metaParameters.getGetterType(property);
    } else if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
      propertyType = parameterType;
    } else if (JdbcType.CURSOR.name().equals(propertiesMap.get("jdbcType"))) {
      propertyType = java.sql.ResultSet.class;
    } else if (property == null || Map.class.isAssignableFrom(parameterType)) {
      propertyType = Object.class;
    } else {
      MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
      if (metaClass.hasGetter(property)) {
        propertyType = metaClass.getGetterType(property);
      } else {
        propertyType = Object.class;
      }
    }
    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
    Class<?> javaType = propertyType;
    String typeHandlerAlias = null;
    ParameterMode mode = null;
    for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue();
      if ("javaType".equals(name)) {
        javaType = resolveClass(value);
        builder.javaType(javaType);
      } else if ("jdbcType".equals(name)) {
        builder.jdbcType(resolveJdbcType(value));
      } else if ("mode".equals(name)) {
        mode = resolveParameterMode(value);
        builder.mode(mode);
      } else if ("numericScale".equals(name)) {
        builder.numericScale(Integer.valueOf(value));
      } else if ("resultMap".equals(name)) {
        builder.resultMapId(value);
      } else if ("typeHandler".equals(name)) {
        typeHandlerAlias = value;
      } else if ("jdbcTypeName".equals(name)) {
        builder.jdbcTypeName(value);
      } else if ("property".equals(name)) {
        // Do Nothing
      } else if ("expression".equals(name)) {
        throw new BuilderException("Expression based parameters are not supported yet");
      } else {
        throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content
            + "}.  Valid properties are " + PARAMETER_PROPERTIES);
      }
    }
    if (typeHandlerAlias != null) {
      builder.typeHandler(resolveTypeHandler(javaType, typeHandlerAlias));
    }
    if (!ParameterMode.OUT.equals(mode) && paramExists) {
      if (metaParameters.hasGetter(propertyTokenizer.getName())) {
        builder.value(metaParameters.getValue(property));
      } else if (parameterObject == null) {
        builder.value(null);
      } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
        builder.value(parameterObject);
      } else {
        MetaObject metaObject = configuration.newMetaObject(parameterObject);
        builder.value(metaObject.getValue(property));
      }
    }
    return builder.build();
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
