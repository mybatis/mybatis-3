/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.jdbc;

import static org.apache.ibatis.jdbc.SqlBuilder.FROM;
import static org.apache.ibatis.jdbc.SqlBuilder.GROUP_BY;
import static org.apache.ibatis.jdbc.SqlBuilder.HAVING;
import static org.apache.ibatis.jdbc.SqlBuilder.INNER_JOIN;
import static org.apache.ibatis.jdbc.SqlBuilder.OR;
import static org.apache.ibatis.jdbc.SqlBuilder.ORDER_BY;
import static org.apache.ibatis.jdbc.SqlBuilder.SELECT;
import static org.apache.ibatis.jdbc.SqlBuilder.SQL;
import static org.apache.ibatis.jdbc.SqlBuilder.WHERE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SqlBuilderTest {

  @Test
  void shouldProduceExpectedSimpleSelectStatement() {
    String expected = """
        SELECT P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME
        FROM PERSON P
        WHERE (P.ID like #id# AND P.FIRST_NAME like #firstName# AND P.LAST_NAME like #lastName#)
        ORDER BY P.LAST_NAME""";
    assertEquals(expected, example2("a", "b", "c"));
  }

  @Test
  void shouldProduceExpectedSimpleSelectStatementMissingFirstParam() {
    String expected = """
        SELECT P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME
        FROM PERSON P
        WHERE (P.FIRST_NAME like #firstName# AND P.LAST_NAME like #lastName#)
        ORDER BY P.LAST_NAME""";
    assertEquals(expected, example2(null, "b", "c"));
  }

  @Test
  void shouldProduceExpectedSimpleSelectStatementMissingFirstTwoParams() {
    String expected = """
        SELECT P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME
        FROM PERSON P
        WHERE (P.LAST_NAME like #lastName#)
        ORDER BY P.LAST_NAME""";
    assertEquals(expected, example2(null, null, "c"));
  }

  @Test
  void shouldProduceExpectedSimpleSelectStatementMissingAllParams() {
    String expected = """
        SELECT P.ID, P.USERNAME, P.PASSWORD, P.FIRST_NAME, P.LAST_NAME
        FROM PERSON P
        ORDER BY P.LAST_NAME""";
    assertEquals(expected, example2(null, null, null));
  }

  @Test
  void shouldProduceExpectedComplexSelectStatement() {
    String expected = """
        SELECT P.ID, P.USERNAME, P.PASSWORD, P.FULL_NAME, P.LAST_NAME, P.CREATED_ON, P.UPDATED_ON
        FROM PERSON P, ACCOUNT A
        INNER JOIN DEPARTMENT D on D.ID = P.DEPARTMENT_ID
        INNER JOIN COMPANY C on D.COMPANY_ID = C.ID
        WHERE (P.ID = A.ID AND P.FIRST_NAME like ?)\s
        OR (P.LAST_NAME like ?)
        GROUP BY P.ID
        HAVING (P.LAST_NAME like ?)\s
        OR (P.FIRST_NAME like ?)
        ORDER BY P.ID, P.FULL_NAME""";
    assertEquals(expected, example1());
  }

  private static String example1() {
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
    return SQL();
  }

  private static String example2(String id, String firstName, String lastName) {
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
    return SQL();
  }

}
