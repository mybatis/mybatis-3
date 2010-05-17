package com.ibatis.sqlmap.engine.mapping.sql.statik;

import com.ibatis.sqlmap.engine.mapping.sql.Sql;
import org.apache.ibatis.mapping.ParameterMapping;

import java.util.List;

public class StaticSql implements Sql {

  private String sqlStatement;

  public StaticSql(String sqlStatement) {
    this.sqlStatement = sqlStatement.replace('\r', ' ').replace('\n', ' ');
  }

  public String getSql(Object parameterObject) {
    return sqlStatement;
  }

  public List<ParameterMapping> getParameterMappings(Object parameterObject) {
    return null;
  }
}
