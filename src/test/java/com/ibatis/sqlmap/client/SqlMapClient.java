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

import java.sql.Connection;

/*
 * A thread safe client for working with your SQL Maps (Start Here).  This interface inherits transaction control
 * and execution methods from the SqlMapTransactionManager and SqlMapExecutor interfaces.
 * <p/>
 * The SqlMapClient is the central class for working with SQL Maps.  This class will allow you
 * to run mapped statements (select, insert, update, delete etc.), and also demarcate
 * transactions and work with batches.  Once you have an SqlMapClient instance, everything
 * you need to work with SQL Maps is easily available.
 * <p/>
 * The SqlMapClient can either
 * be worked with directly as a multi-threaded client (internal session management), or you can get a single threaded
 * session and work with that.  There may be a slight performance increase if you explicitly
 * get a session (using the openSession() method), as it saves the SqlMapClient from having
 * to manage threads contexts.  But for most cases it won't make much of a difference, so
 * choose whichever paradigm suits your needs or preferences.
 * <p/>
 * An SqlMapClient instance can be safely made <i>static</i> or applied as a <i>Singleton</i>.
 * Generally it's a good idea to make a simple configuration class that will configure the
 * instance (using SqlMapClientBuilder) and provide access to it.
 * <p/>
 * <b>The following example will demonstrate the use of SqlMapClient.</b>
 * <pre>
 * <i><font color="green">
 * //
 * // autocommit simple query --these are just examples...not patterns
 * //
 * </font></i>
 * Employee emp = (Employee) <b>sqlMap.queryForObject("getEmployee", new Integer(1))</b>;
 * <i><font color="green">
 * //
 * // transaction --these are just examples...not patterns
 * //
 * </font></i>
 * try {
 *   <b>sqlMap.startTransaction()</b>
 *   Employee emp2 = new Employee();
 *   // ...set emp2 data
 *   Integer generatedKey = (Integer) <b>sqlMap.insert ("insertEmployee", emp2)</b>;
 *   emp2.setFavouriteColour ("green");
 *   <b>sqlMap.update("updateEmployee", emp2)</b>;
 *   <b>sqlMap.commitTransaction()</b>;
 * } finally {
 *   <b>sqlMap.endTransaction()</b>;
 * }
 * <i><font color="green">
 * //
 * // session --these are just examples...not patterns
 * //
 * </font></i>
 * try {
 *   <b>SqlMapSession session = sqlMap.openSession()</b>
 *   <b>session.startTransaction()</b>
 *   Employee emp2 = new Employee();
 *   // ...set emp2 data
 *   Integer generatedKey = (Integer) <b>session.insert ("insertEmployee", emp2)</b>;
 *   emp2.setFavouriteColour ("green");
 *   <b>session.update("updateEmployee", emp2)</b>;
 *   <b>session.commitTransaction()</b>;
 * } finally {
 *   try {
 *     <b>session.endTransaction()</b>;
 *   } finally {
 *     <b>session.close()</b>;
 *   }
 *   // Generally your session scope would be in a wider context and therefore the
 *   // ugly nested finally block above would not be there.  Realize that sessions
 *   // MUST be closed if explicitly opened (via openSession()).
 * }
 * <i><font color="green">
 * //
 * // batch --these are just examples...not patterns
 * //
 * </font></i>
 * try {
 *   <b>sqlMap.startTransaction()</b>
 *   List list = (Employee) <b>sqlMap.queryForList("getFiredEmployees", null)</b>;
 *   <b>sqlMap.startBatch ()</b>;
 *   for (int i=0, n=list.size(); i < n; i++) {
 *     <b>sqlMap.delete ("deleteEmployee", list.get(i))</b>;
 *   }
 *   <b>sqlMap.executeBatch()</b>;
 *   <b>sqlMap.commitTransaction()</b>;
 * } finally {
 *   <b>sqlMap.endTransaction()</b>;
 * }
 * </pre>
 *
 * @see SqlMapClientBuilder
 * @see com.ibatis.sqlmap.client.SqlMapSession
 * @see com.ibatis.sqlmap.client.SqlMapExecutor
 */
