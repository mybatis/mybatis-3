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
