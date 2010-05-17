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