public interface SqlMapClient extends SqlMapExecutor, SqlMapTransactionManager {

  /*
   * Returns a single threaded SqlMapSession implementation for use by
   * one user.  Remember though, that SqlMapClient itself is a thread safe SqlMapSession
   * implementation, so you can also just work directly with it.  If you do get a session
   * explicitly using this method <b>be sure to close it!</b>  You can close a session using
   * the sqlMapSession.close() method.
   * <p/>
   *
   * @return An SqlMapSession instance.
   */
  public SqlMapSession openSession();

  /*
   * Returns a single threaded SqlMapSession implementation for use by
   * one user.  Remember though, that SqlMapClient itself is a thread safe SqlMapSession
   * implementation, so you can also just work directly with it.  If you do get a session
   * explicitly using this method <b>be sure to close it!</b>  You can close a session using
   * the SqlMapSession.close() method.
   * <p/>
   * This particular implementation takes a user provided connection as a parameter.  This
   * connection will be used for executing statements, and therefore overrides any
   * configured datasources.  Using this approach allows the developer to easily use an externally
   * supplied connection for executing statements.
   * <p/>
   * <b>Important:</b> Using a user supplied connection basically sidesteps the datasource
   * so you are responsible for appropriately handling your connection lifecycle (i.e. closing).
   * Here's a (very) simple example (throws SQLException):
   * <pre>
   * try {
   *   Connection connection = dataSource.getConnection();
   *   SqlMapSession session = sqlMap.openSession(connection);
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
   * @param conn - the connection to use for the session
   * @return An SqlMapSession instance.
   */
  public SqlMapSession openSession(Connection conn);

  /*
   * Flushes all data caches.
   */
  public void flushDataCache();

  /*
   * Flushes the data cache that matches the cache model ID provided.
   * cacheId should include the namespace, even when
   * useStatementNamespaces="false".
   *
   * @param cacheId The cache model to flush
   */
  public void flushDataCache(String cacheId);

  /*
   * Returns a generated implementation of a cusom mapper class as specified by the method
   * parameter.  The generated implementation will run mapped statements by matching the method
   * name to the statement name.  The mapped statement elements determine how the statement is
   * run as per the following:
   * <ul>
   *   <li>&lt;insert&gt; -- insert()
   *   <li>&lt;update&gt; -- update()
   *   <li>&lt;delete&gt; -- delete()
   *   <li>&lt;select&gt; -- queryForObject, queryForList or queryForMap, as determined by signature (see below)
   *   <li>&lt;procedure&gt; -- determined by method name (see below)
   * </ul>
   *
   * How select statements are run is determined by the method signature,
   * as per the following:
   * <ul>
   *   <li> Object methodName (Object param) -- queryForObject
   *   <li> List methodName (Object param [, int skip, int max | , int pageSize]) -- queryForList
   *   <li> Map methodName (Object param, String keyProp [,valueProp]) -- queryForMap
   * </ul>
   *
   * How stored procedures are run is determined by the method name,
   * as per the following:
   * <ul>
   *   <li> insertXxxxx -- insert()
   *   <li> createXxxxx -- insert()
   *   <li> updateXxxxx -- update()
   *   <li> saveXxxxx -- update()
   *   <li> deleteXxxxx -- delete()
   *   <li> removeXxxxx -- delete()
   *   <li> selectXxxxx -- queryForXxxxxx() determined by method signature as above
   *   <li> queryXxxxx -- queryForXxxxxx() determined by method signature as above
   *   <li> fetchXxxxx -- queryForXxxxxx() determined by method signature as above
   *   <li> getXxxxx -- queryForXxxxxx() determined by method signature as above
   * </ul>
   *
   * @param iface The interface that contains methods representing the mapped statements contained.
   * @return An instance of iface that can be used to call mapped statements directly in a typesafe
   * manner.
   */
  //public Object getMapper(Class iface);

}