package org.apache.ibatis.mapping;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResultMapping {

  private Configuration configuration;
  private String property;
  private String column;
  private Class javaType;
  private JdbcType jdbcType;
  private TypeHandler typeHandler;
  private String nestedResultMapId;
  private String nestedQueryId;
  private List<ResultFlag> flags;
  private List<ResultMapping> composites;

  private ResultMapping() {
  }

  public static class Builder {
    private ResultMapping resultMapping = new ResultMapping();

    public Builder(Configuration configuration, String property, String column, TypeHandler typeHandler) {
      resultMapping.configuration = configuration;
      resultMapping.property = property;
      resultMapping.column = column;
      resultMapping.typeHandler = typeHandler;
      resultMapping.flags = new ArrayList<ResultFlag>();
      resultMapping.composites = new ArrayList<ResultMapping>();
    }

    public Builder(Configuration configuration, String property, String column, Class javaType) {
      resultMapping.configuration = configuration;
      resultMapping.property = property;
      resultMapping.column = column;
      resultMapping.javaType = javaType;
      resultMapping.flags = new ArrayList<ResultFlag>();
      resultMapping.composites = new ArrayList<ResultMapping>();
    }

    public Builder(Configuration configuration, String property) {
      resultMapping.configuration = configuration;
      resultMapping.property = property;
      resultMapping.flags = new ArrayList<ResultFlag>();
      resultMapping.composites = new ArrayList<ResultMapping>();
    }

    public Builder javaType(Class javaType) {
      resultMapping.javaType = javaType;
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      resultMapping.jdbcType = jdbcType;
      return this;
    }

    public Builder nestedResultMapId(String nestedResultMapId) {
      resultMapping.nestedResultMapId = nestedResultMapId;
      return this;
    }

    public Builder nestedQueryId(String nestedQueryId) {
      resultMapping.nestedQueryId = nestedQueryId;
      return this;
    }

    public Builder flags(List<ResultFlag> flags) {
      resultMapping.flags = flags;
      return this;
    }

    public Builder typeHandler(TypeHandler typeHandler) {
      resultMapping.typeHandler = typeHandler;
      return this;
    }

    public Builder composites(List<ResultMapping> composites) {
      resultMapping.composites = composites;
      return this;
    }

    public ResultMapping build() {
      //lock down collections
      resultMapping.flags = Collections.unmodifiableList(resultMapping.flags);
      resultMapping.composites = Collections.unmodifiableList(resultMapping.composites);
      resolveTypeHandler();
      return resultMapping;
    }

    private void resolveTypeHandler() {
      if (resultMapping.typeHandler == null) {
        if (resultMapping.javaType != null) {
          Configuration configuration = resultMapping.configuration;
          TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
          resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, resultMapping.jdbcType);
        }
      }
    }

    public Builder column(String column) {
      resultMapping.column = column;
      return this;
    }
  }

  public String getProperty() {
    return property;
  }

  public String getColumn() {
    return column;
  }

  public Class getJavaType() {
    return javaType;
  }

  public JdbcType getJdbcType() {
    return jdbcType;
  }

  public TypeHandler getTypeHandler() {
    return typeHandler;
  }

  public String getNestedResultMapId() {
    return nestedResultMapId;
  }

  public String getNestedQueryId() {
    return nestedQueryId;
  }

  public List<ResultFlag> getFlags() {
    return flags;
  }

  public List<ResultMapping> getComposites() {
    return composites;
  }

  public boolean isCompositeResult() {
    return this.composites != null && !this.composites.isEmpty();
  }

}
