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

import java.util.ArrayList;
import java.util.List;

public class SqlBuilder {
  private static final String AND = ") \nAND (";
  private static final String OR = ") \nOR (";

  private static final ThreadLocal<SQL> localSQL = new ThreadLocal<SQL>();

  public static void BEGIN() {
    RESET();
  }

  public static void RESET() {
    localSQL.set(new SQL());
  }

  public static void UPDATE(String table) {
    sql().statementType = SQL.StatementType.UPDATE;
    sql().tables.add(table);
  }

  public static void SET(String sets) {
    sql().sets.add(sets);
  }

  public static String SQL() {
    try {
      return sql().sql();
    } finally {
        RESET();
    }
  }

  public static void INSERT_INTO(String tableName) {
    sql().statementType = SQL.StatementType.INSERT;
    sql().tables.add(tableName);
  }

  public static void VALUES(String columns, String values) {
    sql().columns.add(columns);
    sql().values.add(values);
  }

  public static void SELECT(String columns) {
    sql().statementType = SQL.StatementType.SELECT;
    sql().select.add(columns);
  }

  public static void SELECT_DISTINCT(String columns) {
    sql().distinct = true;
    SELECT(columns);
  }

  public static void DELETE_FROM(String table) {
    sql().statementType = SQL.StatementType.DELETE;
    sql().tables.add(table);
  }

  public static void FROM(String table) {
    sql().tables.add(table);
  }

  public static void JOIN(String join) {
    sql().join.add(join);
  }

  public static void INNER_JOIN(String join) {
    sql().innerJoin.add(join);
  }

  public static void LEFT_OUTER_JOIN(String join) {
    sql().leftOuterJoin.add(join);
  }

  public static void RIGHT_OUTER_JOIN(String join) {
    sql().rightOuterJoin.add(join);
  }

  public static void OUTER_JOIN(String join) {
    sql().outerJoin.add(join);
  }

  public static void WHERE(String conditions) {
    sql().where.add(conditions);
    sql().lastList = sql().where;
  }

  public static void OR() {
    sql().lastList.add(OR);
  }

  public static void AND() {
    sql().lastList.add(AND);
  }

  public static void GROUP_BY(String columns) {
    sql().groupBy.add(columns);
  }

  public static void HAVING(String conditions) {
    sql().having.add(conditions);
    sql().lastList = sql().having;
  }

  public static void ORDER_BY(String columns) {
    sql().orderBy.add(columns);
  }

  private static SQL sql() {
    SQL sql = localSQL.get();
    if (sql == null) {
      RESET();
      sql = localSQL.get();
    }
    return sql;
  }

  private static class SQL {
    public enum StatementType {
        DELETE,
        INSERT,
        SELECT,
        UPDATE
    }
    
    StatementType statementType;
    List<String> sets = new ArrayList<String>();
    List<String> select = new ArrayList<String>();
    List<String> tables = new ArrayList<String>();
    List<String> join = new ArrayList<String>();
    List<String> innerJoin = new ArrayList<String>();
    List<String> outerJoin = new ArrayList<String>();
    List<String> leftOuterJoin = new ArrayList<String>();
    List<String> rightOuterJoin = new ArrayList<String>();
    List<String> where = new ArrayList<String>();
    List<String> having = new ArrayList<String>();
    List<String> groupBy = new ArrayList<String>();
    List<String> orderBy = new ArrayList<String>();
    List<String> lastList = new ArrayList<String>();
    List<String> columns = new ArrayList<String>();
    List<String> values = new ArrayList<String>();
    boolean distinct;

    private void sqlClause(StringBuilder builder, String keyword, List<String> parts, String open, String close, String conjunction) {
      if (!parts.isEmpty()) {
        if (builder.length() > 0) builder.append("\n");
        builder.append(keyword);
        builder.append(" ");
        builder.append(open);
        String last = "________";
        for (int i = 0, n = parts.size(); i < n; i++) {
          String part = parts.get(i);
          if (i > 0 && !part.equals(AND) && !part.equals(OR) && !last.equals(AND) && !last.equals(OR)) {
            builder.append(conjunction);
          }
          builder.append(part);
          last = part;
        }
        builder.append(close);
      }
    }

    private String selectSQL() {
      StringBuilder builder = new StringBuilder();
      if (distinct) {
        sqlClause(builder, "SELECT DISTINCT", select, "", "", ", ");
      } else {
        sqlClause(builder, "SELECT", select, "", "", ", ");
      }

      sqlClause(builder, "FROM", tables, "", "", ", ");
      sqlClause(builder, "JOIN", join, "", "", "\nJOIN ");
      sqlClause(builder, "INNER JOIN", innerJoin, "", "", "\nINNER JOIN ");
      sqlClause(builder, "OUTER JOIN", outerJoin, "", "", "\nOUTER JOIN ");
      sqlClause(builder, "LEFT OUTER JOIN", leftOuterJoin, "", "", "\nLEFT OUTER JOIN ");
      sqlClause(builder, "RIGHT OUTER JOIN", rightOuterJoin, "", "", "\nRIGHT OUTER JOIN ");
      sqlClause(builder, "WHERE", where, "(", ")", " AND ");
      sqlClause(builder, "GROUP BY", groupBy, "", "", ", ");
      sqlClause(builder, "HAVING", having, "(", ")", " AND ");
      sqlClause(builder, "ORDER BY", orderBy, "", "", ", ");
      return builder.toString();
    }

    private String insertSQL() {
      StringBuilder builder = new StringBuilder();

      sqlClause(builder, "INSERT INTO", tables, "", "", "");
      sqlClause(builder, "", columns, "(", ")", ", ");
      sqlClause(builder, "VALUES", values, "(", ")", ", ");
      return builder.toString();
    }

    private String deleteSQL() {
      StringBuilder builder = new StringBuilder();

      sqlClause(builder, "DELETE FROM", tables, "", "", "");
      sqlClause(builder, "WHERE", where, "(", ")", " AND ");
      return builder.toString();
    }

    private String updateSQL() {
      StringBuilder builder = new StringBuilder();

      sqlClause(builder, "UPDATE", tables, "", "", "");
      sqlClause(builder, "SET", sets, "", "", ", ");
      sqlClause(builder, "WHERE", where, "(", ")", " AND ");
      return builder.toString();
    }

    public String sql() {
      if (statementType == null) {
        return null;
      }
      
      String answer;
      
      switch (statementType) {
      case DELETE:
        answer = deleteSQL();
        break;
          
      case INSERT:
        answer = insertSQL();
        break;
      
      case SELECT:
        answer = selectSQL();
        break;
          
      case UPDATE:
        answer = updateSQL();
        break;
          
      default:
        answer = null;
      }

      return answer;
    }
  }
}
