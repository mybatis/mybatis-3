/*
 *    Copyright 2009-2012 The MyBatis Team
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.reflection.ExceptionUtil;

/*
 * ResultSet proxy to add logging
 */
public final class ResultSetLogger extends BaseJdbcLogger implements InvocationHandler {

  private boolean first = true;
  private ResultSet rs;
  
  private static Set<Integer> BLOB_TYPES;
  private Set<Integer> BLOB_COLUMNS;
  
  static {
    BLOB_TYPES = new HashSet<Integer>();
    BLOB_TYPES.add(Types.BINARY);
    BLOB_TYPES.add(Types.BLOB);
    BLOB_TYPES.add(Types.CLOB);
    BLOB_TYPES.add(Types.LONGNVARCHAR);
    BLOB_TYPES.add(Types.LONGVARBINARY);
    BLOB_TYPES.add(Types.LONGVARCHAR);
    BLOB_TYPES.add(Types.NCLOB);
    BLOB_TYPES.add(Types.VARBINARY);
}
    

  private ResultSetLogger(ResultSet rs, Log statementLog) {
    super(statementLog);
    this.rs = rs;

    BLOB_COLUMNS = new HashSet<Integer>();
  }

  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      Object o = method.invoke(rs, params);
      if ("next".equals(method.getName())) {
        if (((Boolean) o)) {
          ResultSetMetaData rsmd = rs.getMetaData();
          final int columnCount = rsmd.getColumnCount();
          if (isTraceEnabled()) {
            if (first) {
              first = false;
              printColumnHeaders(rsmd, columnCount);
            }
            printColumnValues(columnCount);
          }
        }
      }
      clearColumnInfo();
      return o;
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  private void printColumnHeaders(ResultSetMetaData rsmd, int columnCount) throws SQLException {
    StringBuilder row = new StringBuilder();
    row.append("<==    Columns: ");
    for (int i = 1; i <= columnCount; i++) {
      if (BLOB_TYPES.contains(rsmd.getColumnType(i))) {
        BLOB_COLUMNS.add(i);
      }
      String colname = rsmd.getColumnName(i);
      row.append(colname);
      if (i != columnCount) row.append(", ");
    }
    trace(row.toString());
  }

  private void printColumnValues(int columnCount) throws SQLException {
    StringBuilder row = new StringBuilder();
    row.append("<==        Row: ");
    for (int i = 1; i <= columnCount; i++) {
      String colname;
      try {
        if (BLOB_COLUMNS.contains(i)) {
          colname = "<<BLOB>>";
        } else {
          colname = rs.getString(i);
        }
      } catch (SQLException e) {
        // generally can't call getString() on a BLOB column
        colname = "<<Cannot Display>>";
      }
      row.append(colname);
      if (i != columnCount) row.append(", ");
    }
    trace(row.toString());
  }

  /*
   * Creates a logging version of a ResultSet
   *
   * @param rs - the ResultSet to proxy
   * @return - the ResultSet with logging
   */
  public static ResultSet newInstance(ResultSet rs, Log statementLog) {
    InvocationHandler handler = new ResultSetLogger(rs, statementLog);
    ClassLoader cl = ResultSet.class.getClassLoader();
    return (ResultSet) Proxy.newProxyInstance(cl, new Class[]{ResultSet.class}, handler);
  }

  /*
   * Get the wrapped result set
   *
   * @return the resultSet
   */
  public ResultSet getRs() {
    return rs;
  }

}
