package org.apache.ibatis.logging.jdbc;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * ResultSet proxy to add logging
 */
public class ResultSetLogger extends BaseJdbcLogger implements InvocationHandler {

  private static final Log log = LogFactory.getLog(ResultSet.class);

  boolean first = true;
  private ResultSet rs;

  private ResultSetLogger(ResultSet rs) {
    super();
    this.rs = rs;
  }

  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      Object o = method.invoke(rs, params);
      if ("next".equals(method.getName())) {
        if (((Boolean) o)) {
          ResultSetMetaData rsmd = rs.getMetaData();
          final int columnCount = rsmd.getColumnCount();
          if (log.isDebugEnabled()) {
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
      String colname = rsmd.getColumnName(i);
      row.append(colname);
      if (i != columnCount) row.append(", ");
    }
    log.debug(row.toString());
  }

  private void printColumnValues(int columnCount) throws SQLException {
    StringBuilder row = new StringBuilder();
    row.append("<==        Row: ");
    for (int i = 1; i <= columnCount; i++) {
      String colname;
      try {
        colname = rs.getString(i);
      } catch (SQLException e) {
        // generally can't call getString() on a BLOB column
        colname = "<<Cannot Display>>";
      }
      row.append(colname);
      if (i != columnCount) row.append(", ");
    }
    log.debug(row.toString());
  }

  /**
   * Creates a logging version of a ResultSet
   *
   * @param rs - the ResultSet to proxy
   * @return - the ResultSet with logging
   */
  public static ResultSet newInstance(ResultSet rs) {
    InvocationHandler handler = new ResultSetLogger(rs);
    ClassLoader cl = ResultSet.class.getClassLoader();
    return (ResultSet) Proxy.newProxyInstance(cl, new Class[]{ResultSet.class}, handler);
  }

  /**
   * Get the wrapped result set
   *
   * @return the resultSet
   */
  public ResultSet getRs() {
    return rs;
  }

}
