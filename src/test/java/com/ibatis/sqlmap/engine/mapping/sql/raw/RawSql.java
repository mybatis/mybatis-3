package com.ibatis.sqlmap.engine.mapping.sql.raw;

import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ParameterMapping;

import java.util.List;

/**
 * A non-executable SQL container simply for
 * communicating raw SQL around the framework.
 */
public class RawSql implements Sql {

  private String sql;

  public RawSql(String sql) {
    this.sql = sql;
  }

  public String getSql(Object parameterObject) {
    return sql;
  }

  public List<ParameterMapping> getParameterMappings(Object parameterObject) {
    return null;
  }

  public ParameterMap getParameterMap(Object parameterObject) {
    throw new RuntimeException("Method not implemented on RawSql.");
  }


}


