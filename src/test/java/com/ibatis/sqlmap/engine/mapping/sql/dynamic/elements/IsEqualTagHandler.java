package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsEqualTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    return compare(ctx, tag, parameterObject) == 0;
  }

}
