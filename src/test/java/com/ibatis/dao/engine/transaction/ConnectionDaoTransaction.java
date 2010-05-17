package com.ibatis.dao.engine.transaction;

import com.ibatis.dao.client.DaoTransaction;

import java.sql.Connection;

public interface ConnectionDaoTransaction extends DaoTransaction {

  public Connection getConnection();

}
