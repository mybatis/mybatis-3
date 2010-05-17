package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public interface SqlTagHandler {

  // BODY TAG
  public static final int SKIP_BODY = 0;
  public static final int INCLUDE_BODY = 1;
  public static final int REPEAT_BODY = 2;

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject);

  public int doEndFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent);

  public void doPrepend(SqlTagContext ctx, SqlTag tag, Object parameterObject, StringBuffer bodyContent);

}
