/*
 *    Copyright 2009-2011 the original author or authors.
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
package org.apache.ibatis.logging.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.ibatis.logging.Log;

/*
 * Base class for proxies to do logging
 */
public abstract class BaseJdbcLogger {

  protected static final Set<String> SET_METHODS = new HashSet<String>();
  protected static final Set<String> EXECUTE_METHODS = new HashSet<String>();

  private Map<Object, Object> columnMap = new HashMap<Object, Object>();

  private List<Object> columnNames = new ArrayList<Object>();
  private List<Object> columnValues = new ArrayList<Object>();

  private Log statementLog;

  /*
   * Default constructor
   */
  public BaseJdbcLogger(Log log) {
    this.statementLog = log;
  }

  static {
    SET_METHODS.add("setString");
    SET_METHODS.add("setInt");
    SET_METHODS.add("setByte");
    SET_METHODS.add("setShort");
    SET_METHODS.add("setLong");
    SET_METHODS.add("setDouble");
    SET_METHODS.add("setFloat");
    SET_METHODS.add("setTimestamp");
    SET_METHODS.add("setDate");
    SET_METHODS.add("setTime");
    SET_METHODS.add("setArray");
    SET_METHODS.add("setBigDecimal");
    SET_METHODS.add("setAsciiStream");
    SET_METHODS.add("setBinaryStream");
    SET_METHODS.add("setBlob");
    SET_METHODS.add("setBoolean");
    SET_METHODS.add("setBytes");
    SET_METHODS.add("setCharacterStream");
    SET_METHODS.add("setClob");
    SET_METHODS.add("setObject");
    SET_METHODS.add("setNull");

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
    List<Object> typeList = new ArrayList<Object>(columnValues.size());
    for (Object value : columnValues) {
      if (value == null) {
        typeList.add("null");
      } else {
        typeList.add(value + "(" + value.getClass().getSimpleName() + ")");
      }
    }
    final String parameters = typeList.toString();
    return parameters.substring(1, parameters.length() - 1);
  }

  protected String getColumnString() {
    return columnNames.toString();
  }

  protected void clearColumnInfo() {
    columnMap.clear();
    columnNames.clear();
    columnValues.clear();
  }

  protected String removeBreakingWhitespace(String original) {
    StringTokenizer whitespaceStripper = new StringTokenizer(original);
    StringBuilder builder = new StringBuilder();
    while (whitespaceStripper.hasMoreTokens()) {
      builder.append(whitespaceStripper.nextToken());
      builder.append(" ");
    }
    return builder.toString();
  }

  protected boolean isDebugEnabled() {
    return statementLog.isDebugEnabled();
  }

  protected boolean isTraceEnabled() {
    return statementLog.isTraceEnabled();
  }

  protected void debug(String text) {
    if (statementLog.isDebugEnabled()) {
      statementLog.debug(text);
    }
  }

  protected void trace(String text) {
    if (statementLog.isTraceEnabled()) {
      statementLog.trace(text);
    }
  }

  public Log getStatementLog() {
    return statementLog;
  }

}
