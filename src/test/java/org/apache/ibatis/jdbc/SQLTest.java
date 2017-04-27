/**
 *    Copyright 2009-2017 the original author or authors.
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class SQLTest {

  @Test
  public void shouldDemonstrateProvidedStringBuilder() {
    //You can pass in your own StringBuilder
    final StringBuilder sb = new StringBuilder();
    //From the tutorial
    final String sql = example1().usingAppender(sb).toString();

    assertEquals("SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON\n" +
        "FROM PERSON P, ACCOUNT A\n" +
        "INNER JOIN DEPARTMENT D on D.ID = P.DEPARTMENT_ID\n" +
        "INNER JOIN COMPANY C on D.COMPANY_ID = C.ID\n" +
        "WHERE (P.ID = A.ID AND P.FIRST_NAME like ?) \n" +
        "OR (P.LAST_NAME like ?)\n" +
        "GROUP BY P.ID\n" +
        "HAVING (P.LAST_NAME like ?) \n" +
        "OR (P.FIRST_NAME like ?)\n" +
        "ORDER BY P.ID, P.FULL_NAME", sql);
  }

  @Test
  public void shouldDemonstrateMixedStyle() {
    //Mixed
    final String sql = new SQL() {{
      SELECT("id, name");
      FROM("PERSON A");
      WHERE("name like ?").WHERE("id = ?");
    }}.toString();

    assertEquals("" +
        "SELECT id, name\n" +
        "FROM PERSON A\n" +
        "WHERE (name like ? AND id = ?)", sql);
  }

  @Test
  public void shouldDemonstrateFluentStyle() {
    //Fluent Style
    final String sql = new SQL()
        .SELECT("id, name").FROM("PERSON A")
        .WHERE("name like ?")
        .WHERE("id = ?").toString();

    assertEquals("" +
        "SELECT id, name\n" +
        "FROM PERSON A\n" +
        "WHERE (name like ? AND id = ?)", sql);
  }

  @Test
  public void shouldProduceExpectedSimpleSelectStatement() {
    final String expected =
        "SELECT P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME\n" +
            "FROM PERSON P\n" +
            "WHERE (P.ID like #id# AND P.FIRST_NAME like #firstName# AND P.LAST_NAME like #lastName#)\n" +
            "ORDER BY P.LAST_NAME";
    assertEquals(expected, example2("a", "b", "c"));
  }

  @Test
  public void shouldProduceExpectedSimpleSelectStatementMissingFirstParam() {
    final String expected =
        "SELECT P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME\n" +
            "FROM PERSON P\n" +
            "WHERE (P.FIRST_NAME like #firstName# AND P.LAST_NAME like #lastName#)\n" +
            "ORDER BY P.LAST_NAME";
    assertEquals(expected, example2(null, "b", "c"));
  }

  @Test
  public void shouldProduceExpectedSimpleSelectStatementMissingFirstTwoParams() {
    final String expected =
        "SELECT P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME\n" +
            "FROM PERSON P\n" +
            "WHERE (P.LAST_NAME like #lastName#)\n" +
            "ORDER BY P.LAST_NAME";
    assertEquals(expected, example2(null, null, "c"));
  }

  @Test
  public void shouldProduceExpectedSimpleSelectStatementMissingAllParams() {
    final String expected =
        "SELECT P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME\n" +
            "FROM PERSON P\n" +
            "ORDER BY P.LAST_NAME";
    assertEquals(expected, example2(null, null, null));
  }

  @Test
  public void shouldProduceExpectedComplexSelectStatement() {
    final String expected =
        "SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON\n" +
            "FROM PERSON P, ACCOUNT A\n" +
            "INNER JOIN DEPARTMENT D on D.ID = P.DEPARTMENT_ID\n" +
            "INNER JOIN COMPANY C on D.COMPANY_ID = C.ID\n" +
            "WHERE (P.ID = A.ID AND P.FIRST_NAME like ?) \n" +
            "OR (P.LAST_NAME like ?)\n" +
            "GROUP BY P.ID\n" +
            "HAVING (P.LAST_NAME like ?) \n" +
            "OR (P.FIRST_NAME like ?)\n" +
            "ORDER BY P.ID, P.FULL_NAME";
    assertEquals(expected, example1().toString());
  }

  private static SQL example1() {
    return new SQL() {{
      SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME");
      SELECT("P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON");
      FROM("PERSON P");
      FROM("ACCOUNT A");
      INNER_JOIN("DEPARTMENT D on D.ID = P.DEPARTMENT_ID");
      INNER_JOIN("COMPANY C on D.COMPANY_ID = C.ID");
      WHERE("P.ID = A.ID");
      WHERE("P.FIRST_NAME like ?");
      OR();
      WHERE("P.LAST_NAME like ?");
      GROUP_BY("P.ID");
      HAVING("P.LAST_NAME like ?");
      OR();
      HAVING("P.FIRST_NAME like ?");
      ORDER_BY("P.ID");
      ORDER_BY("P.FULL_NAME");
    }};
  }

  private static String example2(final String id, final String firstName, final String lastName) {
    return new SQL() {{
      SELECT("P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME");
      FROM("PERSON P");
      if (id != null) {
        WHERE("P.ID like #id#");
      }
      if (firstName != null) {
        WHERE("P.FIRST_NAME like #firstName#");
      }
      if (lastName != null) {
        WHERE("P.LAST_NAME like #lastName#");
      }
      ORDER_BY("P.LAST_NAME");
    }}.toString();
  }


  @Test
  public void variableLengthArgumentOnSelect() {
    final String sql = new SQL() {{
      SELECT("P.ID", "P.USERNAME");
    }}.toString();

    assertEquals("SELECT P.ID, P.USERNAME", sql);
  }

  @Test
  public void variableLengthArgumentOnSelectDistinct() {
    final String sql = new SQL() {{
      SELECT_DISTINCT("P.ID", "P.USERNAME");
    }}.toString();

    assertEquals("SELECT DISTINCT P.ID, P.USERNAME", sql);
  }

  @Test
  public void variableLengthArgumentOnFrom() {
    final String sql = new SQL() {{
      SELECT().FROM("TABLE_A a", "TABLE_B b");
    }}.toString();

    assertEquals("FROM TABLE_A a, TABLE_B b", sql);
  }

  @Test
  public void variableLengthArgumentOnJoin() {
    final String sql = new SQL() {{
      SELECT().JOIN("TABLE_A b ON b.id = a.id", "TABLE_C c ON c.id = a.id");
    }}.toString();

    assertEquals("JOIN TABLE_A b ON b.id = a.id\n" +
        "JOIN TABLE_C c ON c.id = a.id", sql);
  }

  @Test
  public void variableLengthArgumentOnInnerJoin() {
    final String sql = new SQL() {{
      SELECT().INNER_JOIN("TABLE_A b ON b.id = a.id", "TABLE_C c ON c.id = a.id");
    }}.toString();

    assertEquals("INNER JOIN TABLE_A b ON b.id = a.id\n" +
        "INNER JOIN TABLE_C c ON c.id = a.id", sql);
  }

  @Test
  public void variableLengthArgumentOnOuterJoin() {
    final String sql = new SQL() {{
      SELECT().OUTER_JOIN("TABLE_A b ON b.id = a.id", "TABLE_C c ON c.id = a.id");
    }}.toString();

    assertEquals("OUTER JOIN TABLE_A b ON b.id = a.id\n" +
        "OUTER JOIN TABLE_C c ON c.id = a.id", sql);
  }

  @Test
  public void variableLengthArgumentOnLeftOuterJoin() {
    final String sql = new SQL() {{
      SELECT().LEFT_OUTER_JOIN("TABLE_A b ON b.id = a.id", "TABLE_C c ON c.id = a.id");
    }}.toString();

    assertEquals("LEFT OUTER JOIN TABLE_A b ON b.id = a.id\n" +
        "LEFT OUTER JOIN TABLE_C c ON c.id = a.id", sql);
  }

  @Test
  public void variableLengthArgumentOnRightOuterJoin() {
    final String sql = new SQL() {{
      SELECT().RIGHT_OUTER_JOIN("TABLE_A b ON b.id = a.id", "TABLE_C c ON c.id = a.id");
    }}.toString();

    assertEquals("RIGHT OUTER JOIN TABLE_A b ON b.id = a.id\n" +
        "RIGHT OUTER JOIN TABLE_C c ON c.id = a.id", sql);
  }

  @Test
  public void variableLengthArgumentOnWhere() {
    final String sql = new SQL() {{
      SELECT().WHERE("a = #{a}", "b = #{b}");
    }}.toString();

    assertEquals("WHERE (a = #{a} AND b = #{b})", sql);
  }

  @Test
  public void variableLengthArgumentOnGroupBy() {
    final String sql = new SQL() {{
      SELECT().GROUP_BY("a", "b");
    }}.toString();

    assertEquals("GROUP BY a, b", sql);
  }

  @Test
  public void variableLengthArgumentOnHaving() {
    final String sql = new SQL() {{
      SELECT().HAVING("a = #{a}", "b = #{b}");
    }}.toString();

    assertEquals("HAVING (a = #{a} AND b = #{b})", sql);
  }

  @Test
  public void variableLengthArgumentOnOrderBy() {
    final String sql = new SQL() {{
      SELECT().ORDER_BY("a", "b");
    }}.toString();

    assertEquals("ORDER BY a, b", sql);
  }

  @Test
  public void variableLengthArgumentOnSet() {
    final String sql = new SQL() {{
      UPDATE("TABLE_A").SET("a = #{a}", "b = #{b}");
    }}.toString();

    assertEquals("UPDATE TABLE_A\n" +
        "SET a = #{a}, b = #{b}", sql);
  }

  @Test
  public void variableLengthArgumentOnIntoColumnsAndValues() {
    final String sql = new SQL() {{
      INSERT_INTO("TABLE_A").INTO_COLUMNS("a", "b").INTO_VALUES("#{a}", "#{b}");
    }}.toString();

    System.out.println(sql);

    assertEquals("INSERT INTO TABLE_A\n (a, b)\nVALUES (#{a}, #{b})", sql);
  }

  @Test
  public void fixFor903UpdateJoins() {
    final SQL sql = new SQL().UPDATE("table1 a").INNER_JOIN("table2 b USING (ID)").SET("a.value = b.value");
    assertThat(sql.toString()).isEqualTo("UPDATE table1 a\nINNER JOIN table2 b USING (ID)\nSET a.value = b.value");
  }
}
