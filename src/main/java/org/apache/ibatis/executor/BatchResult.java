package org.apache.ibatis.executor;

import org.apache.ibatis.mapping.MappedStatement;

public class BatchResult {

  private final MappedStatement mappedStatement;
  private final String sql;
  private final Object parameterObject;

  private int[] updateCounts;

  public BatchResult(MappedStatement mappedStatement, String sql, Object parameterObject) {
    super();
    this.mappedStatement = mappedStatement;
    this.sql = sql;
    this.parameterObject = parameterObject;
  }

  public MappedStatement getMappedStatement() {
    return mappedStatement;
  }

  public String getSql() {
    return sql;
  }

  public Object getParameterObject() {
    return parameterObject;
  }

  public int[] getUpdateCounts() {
    return updateCounts;
  }

  public void setUpdateCounts(int[] updateCounts) {
    this.updateCounts = updateCounts;
  }

}
