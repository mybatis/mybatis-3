package org.apache.ibatis.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

public class DataSourceUtils {

  private static Map<String, String> databaseNames = new HashMap<String, String>() {
    {
      put("derby", "Derby");
      put("db2", "DB2");
      put("hsql", "HSQL");
      put("sqlserver", "Microsoft");
      put("mysql", "MySQL");
      put("oracle", "Oracle");
      put("postgres", "PostgreSQL");
      put("sybase", "Sybase");
    }
  };

  public static String getDatabaseName(DataSource dataSource) {
    String productName = getDatabaseProductName(dataSource);
    String databaseName = null;
    for (Entry<String, String> databaseNameEntry : databaseNames.entrySet()) {
      if (productName.contains(databaseNameEntry.getValue())) {
        databaseName = databaseNameEntry.getKey();
        break;
      }
    }
    return databaseName;
  }

  private static String getDatabaseProductName(DataSource dataSource) {
    Connection con = null;
    try {
      con = dataSource.getConnection();
      if (con == null) {
        throw new RuntimeException("Connection returned by DataSource [" + dataSource + "] was null");
      }
      DatabaseMetaData metaData = con.getMetaData();
      if (metaData == null) {
        throw new RuntimeException("DatabaseMetaData returned by Connection [" + con + "] was null");
      }
      return metaData.getDatabaseProductName();
    } catch (SQLException e) {
      throw new RuntimeException("Could not get database product name", e);
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
