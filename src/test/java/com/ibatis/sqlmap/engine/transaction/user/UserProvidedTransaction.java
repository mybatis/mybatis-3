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
package com.ibatis.sqlmap.engine.transaction.user;

import com.ibatis.sqlmap.engine.transaction.BaseTransaction;
import com.ibatis.sqlmap.engine.transaction.TransactionException;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;

import java.sql.Connection;
import java.sql.SQLException;

public class UserProvidedTransaction extends BaseTransaction {

  private Executor executor;

  public UserProvidedTransaction(Configuration configuration, Connection connection) {
    this.executor = configuration.newExecutor(new JdbcTransaction(connection));
  }

  public void commit(boolean required) throws SQLException, TransactionException {
    executor.commit(required);
  }

  public void rollback(boolean required) throws SQLException, TransactionException {
    executor.rollback(required);
  }

  public void close() throws SQLException, TransactionException {
  }

  public Executor getExecutor() throws SQLException, TransactionException {
    return executor;
  }

}
