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
package com.ibatis.dao.client;

/*
 * This interface describes the DaoManager interface.  It provides
 * access to all DAOs it manages and also allows transactions
 * to be committed and ended (possibly rolled back).
 * <p/>
 * DAO instances returned from the DAO Manager are proxied such that transactions
 * can be automatically started, committed and ended (or rolled back). This is
 * a similar semantic to the JDBC autocommit, but much more powerful.
 * <p/>
 * Alternatively, tranasctions can be controlled programmatically, allowing you to
 * demarcate wider scope transactions as needed.
 * <p/>
 * Either way, transactions will only be started for those contexts that require them.
 * No unneccessary transactions will be started.  When commitTransaction() and
 * endTransaction() are called, all transactions which have been started in each
 * configured context will be committed and ended respectively.  endTransaction() will
 * automatically rollback any transactions that have not been committed.
 * <p/>
 * Here's a couple of examples:
 * <p/>
 * <p/>
 * <p/>
 * <pre>
 * <p/>
 * // **************************************************************
 * //              AUTO COMMIT TRANASACTION SEMANTIC
 * // **************************************************************
 * <p/>
 * DaoManager daoManager = DaoManagerBuilder.buildDaoManager(reader);
 * PersonDao personDao = daoManager.getDao(PersonDao.class);
 * // A transaction will be automatically started committed and ended
 * // by calling any method on the DAO.  The following insert and update
 * // are TWO separate transactions.
 * personDao.insertPerson (person); // Starts transaction
 * person.setLastName("Begin");
 * personDao.updatePerson (person); // Starts a new transaction
 * <p/>
 * // **************************************************************
 * //      PROGRAMMATIC DEMARCATION OF TRANSACTION SCOPE
 * // **************************************************************
 * <p/>
 * DaoManager daoManager = DaoManagerBuilder.buildDaoManager(reader);
 * PersonDao personDao = daoManager.getDao(PersonDao.class);
 * try {
 *   // Calling startTransaction() tells the DAO Manager that you
 *   // are going to be managing the transactions manually.
 *   daoManager.startTransaction();
 *   personDao.insertPerson (person);
 *   person.setLastName("Begin");
 *   personDao.updatePerson (person);
 *   // Commit all active transactions in all contexts
 *   daoManager.commitTransaction();
 * } finally {
 *   // End all active transactions in all contexts and rollback if necessary.
 *   daoManager.endTransaction();
 * }
 * </pre>
 * <p/>
 * <b>Important: </b> In order to achieve global transaction behaviour
 * (i.e. two phase commit), you'll need to configure all of your contexts
 * using JTA, JNDI and XA compliant DataSources.
 * <p/>
 */
public interface DaoManager {

  /*
   * Gets a Dao instance for the requested interface type.
   *
   * @param type The interface or generic type for which an implementation
   *             should be returned.
   * @return The Dao implementation instance.
   */
  public Dao getDao(Class type);

  /*
   * Gets a Dao instance for the requested interface type registered
   * under the context with the specified id.
   *
   * @param iface     The interface or generic type for which an implementation
   *                  should be returned.
   * @param contextId The ID of the context under which to find the DAO
   *                  implementation (use for multiple interface defs).
   * @return The Dao implementation instance.
   */
  public Dao getDao(Class iface, String contextId);

  /*
   * Gets the transaction that the provided Dao is currently working
   * under.  If there is no current transaction in scope, one will
   * be started.
   *
   * @param dao The Dao to find a transaction for.
   * @return The Transaction under which the Dao provided is working
   *         under.
   */
  public DaoTransaction getTransaction(Dao dao);

  /*
   * Starts a transaction scope managed by this  DaoManager.
   * If this method isn't called, then all DAO methods use
   * "autocommit" semantics.
   */
  public void startTransaction();

  /*
   * Commits all transactions currently started for all DAO contexts
   * managed by this  DaoManager.
   */
  public void commitTransaction();

  /*
   * Ends all transactions currently started for all DAO contexts
   * managed by this  DaoManager.  If any transactions have not been
   * successfully committed, then those remaining will be rolled back.
   */
  public void endTransaction();

}
