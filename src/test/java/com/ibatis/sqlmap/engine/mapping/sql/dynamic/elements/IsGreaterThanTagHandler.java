package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class IsGreaterThanTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    long x = compare(ctx, tag, parameterObject);
    return x > 0 && x != NOT_COMPARABLE;
  }

}
