package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

public class DynamicTagHandler extends BaseTagHandler {

  public int doStartFragment(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    ctx.pushRemoveFirstPrependMarker(tag);
    return INCLUDE_BODY;
  }

}


