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
