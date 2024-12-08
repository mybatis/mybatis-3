/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.mapping;

import java.sql.ResultSet;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

/**
 * @author Clinton Begin
 */
public class ParameterMapping {

  private ParameterMappingConfig config;
  private String property;
  private ParameterMode mode;
  private Integer numericScale;
  private String resultMapId;
  private String jdbcTypeName;
  private String expression;

  private ParameterMapping() {
    this.config = new ParameterMappingConfig();
  }

  public static class Builder {
    private final ParameterMapping parameterMapping = new ParameterMapping();

    public Builder(Configuration configuration, String property, TypeHandler<?> typeHandler) {
      parameterMapping.config.setConfiguration(configuration);
      parameterMapping.property = property;
      parameterMapping.config.setTypeHandler(typeHandler);
      parameterMapping.mode = ParameterMode.IN;
    }

    public Builder(Configuration configuration, String property, Class<?> javaType) {
      parameterMapping.config.setConfiguration(configuration);
      parameterMapping.property = property;
      parameterMapping.config.setJavaType(javaType);
      parameterMapping.mode = ParameterMode.IN;
    }

    public Builder mode(ParameterMode mode) {
      parameterMapping.mode = mode;
      return this;
    }

    public Builder javaType(Class<?> javaType) {
      parameterMapping.config.setJavaType(javaType);
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      parameterMapping.config.setJdbcType(jdbcType);
      return this;
    }

    public Builder numericScale(Integer numericScale) {
      parameterMapping.numericScale = numericScale;
      return this;
    }

    public Builder resultMapId(String resultMapId) {
      parameterMapping.resultMapId = resultMapId;
      return this;
    }

    public Builder typeHandler(TypeHandler<?> typeHandler) {
      parameterMapping.config.setTypeHandler(typeHandler);
      return this;
    }

    public Builder jdbcTypeName(String jdbcTypeName) {
      parameterMapping.jdbcTypeName = jdbcTypeName;
      return this;
    }

    public Builder expression(String expression) {
      parameterMapping.expression = expression;
      return this;
    }

    public ParameterMapping build() {
      resolveTypeHandler();
      validate();
      return parameterMapping;
    }

    private void validate() {
      if (ResultSet.class.equals(parameterMapping.config.getJavaType())) {
        if (parameterMapping.resultMapId == null) {
          throw new IllegalStateException("Missing resultmap in property '" + parameterMapping.property + "'.  "
              + "Parameters of type java.sql.ResultSet require a resultmap.");
        }
      } else if (parameterMapping.config.getTypeHandler() == null) {
        throw new IllegalStateException("Type handler was null on parameter mapping for property '"
            + parameterMapping.property + "'. It was either not specified and/or could not be found for the javaType ("
            + parameterMapping.config.getJavaType().getName() + ") : jdbcType (" + parameterMapping.config.getJdbcType()
            + ") combination.");
      }
    }

    private void resolveTypeHandler() {
      if (parameterMapping.config.getTypeHandler() == null && parameterMapping.config.getJavaType() != null) {
        Configuration configuration = parameterMapping.config.getConfiguration();
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        parameterMapping.config.setTypeHandler(typeHandlerRegistry.getTypeHandler(parameterMapping.config.getJavaType(),
            parameterMapping.config.getJdbcType()));
      }
    }
  }

  public String getProperty() {
    return property;
  }

  public ParameterMode getMode() {
    return mode;
  }

  public Class<?> getJavaType() {
    return config.getJavaType();
  }

  public JdbcType getJdbcType() {
    return config.getJdbcType();
  }

  public Integer getNumericScale() {
    return numericScale;
  }

  public TypeHandler<?> getTypeHandler() {
    return config.getTypeHandler();
  }

  public String getResultMapId() {
    return resultMapId;
  }

  public String getJdbcTypeName() {
    return jdbcTypeName;
  }

  public String getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ParameterMapping{");
    sb.append("property='").append(property).append('\'');
    sb.append(", mode=").append(mode);
    sb.append(", javaType=").append(config.getJavaType());
    sb.append(", jdbcType=").append(config.getJdbcType());
    sb.append(", numericScale=").append(numericScale);
    sb.append(", resultMapId='").append(resultMapId).append('\'');
    sb.append(", jdbcTypeName='").append(jdbcTypeName).append('\'');
    sb.append(", expression='").append(expression).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
