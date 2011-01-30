package org.apache.ibatis.builder.xml.dynamic;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlException;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.SqlMapperException;

public class ExpressionEvaluator {

  public boolean evaluateBoolean(String expression, Object parameterObject) {
    try {
      Object value = Ognl.getValue(expression, parameterObject);
      if (value instanceof Boolean) return (Boolean) value;
      if (value instanceof Number) return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
      return value != null;
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
    }
  }

  public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
    try {
      Object value = Ognl.getValue(expression, parameterObject);
      if (value == null) throw new SqlMapperException("The expression '" + expression + "' evaluated to a null value.");
      if (value instanceof Iterable) return (Iterable<?>) value;
      if (value.getClass().isArray()) {
          // the array may be primitive, so Arrays.asList() may throw 
          // a ClassCastException (issue 209).  Do the work manually
          // Curse primitives! :) (JGB)
          int size = Array.getLength(value);
          List<Object> answer = new ArrayList<Object>();
          for (int i = 0; i < size; i++) {
              Object o = Array.get(value, i);
              answer.add(o);
          }
          
          return answer;
      }
      throw new BuilderException("Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
    } catch (OgnlException e) {
      throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
    }
  }


}
