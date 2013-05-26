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
package com.ibatis.dao.engine.impl;

import com.ibatis.dao.client.Dao;
import com.ibatis.dao.client.DaoException;
import com.ibatis.dao.client.DaoTransaction;
import com.ibatis.dao.engine.transaction.DaoTransactionManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DaoContext {

  private String id;
  private StandardDaoManager daoManager;
  private DaoTransactionManager transactionManager;
  private ThreadLocal transaction = new ThreadLocal();
  private ThreadLocal state = new ThreadLocal();

  private Map typeDaoImplMap = new HashMap();

  public DaoContext() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public StandardDaoManager getDaoManager() {
    return daoManager;
  }

  public void setDaoManager(StandardDaoManager daoManager) {
    this.daoManager = daoManager;
  }

  public DaoTransactionManager getTransactionManager() {
    return transactionManager;
  }

  public void setTransactionManager(DaoTransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  public void addDao(DaoImpl daoImpl) {
    if (typeDaoImplMap.containsKey(daoImpl.getDaoInterface())) {
      throw new DaoException("More than one implementation for '" + daoImpl.getDaoInterface() + "' was configured.  " +
          "Only one implementation per context is allowed.");
    }
    typeDaoImplMap.put(daoImpl.getDaoInterface(), daoImpl);
  }

  public Dao getDao(Class iface) {
    DaoImpl impl = (DaoImpl) typeDaoImplMap.get(iface);
    if (impl == null) {
      throw new DaoException("There is no DAO implementation found for " + iface + " in this context.");
    }
    return impl.getProxy();
  }

  public Iterator getDaoImpls() {
    return typeDaoImplMap.values().iterator();
  }

  public DaoTransaction getTransaction() {
    startTransaction();
    return (DaoTransaction) transaction.get();
  }

  public void startTransaction() {
    if (state.get() != DaoTransactionState.ACTIVE) {
      DaoTransaction trans = transactionManager.startTransaction();
      transaction.set(trans);
      state.set(DaoTransactionState.ACTIVE);
      daoManager.addContextInTransaction(this);
    }
  }

  public void commitTransaction() {
    DaoTransaction trans = (DaoTransaction) transaction.get();
    if (state.get() == DaoTransactionState.ACTIVE) {
      transactionManager.commitTransaction(trans);
      state.set(DaoTransactionState.COMMITTED);
    } else {
      state.set(DaoTransactionState.INACTIVE);
    }
  }

  public void endTransaction() {
    DaoTransaction trans = (DaoTransaction) transaction.get();
    if (state.get() == DaoTransactionState.ACTIVE) {
      try {
        transactionManager.rollbackTransaction(trans);
      } finally {
        state.set(DaoTransactionState.ROLLEDBACK);
        transaction.set(null);
      }
    } else {
      state.set(DaoTransactionState.INACTIVE);
      transaction.set(null);
    }
  }

}
