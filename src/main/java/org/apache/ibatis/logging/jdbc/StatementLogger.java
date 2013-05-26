/*
 *    Copyright 2009-2012 the original author or authors.
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
import java.sql.Statement;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.reflection.ExceptionUtil;

/*
 * Statement proxy to add logging
 */
public final class StatementLogger extends BaseJdbcLogger implements InvocationHandler {

  private Statement statement;

  private StatementLogger(Statement stmt, Log statementLog) {
    super(statementLog);
    this.statement = stmt;
  }

  public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
    try {
      if (EXECUTE_METHODS.contains(method.getName())) {
        if (isDebugEnabled()) {
          debug("==>  Executing: " + removeBreakingWhitespace((String) params[0]));
        }
        if ("executeQuery".equals(method.getName())) {
          ResultSet rs = (ResultSet) method.invoke(statement, params);
          if (rs != null) {
            return ResultSetLogger.newInstance(rs, getStatementLog());
          } else {
            return null;
          }
        } else {
          return method.invoke(statement, params);
        }
      } else if ("getResultSet".equals(method.getName())) {
        ResultSet rs = (ResultSet) method.invoke(statement, params);
        if (rs != null) {
          return ResultSetLogger.newInstance(rs, getStatementLog());
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

  /*
   * Creates a logging version of a Statement
   *
   * @param stmt - the statement
   * @return - the proxy
   */
  public static Statement newInstance(Statement stmt, Log statementLog) {
    InvocationHandler handler = new StatementLogger(stmt, statementLog);
    ClassLoader cl = Statement.class.getClassLoader();
    return (Statement) Proxy.newProxyInstance(cl, new Class[]{Statement.class}, handler);
  }

  /*
   * return the wrapped statement
   *
   * @return the statement
   */
  public Statement getStatement() {
    return statement;
  }

}
