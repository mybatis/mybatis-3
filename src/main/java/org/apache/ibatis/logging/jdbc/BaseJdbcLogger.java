/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.logging.jdbc;

import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.reflection.ArrayUtil;

/**
 * Base class for proxies to do logging.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public abstract class BaseJdbcLogger {

  protected static final Set<String> SET_METHODS;
  protected static final Set<String> EXECUTE_METHODS = new HashSet<>();
  protected static final Set<String> MASK_LOG_RESULT_COLUMNS = new HashSet<>();

  private final Map<Object, Object> columnMap = new HashMap<>();

  private final List<Object> columnNames = new ArrayList<>();
  private final List<Object> columnValues = new ArrayList<>();
  private final List<Boolean> columnMasks = new ArrayList<>();

  protected final Log statementLog;
  protected final int queryStack;

  /*
   * Default constructor
   */
  public BaseJdbcLogger(Log log, int queryStack) {
    this(log, queryStack, null);
  }

  /*
   * Default constructor
   */
  public BaseJdbcLogger(Log log, int queryStack, Set<String> maskLogResultColumns) {
    this.statementLog = log;
    if (queryStack == 0) {
      this.queryStack = 1;
    } else {
      this.queryStack = queryStack;
    }
    if (maskLogResultColumns != null) {
      MASK_LOG_RESULT_COLUMNS.addAll(maskLogResultColumns);
    }
  }

  static {
    SET_METHODS = Arrays.stream(PreparedStatement.class.getDeclaredMethods())
        .filter(method -> method.getName().startsWith("set")).filter(method -> method.getParameterCount() > 1)
        .map(Method::getName).collect(Collectors.toSet());

    EXECUTE_METHODS.add("execute");
    EXECUTE_METHODS.add("executeUpdate");
    EXECUTE_METHODS.add("executeQuery");
    EXECUTE_METHODS.add("addBatch");
  }

  protected void setColumn(Object key, Object value) {
    columnMap.put(key, value);
    columnNames.add(key);
    columnValues.add(value);
  }

  protected Object getColumn(Object key) {
    return columnMap.get(key);
  }

  protected String getParameterValueString() {
    int size = columnValues.size();
    boolean equal = size == columnMasks.size();
    List<Object> typeList = new ArrayList<>(size);
    for (int i = 0; i < size; ++i) {
      Object value = columnValues.get(i);
      if (value == null) {
        typeList.add("null");
      } else {
        String strVal = objectValueString(value);
        boolean mask = equal && columnMasks.get(i);
        typeList.add(mask ? mask(strVal) : strVal + "(" + value.getClass().getSimpleName() + ")");
      }
    }
    final String parameters = typeList.toString();
    return parameters.substring(1, parameters.length() - 1);
  }

  protected String objectValueString(Object value) {
    if (value instanceof Array) {
      try {
        return ArrayUtil.toString(((Array) value).getArray());
      } catch (SQLException e) {
        // Intentionally fall through to return value.toString()
      }
    }
    return value.toString();
  }

  protected String getColumnString() {
    return columnNames.toString();
  }

  public void addColumnMasks(Boolean maskLog) {
    this.columnMasks.add(maskLog);
  }

  protected void clearColumnInfo() {
    columnMap.clear();
    columnNames.clear();
    columnValues.clear();
    columnMasks.clear();
  }

  protected String removeExtraWhitespace(String original) {
    return SqlSourceBuilder.removeExtraWhitespaces(original);
  }

  protected boolean isDebugEnabled() {
    return statementLog.isDebugEnabled();
  }

  protected boolean isTraceEnabled() {
    return statementLog.isTraceEnabled();
  }

  protected void debug(String text, boolean input) {
    if (statementLog.isDebugEnabled()) {
      statementLog.debug(prefix(input) + text);
    }
  }

  protected void trace(String text, boolean input) {
    if (statementLog.isTraceEnabled()) {
      statementLog.trace(prefix(input) + text);
    }
  }

  protected String mask(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }
    int length = value.length();
    if (length == 1) {
      return value;
    } else if (length == 2) {
      return value.charAt(0) + "*";
    } else if (length == 3) {
      return value.charAt(0) + "*" + value.charAt(2);
    } else {
      int maskLen = length >> 1;
      int rawLen = length - maskLen;
      return value.substring(0, (rawLen & 1) == 0 ? rawLen >> 1 : (rawLen >> 1) + 1) + "*".repeat(maskLen)
          + value.substring(length - (rawLen >> 1));
    }
  }

  private String prefix(boolean isInput) {
    char[] buffer = new char[queryStack * 2 + 2];
    Arrays.fill(buffer, '=');
    buffer[queryStack * 2 + 1] = ' ';
    if (isInput) {
      buffer[queryStack * 2] = '>';
    } else {
      buffer[0] = '<';
    }
    return new String(buffer);
  }

}
