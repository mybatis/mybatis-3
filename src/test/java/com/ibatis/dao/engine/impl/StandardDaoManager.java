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
import com.ibatis.dao.client.DaoManager;
import com.ibatis.dao.client.DaoTransaction;

import java.util.*;

public class StandardDaoManager implements DaoManager {

  private static final String DAO_EXPLICIT_TX = "__DAO_EXPLICIT_TX";

  private ThreadLocal transactionMode = new ThreadLocal();
  private ThreadLocal contextInTransactionList = new ThreadLocal();

  private Map idContextMap = new HashMap();
  private Map typeContextMap = new HashMap();
  private Map daoImplMap = new HashMap();

  public void addContext(DaoContext context) {
    // Add context ID mapping
    if (context.getId() != null && context.getId().length() > 0) {
      if (idContextMap.containsKey(context.getId())) {
        throw new DaoException("There is already a DAO Context with the ID '" + context.getId() + "'.");
      }
      idContextMap.put(context.getId(), context);
    }
    // Add type mappings
    Iterator i = context.getDaoImpls();
    while (i.hasNext()) {
      DaoImpl daoImpl = (DaoImpl) i.next();

      // Don't associate a default DAO when multiple DAO impls are registered.
      if (typeContextMap.containsKey(daoImpl.getDaoInterface())) {
        typeContextMap.put(daoImpl.getDaoInterface(), null);
      } else {
        typeContextMap.put(daoImpl.getDaoInterface(), context);
      }

      daoImplMap.put(daoImpl.getProxy(), daoImpl);
      daoImplMap.put(daoImpl.getDaoInstance(), daoImpl);
    }
  }

  public Dao getDao(Class iface) {
    DaoContext context = (DaoContext) typeContextMap.get(iface);
    if (context == null) {
      throw new DaoException("There is no DAO implementation found for " + iface + " in any context. If you've " +
          "registered multiple implementations of this DAO, you must specify the Context ID for the DAO implementation" +
          "you're looking for using the getDao(Class iface, String contextId) method.");
    }
    return context.getDao(iface);
  }

  public Dao getDao(Class iface, String contextId) {
    DaoContext context = (DaoContext) idContextMap.get(contextId);
    if (context == null) {
      throw new DaoException("There is no Context found with the ID " + contextId + ".");
    }
    return context.getDao(iface);
  }

  public void startTransaction() {
    transactionMode.set(DAO_EXPLICIT_TX);
  }

  public void commitTransaction() {
    List ctxList = getContextInTransactionList();
    Iterator i = ctxList.iterator();
    while (i.hasNext()) {
      DaoContext context = (DaoContext) i.next();
      context.commitTransaction();
    }
  }

  public void endTransaction() {
    List ctxList = getContextInTransactionList();
    try {
      Iterator i = ctxList.iterator();
      while (i.hasNext()) {
        DaoContext context = (DaoContext) i.next();
        context.endTransaction();
      }
    } finally {
      transactionMode.set(null);
      ctxList.clear();
    }
  }

  public DaoTransaction getTransaction(Dao dao) {
    DaoImpl impl = (DaoImpl) daoImplMap.get(dao);
    return impl.getDaoContext().getTransaction();
  }

  public boolean isExplicitTransaction() {
    return DAO_EXPLICIT_TX.equals(transactionMode.get());
  }

  public void addContextInTransaction(DaoContext ctx) {
    List ctxList = getContextInTransactionList();
    if (!ctxList.contains(ctx)) {
      ctxList.add(ctx);
    }
  }

  private List getContextInTransactionList() {
    List list = (List) contextInTransactionList.get();
    if (list == null) {
      list = new ArrayList();
      contextInTransactionList.set(list);
    }
    return list;
  }

}


