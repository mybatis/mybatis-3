package org.apache.ibatis.logging.jdbc;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Connection proxy to add logging
 */
public class ConnectionLogger extends BaseJdbcLogger implements InvocationHandler {

  private static final Log log = LogFactory.getLog(Connection.class);

  private Connection connection;

  private ConnectionLogger(Connection conn) {
    super();
    this.connection = conn;
    if (log.isDebugEnabled()) {
      log.debug("ooo Connection Opened");
    }
  }

  public Object invoke(Object proxy, Method method, Object[] params)
      throws Throwable {
    try {
      if ("prepareStatement".equals(method.getName())) {
        PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
        stmt = PreparedStatementLogger.newInstance(stmt, (String) params[0]);
        return stmt;
      } else if ("prepareCall".equals(method.getName())) {
        PreparedStatement stmt = (PreparedStatement) method.invoke(connection, params);
        stmt = PreparedStatementLogger.newInstance(stmt, (String) params[0]);
        return stmt;
      } else if ("createStatement".equals(method.getName())) {
        Statement stmt = (Statement) method.invoke(connection, params);
        stmt = StatementLogger.newInstance(stmt);
        return stmt;
      } else if ("close".equals(method.getName())) {
        if (log.isDebugEnabled()) {
          log.debug("xxx Connection Closed");
        }
        return method.invoke(connection, params);
      } else {
        return method.invoke(connection, params);
      }
    } catch (Throwable t) {
      Throwable t1 = ExceptionUtil.unwrapThrowable(t);
      log.error("Error calling Connection." + method.getName() + ':', t1);
      throw t1;
    }

  }

  /**
   * Creates a logging version of a connection
   *
   * @param conn - the original connection
   * @return - the connection with logging
   */
  public static Connection newInstance(Connection conn) {
    InvocationHandler handler = new ConnectionLogger(conn);
    ClassLoader cl = Connection.class.getClassLoader();
    return (Connection) Proxy.newProxyInstance(cl, new Class[]{Connection.class}, handler);
  }

  /**
   * return the wrapped connection
   *
   * @return the connection
   */
  public Connection getConnection() {
    return connection;
  }

}
