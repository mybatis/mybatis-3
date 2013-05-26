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
package com.ibatis.dao.client.template;

import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoManager;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.ConnectionDaoTransaction;

import java.sql.Connection;

/*
 * A DaoTemplate for JDBC implementations that provides a
 * convenient method to access the JDBC Connection.
 * <p/>
 * Use this template for both JDBC and JTA transaction managers.
 * It can also be used for any transaction manager that supports
 * normal JDBC connections, including iBATIS SQL Maps and Hibernate.
 */
public abstract class JdbcDaoTemplate extends DaoTemplate {

  /*
   * The DaoManager that manages this Dao instance will be passed
   * in as the parameter to this constructor automatically upon
   * instantiation.
   *
   * @param daoManager
   */
  public JdbcDaoTemplate(DaoManager daoManager) {
    super(daoManager);
  }

  /*
   * Gets the JDBC Connection associated with the current
   * DaoTransaction that this Dao is working under.
   *
   * @return A JDBC Connection instance.
   */
  protected Connection getConnection() {
    DaoTransaction trans = daoManager.getTransaction(this);
    if (!(trans instanceof ConnectionDaoTransaction)) {
      throw new DaoException("The DAO manager of type " + daoManager.getClass().getName() +
          " cannot supply a JDBC Connection for this template, and is therefore not" +
          "supported by JdbcDaoTemplate.");
    }
    return ((ConnectionDaoTransaction) trans).getConnection();
  }

}
