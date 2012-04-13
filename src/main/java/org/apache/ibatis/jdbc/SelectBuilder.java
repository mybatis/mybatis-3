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

public class SelectBuilder {
  private static final String AND = ") \nAND (";
  private static final String OR = ") \nOR (";


  private static final ThreadLocal<SelectSQL> localSQL = new ThreadLocal<SelectSQL>();

  public static void BEGIN() {
    RESET();
  }

  public static void RESET() {
    localSQL.set(new SelectSQL());
  }

  public static void SELECT(String columns) {
    sql().select.add(columns);
  }

  public static void SELECT_DISTINCT(String columns) {
    sql().distinct = true;
    SELECT(columns);
  }

  public static void FROM(String table) {
    sql().from.add(table);
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

  public static String SQL() {
    try {
      StringBuilder builder = new StringBuilder();
      if (sql().distinct) {
          selectClause(builder, "SELECT DISTINCT", sql().select, "", "", ", ");
      } else {
          selectClause(builder, "SELECT", sql().select, "", "", ", ");
      }
      
      selectClause(builder, "FROM", sql().from, "", "", ", ");
      selectClause(builder, "JOIN", sql().join, "", "", "\nJOIN ");
      selectClause(builder, "INNER JOIN", sql().innerJoin, "", "", "\nINNER JOIN ");
      selectClause(builder, "OUTER JOIN", sql().outerJoin, "", "", "\nOUTER JOIN ");
      selectClause(builder, "LEFT OUTER JOIN", sql().leftOuterJoin, "", "", "\nLEFT OUTER JOIN ");
      selectClause(builder, "RIGHT OUTER JOIN", sql().rightOuterJoin, "", "", "\nRIGHT OUTER JOIN ");
      selectClause(builder, "WHERE", sql().where, "(", ")", " AND ");
      selectClause(builder, "GROUP BY", sql().groupBy, "", "", ", ");
      selectClause(builder, "HAVING", sql().having, "(", ")", " AND ");
      selectClause(builder, "ORDER BY", sql().orderBy, "", "", ", ");
      return builder.toString();
    } finally {
      RESET();
    }
  }

  private static void selectClause(StringBuilder builder, String keyword, List<String> parts, String open, String close, String conjunction) {
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

  private static SelectSQL sql() {
    SelectSQL selectSQL = localSQL.get();
    if (selectSQL == null) {
      RESET();
      selectSQL = localSQL.get();
    }
    return selectSQL;
  }

  private static class SelectSQL {
    List<String> select = new ArrayList<String>();
    List<String> from = new ArrayList<String>();
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
    boolean distinct;
  }

}
