package com.ibatis.dao.engine.transaction.sqlmap;

import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.engine.transaction.ConnectionDaoTransaction;
import com.ibatis.sqlmap.client.SqlMapClient;

import java.sql.Connection;
import java.sql.SQLException;

public class SqlMapDaoTransaction implements ConnectionDaoTransaction {

  private SqlMapClient client;

  public SqlMapDaoTransaction(SqlMapClient client) {
    try {
      client.startTransaction();
      this.client = client;
    } catch (SQLException e) {
      throw new DaoException("Error starting SQL Map transaction.  Cause: " + e, e);
    }
  }

  public void commit() {
    try {
      client.commitTransaction();
      client.endTransaction();
    } catch (SQLException e) {
      throw new DaoException("Error committing SQL Map transaction.  Cause: " + e, e);
    }
  }

  public void rollback() {
    try {
      client.endTransaction();
    } catch (SQLException e) {
      throw new DaoException("Error ending SQL Map transaction.  Cause: " + e, e);
    }
  }

  public SqlMapClient getSqlMap() {
    return client;
  }

  public Connection getConnection() {
    try {
      return client.getCurrentConnection();
    } catch (SQLException e) {
      throw new DaoException("Error getting connection from SQL Map Client.  Cause: " + e, e);
    }
  }

}
