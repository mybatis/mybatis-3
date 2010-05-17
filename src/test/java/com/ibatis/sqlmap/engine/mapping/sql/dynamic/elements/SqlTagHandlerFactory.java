package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import java.util.HashMap;
import java.util.Map;

public class SqlTagHandlerFactory {

  private static final Map HANDLER_MAP = new HashMap();

  static {
    HANDLER_MAP.put("isEmpty", new IsEmptyTagHandler());
    HANDLER_MAP.put("isEqual", new IsEqualTagHandler());
    HANDLER_MAP.put("isGreaterEqual", new IsGreaterEqualTagHandler());
    HANDLER_MAP.put("isGreaterThan", new IsGreaterThanTagHandler());
    HANDLER_MAP.put("isLessEqual", new IsLessEqualTagHandler());
    HANDLER_MAP.put("isLessThan", new IsLessThanTagHandler());
    HANDLER_MAP.put("isNotEmpty", new IsNotEmptyTagHandler());
    HANDLER_MAP.put("isNotEqual", new IsNotEqualTagHandler());
    HANDLER_MAP.put("isNotNull", new IsNotNullTagHandler());
    HANDLER_MAP.put("isNotParameterPresent", new IsNotParameterPresentTagHandler());
    HANDLER_MAP.put("isNotPropertyAvailable", new IsNotPropertyAvailableTagHandler());
    HANDLER_MAP.put("isNull", new IsNullTagHandler());
    HANDLER_MAP.put("isParameterPresent", new IsParameterPresentTagHandler());
    HANDLER_MAP.put("isPropertyAvailable", new IsPropertyAvailableTagHandler());
    HANDLER_MAP.put("iterate", new IterateTagHandler());
    HANDLER_MAP.put("dynamic", new DynamicTagHandler());
  }

  private SqlTagHandlerFactory() {
  }

  public static SqlTagHandler getSqlTagHandler(String name) {
    return (SqlTagHandler) HANDLER_MAP.get(name);
  }

}
