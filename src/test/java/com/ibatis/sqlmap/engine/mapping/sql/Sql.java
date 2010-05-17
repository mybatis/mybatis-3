package com.ibatis.sqlmap.engine.mapping.sql;

import org.apache.ibatis.mapping.ParameterMapping;

import java.util.List;

public interface Sql {

  public String getSql(Object parameterObject);

  public List<ParameterMapping> getParameterMappings(Object parameterObject);

}
