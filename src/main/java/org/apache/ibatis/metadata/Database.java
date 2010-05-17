package org.apache.ibatis.metadata;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Database {

  private String catalog;
  private String schema;

  private Map tables = new HashMap();

  public Database(String catalog, String schema) {
    this.catalog = catalog;
    this.schema = schema;
  }

  public String getCatalog() {
    return catalog;
  }

  public String getSchema() {
    return schema;
  }

  public void addTable(Table table) {
    tables.put(table.getName().toUpperCase(Locale.ENGLISH), table);
  }

  public Table getTable(String name) {
    return (Table) tables.get(name.toUpperCase(Locale.ENGLISH));
  }

  public String[] getTableNames() {
    return (String[]) tables.keySet().toArray(new String[tables.size()]);
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final Database database = (Database) o;

    if (catalog != null ? !catalog.equals(database.catalog) : database.catalog != null) return false;
    if (schema != null ? !schema.equals(database.schema) : database.schema != null) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (catalog != null ? catalog.hashCode() : 0);
    result = 29 * result + (schema != null ? schema.hashCode() : 0);
    return result;
  }

}
