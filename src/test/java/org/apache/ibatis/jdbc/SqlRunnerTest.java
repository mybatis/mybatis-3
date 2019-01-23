/**
 *    Copyright 2009-2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.junit.jupiter.api.Test;

class SqlRunnerTest extends BaseDataTest {

  @Test
  void shouldSelectOne() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    try (Connection connection = ds.getConnection()) {
      SqlRunner exec = new SqlRunner(connection);
      Map<String, Object> row = exec.selectOne("SELECT * FROM PRODUCT WHERE PRODUCTID = ?", "FI-SW-01");
      assertEquals("FI-SW-01", row.get("PRODUCTID"));
    }
  }

  @Test
  void shouldSelectList() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    try (Connection connection = ds.getConnection()) {
      SqlRunner exec = new SqlRunner(connection);
      List<Map<String, Object>> rows = exec.selectAll("SELECT * FROM PRODUCT");
      assertEquals(16, rows.size());
    }
  }

  @Test
  void shouldInsert() throws Exception {
    DataSource ds = createUnpooledDataSource(BLOG_PROPERTIES);
    runScript(ds, BLOG_DDL);
    try (Connection connection = ds.getConnection()) {
      SqlRunner exec = new SqlRunner(connection);
      exec.setUseGeneratedKeySupport(true);
      int id = exec.insert("INSERT INTO author (username, password, email, bio) VALUES (?,?,?,?)", "someone", "******", "someone@apache.org", Null.LONGVARCHAR);
      Map<String, Object> row = exec.selectOne("SELECT * FROM author WHERE username = ?", "someone");
      connection.rollback();
      assertTrue(SqlRunner.NO_GENERATED_KEY != id);
      assertEquals("someone", row.get("USERNAME"));
    }
  }

  @Test
  void shouldUpdateCategory() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    try (Connection connection = ds.getConnection()) {
      SqlRunner exec = new SqlRunner(connection);
      int count = exec.update("update product set category = ? where productid = ?", "DOGS", "FI-SW-01");
      Map<String, Object> row = exec.selectOne("SELECT * FROM PRODUCT WHERE PRODUCTID = ?", "FI-SW-01");
      assertEquals("DOGS", row.get("CATEGORY"));
      assertEquals(1, count);
    }
  }

  @Test
  void shouldDeleteOne() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    try (Connection connection = ds.getConnection()) {
      SqlRunner exec = new SqlRunner(connection);
      int count = exec.delete("delete from item");
      List<Map<String, Object>> rows = exec.selectAll("SELECT * FROM ITEM");
      assertEquals(28, count);
      assertEquals(0, rows.size());
    }
  }

  @Test
  void shouldDemonstrateDDLThroughRunMethod() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    try (Connection connection = ds.getConnection()) {
      SqlRunner exec = new SqlRunner(connection);
      exec.run("CREATE TABLE BLAH(ID INTEGER)");
      exec.run("insert into BLAH values (1)");
      List<Map<String, Object>> rows = exec.selectAll("SELECT * FROM BLAH");
      exec.run("DROP TABLE BLAH");
      assertEquals(1, rows.size());
    }
  }
}
