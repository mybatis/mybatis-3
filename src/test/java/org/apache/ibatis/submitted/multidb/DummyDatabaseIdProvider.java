package org.apache.ibatis.submitted.multidb;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.DatabaseIdProvider;

public class DummyDatabaseIdProvider implements DatabaseIdProvider {

  private Properties properties;
  
  public String getDatabaseId(DataSource dataSource) {
    return properties.getProperty("name");
  }

  public void setProperties(Properties p) {
    this.properties = p;
  }

}
