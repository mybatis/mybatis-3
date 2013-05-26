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
package com.ibatis.sqlmap.engine.transaction;

import org.apache.ibatis.session.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

public interface TransactionConfig {

  Transaction newTransaction(Configuration configuration, int transactionIsolation)
      throws SQLException, TransactionException;

  DataSource getDataSource();

  void setDataSource(DataSource ds);

  boolean isForceCommit();

  void setForceCommit(boolean forceCommit);

  void setProperties(Properties props)
      throws SQLException, TransactionException;

}
