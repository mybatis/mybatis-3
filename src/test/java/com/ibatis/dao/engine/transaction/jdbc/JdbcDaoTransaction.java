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
        connection = ConnectionLogger.newInstance(connection);
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
