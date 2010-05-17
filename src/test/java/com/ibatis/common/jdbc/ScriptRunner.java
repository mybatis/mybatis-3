package com.ibatis.common.jdbc;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class ScriptRunner extends org.apache.ibatis.jdbc.ScriptRunner {

  public ScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
    super(connection);
    setAutoCommit(autoCommit);
    setStopOnError(stopOnError);
  }

  public ScriptRunner(String driver, String url, String username, String password, boolean autoCommit, boolean stopOnError) throws SQLException {
    super(new UnpooledDataSource(driver, url, username, password).getConnection());
    setAutoCommit(autoCommit);
    setStopOnError(stopOnError);
  }
}
