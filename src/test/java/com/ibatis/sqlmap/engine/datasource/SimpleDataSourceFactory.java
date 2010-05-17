package com.ibatis.sqlmap.engine.datasource;

import com.ibatis.common.jdbc.SimpleDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * DataSourceFactory implementation for the iBATIS SimpleDataSource
 */
public class SimpleDataSourceFactory implements DataSourceFactory {

  private DataSource dataSource;

  public void initialize(Map map) {
    dataSource = new SimpleDataSource(map);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

}
