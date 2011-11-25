package org.apache.ibatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

public interface Transaction {

  Connection getConnection() throws SQLException;

  void commit() throws SQLException;

  void rollback() throws SQLException;

  void close() throws SQLException;

}
