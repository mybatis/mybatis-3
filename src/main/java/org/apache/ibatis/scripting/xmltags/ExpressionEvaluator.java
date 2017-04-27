/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.scripting.xmltags;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.BuilderException;

/**
 * @author Clinton Begin
 */
public class ExpressionEvaluator {

  public boolean evaluateBoolean(String expression, Object parameterObject) {
    Object value = OgnlCache.getValue(expression, parameterObject);
    if (value instanceof Boolean) {
      return (Boolean) value;
    }
    if (value instanceof Number) {
        return !new BigDecimal(String.valueOf(value)).equals(BigDecimal.ZERO);
    }
    return value != null;
  }

  public Iterable<?> evaluateIterable(String expression, Object parameterObject) {
    Object value = OgnlCache.getValue(expression, parameterObject);
    if (value == null) {
      throw new BuilderException("The expression '" + expression + "' evaluated to a null value.");
    }
    if (value instanceof Iterable) {
      return (Iterable<?>) value;
    }
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
    if (value instanceof Map) {
      return ((Map) value).entrySet();
    }
    throw new BuilderException("Error evaluating expression '" + expression + "'.  Return value (" + value + ") was not iterable.");
  }

}
