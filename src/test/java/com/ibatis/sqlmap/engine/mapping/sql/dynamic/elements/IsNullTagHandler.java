package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import org.apache.ibatis.reflection.MetaObject;

public class IsNullTagHandler extends ConditionalTagHandler {

  public boolean isCondition(SqlTagContext ctx, SqlTag tag, Object parameterObject) {
    if (parameterObject == null) {
      return true;
    } else {
      String prop = getResolvedProperty(ctx, tag);
      Object value;
      if (prop != null) {
        value = MetaObject.forObject(parameterObject).getValue(prop);
      } else {
        value = parameterObject;
      }
      return value == null;
    }
  }


}
