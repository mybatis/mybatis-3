package org.apache.ibatis.logging.jdbc;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Statement proxy to add logging
 */
public class StatementLogger extends BaseJdbcLogger implements InvocationHandler {

  private static final Log log = LogFactory.getLog(Statement.class);

  private Statement statement;

  private StatementLogger(Statement stmt) {
    super();
    this.statement = stmt;
  }

  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      if (EXECUTE_METHODS.contains(method.getName())) {
        if (log.isDebugEnabled()) {
          log.debug("==>  Executing: " + removeBreakingWhitespace((String) params[0]));
        }
        if ("executeQuery".equals(method.getName())) {
          ResultSet rs = (ResultSet) method.invoke(statement, params);
          if (rs != null) {
            return ResultSetLogger.newInstance(rs);
          } else {
            return null;
          }
        } else {
          return method.invoke(statement, params);
        }
      } else if ("getResultSet".equals(method.getName())) {
        ResultSet rs = (ResultSet) method.invoke(statement, params);
        if (rs != null) {
          return ResultSetLogger.newInstance(rs);
        } else {
          return null;
        }
      } else if ("equals".equals(method.getName())) {
        Object ps = params[0];
        return ps instanceof Proxy && proxy == ps;
      } else if ("hashCode".equals(method.getName())) {
        return proxy.hashCode();
      } else {
        return method.invoke(statement, params);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  /**
   * Creates a logging version of a Statement
   *
   * @param stmt - the statement
   * @return - the proxy
   */
  public static Statement newInstance(Statement stmt) {
    InvocationHandler handler = new StatementLogger(stmt);
    ClassLoader cl = Statement.class.getClassLoader();
    return (Statement) Proxy.newProxyInstance(cl, new Class[]{Statement.class}, handler);
  }

  /**
   * return the wrapped statement
   *
   * @return the statement
   */
  public Statement getStatement() {
    return statement;
  }

}
