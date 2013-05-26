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
package com.ibatis.sqlmap.client;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/*
 * This interface declares methods for demarcating SQL Map transactions.
 *
 * @see com.ibatis.sqlmap.client.SqlMapSession
 * @see SqlMapClient
 */
public interface SqlMapTransactionManager {

  /*
   * Demarcates the beginning of a transaction scope.  Transactions must be properly
   * committed or rolled back to be effective.  Use the following pattern when working
   * with transactions:
   * <pre>
   * try {
   *   sqlMap.startTransaction();
   *   // do work
   *   sqlMap.commitTransaction();
   * } finally {
   *   sqlMap.endTransaction();
   * }
   * </pre>
   * <p/>
   * Always call endTransaction() once startTransaction() has been called.
   *
   * @throws java.sql.SQLException If an error occurs while starting the transaction, or
   *                               the transaction could not be started.
   */
  public void startTransaction() throws SQLException;


  /*
   * Demarcates the beginning of a transaction scope using the specified transaction
   * isolation.  Transactions must be properly committed or rolled back to be effective.
   * Use the following pattern when working with transactions:
   * <pre>
   * try {
   *   sqlMap.startTransaction(Connection.TRANSACTION_REPEATABLE_READ);
   *   // do work
   *   sqlMap.commitTransaction();
   * } finally {
   *   sqlMap.endTransaction();
   * }
   * </pre>
   * <p/>
   * Always call endTransaction() once startTransaction() has been called.
   *
   * @throws java.sql.SQLException If an error occurs while starting the transaction, or
   *                               the transaction could not be started.
   */
  public void startTransaction(int transactionIsolation) throws SQLException;

  /*
   * Commits the currently started transaction.
   *
   * @throws java.sql.SQLException If an error occurs while committing the transaction, or
   *                               the transaction could not be committed.
   */
  public void commitTransaction() throws SQLException;

  /*
   * Ends a transaction and rolls back if necessary.  If the transaction has
   * been started, but not committed, it will be rolled back upon calling
   * endTransaction().
   *
   * @throws java.sql.SQLException If an error occurs during rollback or the transaction could
   *                               not be ended.
   */
  public void endTransaction() throws SQLException;

  /*
   * Allows the developer to easily use an externally supplied connection
   * when executing statements.
   * <p/>
   * <b>Important:</b> Using a user supplied connection basically sidesteps the transaction manager,
   * so you are responsible for appropriately.  Here's a (very) simple example (throws SQLException):
   * <pre>
   * try {
   *   Connection connection = dataSource.getConnection();
   *   sqlMap.setUserConnection(connection);
   *   // do work
   *   connection.commit();
   * } catch (SQLException e) {
   *     try {
   *       if (connection != null) commit.rollback();
   *     } catch (SQLException ignored) {
   *       // generally ignored
   *     }
   *     throw e;  // rethrow the exception
   * } finally {
   *   try {
   *     if (connection != null) connection.close();
   *   } catch (SQLException ignored) {
   *     // generally ignored
   *   }
   * }
   * </pre>
   *
   * @param connnection
   * @throws java.sql.SQLException
   */
  public void setUserConnection(Connection connnection) throws SQLException;

  /*
   * Returns the current connection in use.  If no connection exists null will
   * be returned. There may be no connection if no transaction has been started,
   * and if no user provided connection has been set.
   *
   * @return The current connection or null.
   * @throws java.sql.SQLException
   */
  public Connection getCurrentConnection() throws SQLException;

  /*
   * Returns the DataSource instance currently being used by the SqlMapSession.
   *
   * @return The DataSource instance currently being used by the SqlMapSession.
   */
  public DataSource getDataSource();


}
