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
package com.ibatis.dao.engine.transaction.jdbc;

import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.engine.transaction.ConnectionDaoTransaction;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class JdbcDaoTransaction implements ConnectionDaoTransaction {

  private static final Log connectionLog = LogFactory.getLog(Connection.class);

  private Connection connection;

  public JdbcDaoTransaction(DataSource dataSource) {
    try {
      connection = dataSource.getConnection();
      if (connection == null) {
        throw new DaoException("Could not start transaction.  Cause: The DataSource returned a null connection.");
      }
      if (connection.getAutoCommit()) {
        connection.setAutoCommit(false);
      }
      if (connectionLog.isDebugEnabled()) {
//        connection = ConnectionLogger.newInstance(connection);
      }
    } catch (SQLException e) {
      throw new DaoException("Error starting JDBC transaction.  Cause: " + e);
    }
  }

  public void commit() {
    try {
      try {
        connection.commit();
      } finally {
        connection.close();
      }
    } catch (SQLException e) {
      throw new DaoException("Error committing JDBC transaction.  Cause: " + e);
    }
  }

  public void rollback() {
    try {
      try {
        connection.rollback();
      } finally {
        connection.close();
      }
    } catch (SQLException e) {
      throw new DaoException("Error ending JDBC transaction.  Cause: " + e);
    }
  }

  public Connection getConnection() {
    return connection;
  }

}
