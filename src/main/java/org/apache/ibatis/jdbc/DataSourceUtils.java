package org.apache.ibatis.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public class DataSourceUtils {

  /**
   * For each type, the supported DatabaseProductNameS
   *
   * TODO is this useful?
   */
  private static Map<String, Collection<String>> TYPE_NAME = new HashMap<String, Collection<String>>();

  /**
   * For each DatabaseProductName, the related type
   */
  private static Map<String, String> NAME_TYPE = new HashMap<String, String>();

  static {
    // data kindly borrowed from DbVisualizer (http://www.dbvis.com/)
    register("cache", "Cache");
    register("db2", "DB2");
    register("db2", "DB2 (DataDirect)");
    register("generic", "DB2 for AS/400 (JTOpen)");
    register("firebird", "Firebird");
    register("frontbase", "FrontBase");
    register("neoview", "HP Neoview");
    register("hsql", "HSQLDB server");
    register("hsql", "HSQLDB embedded");
    register("h2", "H2 server");
    register("h2", "H2 embedded");
    register("informix", "Informix");
    register("informix", "Informix (DataDirect)");
    register("derby", "JavaDB/Derby server");
    register("derby", "JavaDB/Derby embedded");
    register("jdatastore", "JDataStore");
    register("generic", "JDBC/ODBC Bridge");
    register("maxdb", "MaxDB");
    register("generic", "McKoi");
    register("mimer", "Mimer");
    register("mysql", "MySQL");
    register("netezza", "Netezza");
    register("oracle", "Oracle Thin");
    register("oracle", "Oracle OCI");
    register("oracle", "Oracle (DataDirect)");
    register("pervasive", "Pervasive");
    register("postgresql", "PostgreSQL");
    register("progress", "Progress");
    register("sqlite", "SQLite");
    register("sqlserver", "SQL Server (DataDirect)");
    register("sqlserver", "SQL Server (jTDS)");
    register("sqlserver", "SQL Server (Microsoft JDBC Driver)");
    register("sqlserver", "SQL Server 2008 (Microsoft JDBC Driver)");
    register("sybase-ase", "Sybase ASE (jTDS)");
    register("sybase-ase", "Sybase ASE (JConnect)");
    register("sybase-asa", "Sybase SQL Anywhere (JConnect)");
    register("sybase", "Sybase (DataDirect)");
  }

  private static void register(String type, String databaseProductName) {
    Collection<String> names = TYPE_NAME.get(type);
    if (names == null) {
      names = new ArrayList<String>();
      TYPE_NAME.put(type, names);
    }
    TYPE_NAME.put(type, names);

    NAME_TYPE.put(databaseProductName, type);
  }

  public static String getDatabaseName(DataSource dataSource) {
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

      String productName = metaData.getDatabaseProductName();
      return NAME_TYPE.get(productName);
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
