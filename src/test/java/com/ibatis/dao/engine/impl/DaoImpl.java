package com.ibatis.dao.engine.impl;

import com.ibatis.dao.client.Dao;

public class DaoImpl {

  private StandardDaoManager daoManager;
  private DaoContext daoContext;
  private Class daoInterface;
  private Class daoImplementation;
  private Dao daoInstance;
  private Dao proxy;

  public StandardDaoManager getDaoManager() {
    return daoManager;
  }

  public void setDaoManager(StandardDaoManager daoManager) {
    this.daoManager = daoManager;
  }

  public DaoContext getDaoContext() {
    return daoContext;
  }

  public void setDaoContext(DaoContext daoContext) {
    this.daoContext = daoContext;
  }

  public Class getDaoInterface() {
    return daoInterface;
  }

  public void setDaoInterface(Class daoInterface) {
    this.daoInterface = daoInterface;
  }

  public Class getDaoImplementation() {
    return daoImplementation;
  }

  public void setDaoImplementation(Class daoImplementation) {
    this.daoImplementation = daoImplementation;
  }

  public Dao getDaoInstance() {
    return daoInstance;
  }

  public void setDaoInstance(Dao daoInstance) {
    this.daoInstance = daoInstance;
  }

  public Dao getProxy() {
    return proxy;
  }

  public void initProxy() {
    proxy = DaoProxy.newInstance(this);
  }

}
