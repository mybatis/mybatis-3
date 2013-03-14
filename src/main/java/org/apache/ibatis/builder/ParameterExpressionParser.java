/*
 * Copyright 2012 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Inline parameter expression parser.
 * Supported grammar (simplified):
 * <pre>
 * inline-parameter = (propertyName | expression) oldJdbcType attributes
 * propertyName = /expression language's property navigation path/
 * expression = '(' /expression language's expression/ ')'
 * oldJdbcType = ':' /any valid jdbc type/
 * attributes = (',' attribute)*
 * attribute = name '=' value
 * </pre>
 */
public class ParameterExpressionParser {

  public static Map<String, String> parse(String expression) {
    Map<String, String> map = new HashMap<String, String>();
    parse(expression.toCharArray(), map);
    return map;
  }

  private static void parse(char[] expression, Map<String, String> map) {
    int p = skipWS(expression, 0);
    if (expression[p] == '(') {
      expression(expression, p + 1, map);
    } else {
      property(expression, p, map);
    }
  }

  private static void expression(char[] expression, int left, Map<String, String> map) {
    int match = 1;
    int right = left + 1;
    while (match > 0) {
      if (expression[right] == ')') {
        match--;
      } else if (expression[right] == '(') {
        match++;
      }
      right++;
    }
    map.put("expression", new String(expression, left, right - left - 1));
    jdbcTypeOpt(expression, right, map);
  }

  private static void property(char[] expression, int left, Map<String, String> map) {
    if (left < expression.length) {
      int right = skipUntil(expression, left, ",:");
      map.put("property", trimmedStr(expression, left, right));
      jdbcTypeOpt(expression, right, map);
    }
  }

  private static int skipWS(char[] expression, int p) {
    for (int i = p; i < expression.length; i++) {
      if (expression[i] > 0x20) {
        return i;
      }
    }
    return expression.length;
  }

  private static int skipUntil(char[] expression, int p, final String endChars) {
    for (int i = p; i < expression.length; i++) {
      char c = expression[i];
      if (endChars.indexOf(c) > -1) {
        return i;
      }
    }
    return expression.length;
  }

  private static void jdbcTypeOpt(char[] expression, int p, Map<String, String> map) {
    p = skipWS(expression, p);
    if (p < expression.length) {
      if (expression[p] == ':') {
        jdbcType(expression, p + 1, map);
      } else if (expression[p] == ',') {
        option(expression, p + 1, map);
      } else {
        throw new BuilderException("Parsing error in {" + new String(expression) + "} in position " + p);
      }
    }
  }

  private static void jdbcType(char[] expression, int p, Map<String, String> map) {
    int left = skipWS(expression, p);
    int right = skipUntil(expression, left, ",");
    if (right > left) {
      map.put("jdbcType", trimmedStr(expression, left, right));
    } else {
      throw new BuilderException("Parsing error in {" + new String(expression) + "} in position " + p);
    }
    option(expression, right + 1, map);
  }

  private static void option(char[] expression, int p, Map<String, String> map) {
    int left = skipWS(expression, p);
    if (left < expression.length) {
      int right = skipUntil(expression, left, "=");
      String name = trimmedStr(expression, left, right);
      left = right + 1;
      right = skipUntil(expression, left, ",");
      String value = trimmedStr(expression, left, right);
      map.put(name, value);
      option(expression, right + 1, map);
    }
  }

  private static String trimmedStr(char[] arr, int start, int end)
  {
    while (arr[start] <= 0x20)
    {
      start++;
    }
    while (arr[end - 1] <= 0x20)
    {
      end--;
    }
    return start >= end ? "" : new String(arr, start, end - start);
  }
}
