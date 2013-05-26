/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseFactory {
  private DatabaseFactory() {
  }

  public static Database newDatabase(Connection conn, String catalogFilter, String schemaFilter) throws SQLException {
    Database database = new Database(catalogFilter, schemaFilter);
    ResultSet rs = null;
    try {
      DatabaseMetaData dbmd = conn.getMetaData();

      try {
        rs = dbmd.getColumns(catalogFilter, schemaFilter, null, null);
        while (rs.next()) {
          String catalogName = rs.getString("TABLE_CAT");
          String schemaName = rs.getString("TABLE_SCHEM");
          String tableName = rs.getString("TABLE_NAME");
          String columnName = rs.getString("COLUMN_NAME");
          int dataType = Integer.parseInt(rs.getString("DATA_TYPE"));
          Table table = database.getTable(tableName);
          if (table == null) {
            table = new Table(tableName);
            table.setCatalog(catalogName);
            table.setSchema(schemaName);
            database.addTable(table);
          }
          table.addColumn(new Column(columnName, dataType));
        }
      } finally {
        if (rs != null) rs.close();
      }

      try {
        String[] tableNames = database.getTableNames();
        for (int i = 0; i < tableNames.length; i++) {
          Table table = database.getTable(tableNames[i]);
          rs = dbmd.getPrimaryKeys(catalogFilter, schemaFilter, table.getName());
          if (rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");
            table.setPrimaryKey(table.getColumn(columnName));
          }
        }
      } finally {
        if (rs != null) rs.close();
      }

    } finally {
      try {
        conn.rollback();
      } catch (Exception e) { /*ignore*/ }
    }
    return database;
  }

}
