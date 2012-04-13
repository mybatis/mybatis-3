/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.jdbc;

import static java.util.Arrays.asList;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public class DataSourceUtils {

  /*
   * For each type, the supported DatabaseProductNameS
   *
   * TODO is this useful?
   */
  private static Map<String, Collection<String>> TYPE_NAME = new HashMap<String, Collection<String>>();

  /*
   * For each DatabaseProductName, the related type
   */
  private static Map<String, String> NAME_TYPE = new HashMap<String, String>();

  static {
    register("cache", "Cache");
    register("db2", "DB2",
                    "DB2 (DataDirect)");
    register("generic", "DB2 for AS/400 (JTOpen)",
                        "JDBC/ODBC Bridge", "McKoi");
    register("firebird", "Firebird");
    register("frontbase", "FrontBase");
    register("neoview", "HP Neoview");
    register("hsql", "HSQLDB server",
                     "HSQLDB embedded");
    register("h2", "H2 server",
                   "H2 embedded");
    register("informix", "Informix",
                         "Informix (DataDirect)");
    register("derby", "JavaDB/Derby server",
                      "JavaDB/Derby embedded");
    register("jdatastore", "JDataStore");
    register("maxdb", "MaxDB");
    register("mimer", "Mimer");
    register("mysql", "MySQL");
    register("netezza", "Netezza");
    register("oracle", "Oracle Thin",
                       "Oracle OCI",
                       "Oracle (DataDirect)");
    register("pervasive", "Pervasive");
    register("postgresql", "PostgreSQL");
    register("progress", "Progress");
    register("sqlite", "SQLite");
    register("sqlserver", "SQL Server (DataDirect)",
                          "SQL Server (jTDS)",
                          "SQL Server (Microsoft JDBC Driver)",
                          "SQL Server 2008 (Microsoft JDBC Driver)" );
    register("sybase-ase", "Sybase ASE (jTDS)",
                           "Sybase ASE (JConnect)",
                           "Sybase SQL Anywhere (JConnect)");
    register("sybase", "Sybase (DataDirect)");
  }

  private static void register(String type, String...databaseProductNames) {
    TYPE_NAME.put(type, asList(databaseProductNames));

    for (String databaseProductName : databaseProductNames) {
      NAME_TYPE.put(databaseProductName, type);
    }
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
