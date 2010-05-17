package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import org.apache.ibatis.reflection.MetaObject;

import java.lang.reflect.Array;
import java.util.Collection;

public class IsEmptyTagHandler extends ConditionalTagHandler {

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
      if (value instanceof Collection) {
        return value == null || ((Collection) value).size() < 1;
      } else if (value != null && value.getClass().isArray()) {
        return Array.getLength(value) == 0;
      } else {
        return value == null || String.valueOf(value).equals("");
      }
    }
  }

}
