package com.ibatis.sqlmap.engine.datasource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Interface to provide a way to create and configure a DataSource for iBATIS
 */
public interface DataSourceFactory {

  /**
   * Simple method to initialize/configure a datasource
   *
   * @param map - the configuration information
   */
  public void initialize(Map map);

  /**
   * Returns a datasource
   *
   * @return an implementation of DataSource
   */
  public DataSource getDataSource();

}
