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
import org.apache.ibatis.reflection.ExceptionUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

public class DaoProxy implements InvocationHandler {

  private static final Set PASSTHROUGH_METHODS = new HashSet();

  private DaoImpl daoImpl;

  static {
    PASSTHROUGH_METHODS.add("equals");
    PASSTHROUGH_METHODS.add("getClass");
    PASSTHROUGH_METHODS.add("hashCode");
    PASSTHROUGH_METHODS.add("notify");
    PASSTHROUGH_METHODS.add("notifyAll");
    PASSTHROUGH_METHODS.add("toString");
    PASSTHROUGH_METHODS.add("wait");
  }

  public DaoProxy(DaoImpl daoImpl) {
    this.daoImpl = daoImpl;
  }

  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
    Object result = null;
    if (PASSTHROUGH_METHODS.contains(method.getName())) {
      try {
        result = method.invoke(daoImpl.getDaoInstance(), args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    } else {
      StandardDaoManager daoManager = daoImpl.getDaoManager();
      DaoContext context = daoImpl.getDaoContext();

      if (daoManager.isExplicitTransaction()) {
        // Just start the transaction (explicit)
        try {
          context.startTransaction();
          result = method.invoke(daoImpl.getDaoInstance(), args);
        } catch (Throwable t) {
          throw ExceptionUtil.unwrapThrowable(t);
        }
      } else {
        // Start, commit and end the transaction (autocommit)
        try {
          context.startTransaction();
          result = method.invoke(daoImpl.getDaoInstance(), args);
          context.commitTransaction();
        } catch (Throwable t) {
          throw ExceptionUtil.unwrapThrowable(t);
        } finally {
          context.endTransaction();
        }
      }

    }
    return result;
  }

  public static Dao newInstance(DaoImpl daoImpl) {
    return (Dao) Proxy.newProxyInstance(daoImpl.getDaoInterface().getClassLoader(),
        new Class[]{Dao.class, daoImpl.getDaoInterface()},
        new DaoProxy(daoImpl));
  }

}
