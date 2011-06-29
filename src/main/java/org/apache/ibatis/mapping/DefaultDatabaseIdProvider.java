package org.apache.ibatis.mapping;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * Default DatabaseId provider
 * 
 * It returns database product name as a databaseId
 * If the user provides a properties it uses it to translate database product name
 * key="Microsoft SQL Server", value="ms" will return "ms" 
 * It can return null, if no database product name or 
 * a properties was specified and no translation was found 
 * 
 */
public class DefaultDatabaseIdProvider implements DatabaseIdProvider {
  
  private Properties properties;
  
  public String getDatabaseId(DataSource dataSource) {
    try {
      return getDatabaseName(dataSource);
    } catch (Exception e) {
      // intentionally ignored
    }
    return null;
  }

  public void setProperties(Properties p) {
    this.properties = p;
  }
  
  private String getDatabaseName(DataSource dataSource) throws SQLException {
    String productName = getDatabaseProductName(dataSource);
    if (this.properties != null) {
      return this.properties.getProperty(productName);
    }
    return productName;
  }

  private String getDatabaseProductName(DataSource dataSource) throws SQLException {
    Connection con = null;
    try {
      con = dataSource.getConnection();
      DatabaseMetaData metaData = con.getMetaData();
      return metaData.getDatabaseProductName();
    } finally {
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          // ignored
        }
      }
    }
  }
  
}
