package com.ibatis.dao.client.template;

import com.ibatis.dao.client.Dao;
import com.ibatis.dao.client.DaoManager;

/**
 * A base class for Dao implementations, or other DAO templates.
 */
public abstract class DaoTemplate implements Dao {

  protected DaoManager daoManager;

  /**
   * The DaoManager that manages this Dao instance will be passed
   * in as the parameter to this constructor automatically upon
   * instantiation.
   *
   * @param daoManager
   */
  public DaoTemplate(DaoManager daoManager) {
    this.daoManager = daoManager;
  }

}
