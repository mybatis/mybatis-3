/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.builder;

import java.util.HashMap;

/**
 * Inline parameter expression parser. Supported grammar (simplified):
 *
 * <pre>
 * inline-parameter = (propertyName | expression) oldJdbcType attributes
 * propertyName = /expression language's property navigation path/
 * expression = '(' /expression language's expression/ ')'
 * oldJdbcType = ':' /any valid jdbc type/
 * attributes = (',' attribute)*
 * attribute = name '=' value
 * </pre>
 *
 * @author Frank D. Martinez [mnesarco]
 */
public class ParameterExpression extends HashMap<String, String> {

  private static final long serialVersionUID = -2417552199605158680L;

  public ParameterExpression(String expression) {
    parse(expression);
  }

  private void parse(String expression) {
    int p = skipWS(expression, 0);
    if (expression.charAt(p) == '(') {
      expression(expression, p + 1);
    } else {
      property(expression, p);
    }
  }

  private void expression(String expression, int left) {
    int match = 1;
    int right = left + 1;
    while (match > 0) {
      if (expression.charAt(right) == ')') {
        match--;
      } else if (expression.charAt(right) == '(') {
        match++;
      }
      right++;
    }
    put("expression", expression.substring(left, right - 1));
    jdbcTypeOpt(expression, right);
  }

  private void property(String expression, int left) {
    if (left < expression.length()) {
      int right = skipUntil(expression, left, ",:");
      put("property", trimmedStr(expression, left, right));
      jdbcTypeOpt(expression, right);
    }
  }

  private int skipWS(String expression, int p) {
    for (int i = p; i < expression.length(); i++) {
      if (expression.charAt(i) > 0x20) {
        return i;
      }
    }
    return expression.length();
  }

  private int skipUntil(String expression, int p, final String endChars) {
    for (int i = p; i < expression.length(); i++) {
      char c = expression.charAt(i);
      if (endChars.indexOf(c) > -1) {
        return i;
      }
    }
    return expression.length();
  }

  private void jdbcTypeOpt(String expression, int p) {
    p = skipWS(expression, p);
    if (p < expression.length()) {
      if (expression.charAt(p) == ':') {
        jdbcType(expression, p + 1);
      } else if (expression.charAt(p) == ',') {
        option(expression, p + 1);
      } else {
        throw new BuilderException("Parsing error in {" + expression + "} in position " + p);
      }
    }
  }

  private void jdbcType(String expression, int p) {
    int left = skipWS(expression, p);
    int right = skipUntil(expression, left, ",");
    if (right > left) {
      put("jdbcType", trimmedStr(expression, left, right));
    } else {
      throw new BuilderException("Parsing error in {" + expression + "} in position " + p);
    }
    option(expression, right + 1);
  }

  private void option(String expression, int p) {
    int left = skipWS(expression, p);
    if (left < expression.length()) {
      int right = skipUntil(expression, left, "=");
      String name = trimmedStr(expression, left, right);
      left = right + 1;
      right = skipUntil(expression, left, ",");
      String value = trimmedStr(expression, left, right);
      put(name, value);
      option(expression, right + 1);
    }
  }

  private String trimmedStr(String str, int start, int end) {
    while (str.charAt(start) <= 0x20) {
      start++;
    }
    while (str.charAt(end - 1) <= 0x20) {
      end--;
    }
    return start >= end ? "" : str.substring(start, end);
  }

}
