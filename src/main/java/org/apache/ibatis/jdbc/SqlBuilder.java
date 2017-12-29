/**
 *    Copyright 2009-2016 the original author or authors.
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

/**
 * @deprecated Use the {@link SQL} Class
 *
 * @author Jeff Butler
 */
public class SqlBuilder {

  private static final ThreadLocal<SQL> localSQL = new ThreadLocal<SQL>();

  static {
    BEGIN();
  }

  private SqlBuilder() {
    // Prevent Instantiation
  }

  public static void BEGIN() {
    RESET();
  }

  public static void RESET() {
    localSQL.set(new SQL());
  }

  public static void UPDATE(String table) {
    sql().UPDATE(table);
  }

  public static void SET(String sets) {
    sql().SET(sets);
  }

  public static String SQL() {
    try {
      return sql().toString();
    } finally {
        RESET();
    }
  }

  public static void INSERT_INTO(String tableName) {
    sql().INSERT_INTO(tableName);
  }

  public static void VALUES(String columns, String values) {
    sql().VALUES(columns, values);
  }

  public static void SELECT(String columns) {
    sql().SELECT(columns);
  }

  public static void SELECT_DISTINCT(String columns) {
    sql().SELECT_DISTINCT(columns);
  }

  public static void DELETE_FROM(String table) {
    sql().DELETE_FROM(table);
  }

  public static void FROM(String table) {
    sql().FROM(table);
  }

  public static void JOIN(String join) {
    sql().JOIN(join);
  }

  public static void INNER_JOIN(String join) {
    sql().INNER_JOIN(join);
  }

  public static void LEFT_OUTER_JOIN(String join) {
    sql().LEFT_OUTER_JOIN(join);
  }

  public static void RIGHT_OUTER_JOIN(String join) {
    sql().RIGHT_OUTER_JOIN(join);
  }

  public static void OUTER_JOIN(String join) {
    sql().OUTER_JOIN(join);
  }

  public static void WHERE(String conditions) {
    sql().WHERE(conditions);
  }

  public static void OR() {
    sql().OR();
  }

  public static void AND() {
    sql().AND();
  }

  public static void GROUP_BY(String columns) {
    sql().GROUP_BY(columns);
  }

  public static void HAVING(String conditions) {
    sql().HAVING(conditions);
  }

  public static void ORDER_BY(String columns) {
    sql().ORDER_BY(columns);
  }

  private static SQL sql() {
    return localSQL.get();
  }

}
