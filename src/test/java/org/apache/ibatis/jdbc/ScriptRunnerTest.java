/*
 *    Copyright 2009-2020 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ScriptRunnerTest extends BaseDataTest {

  private static final String LINE_SEPARATOR = System.lineSeparator();

  @Test
  @Disabled("This fails with HSQLDB 2.0 due to the create index statements in the schema script")
  void shouldRunScriptsBySendingFullScriptAtOnce() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setSendFullScript(true);
    runner.setAutoCommit(true);
    runner.setStopOnError(false);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);
    conn.close();
    runJPetStoreScripts(runner);
    assertProductsTableExistsAndLoaded();
  }

  @Test
  void shouldRunScriptsUsingConnection() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    try (Connection conn = ds.getConnection()) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setAutoCommit(true);
      runner.setStopOnError(false);
      runner.setErrorLogWriter(null);
      runner.setLogWriter(null);
      runJPetStoreScripts(runner);
    }
    assertProductsTableExistsAndLoaded();
  }

  @Test
  void shouldRunScriptsUsingProperties() throws Exception {
    Properties props = Resources.getResourceAsProperties(JPETSTORE_PROPERTIES);
    DataSource dataSource = new UnpooledDataSource(
        props.getProperty("driver"),
        props.getProperty("url"),
        props.getProperty("username"),
        props.getProperty("password"));
    ScriptRunner runner = new ScriptRunner(dataSource.getConnection());
    runner.setAutoCommit(true);
    runner.setStopOnError(false);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);
    runJPetStoreScripts(runner);
    assertProductsTableExistsAndLoaded();
  }

  @Test
  void shouldReturnWarningIfEndOfLineTerminatorNotFound() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    String resource = "org/apache/ibatis/jdbc/ScriptMissingEOLTerminator.sql";
    try (Connection conn = ds.getConnection();
         Reader reader = Resources.getResourceAsReader(resource)) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setAutoCommit(true);
      runner.setStopOnError(false);
      runner.setErrorLogWriter(null);
      runner.setLogWriter(null);

      try {
        runner.runScript(reader);
        fail("Expected script runner to fail due to missing end of line terminator.");
      } catch (Exception e) {
        assertTrue(e.getMessage().contains("end-of-line terminator"));
      }
    }
  }

  @Test
  void commentAferStatementDelimiterShouldNotCauseRunnerFail() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    String resource = "org/apache/ibatis/jdbc/ScriptCommentAfterEOLTerminator.sql";
    try (Connection conn = ds.getConnection();
         Reader reader = Resources.getResourceAsReader(resource)) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setAutoCommit(true);
      runner.setStopOnError(true);
      runner.setErrorLogWriter(null);
      runner.setLogWriter(null);
      runJPetStoreScripts(runner);
      runner.runScript(reader);
    }
  }

  @Test
  void shouldReturnWarningIfNotTheCurrentDelimiterUsed() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    String resource = "org/apache/ibatis/jdbc/ScriptChangingDelimiterMissingDelimiter.sql";
    try (Connection conn = ds.getConnection();
         Reader reader = Resources.getResourceAsReader(resource)) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setAutoCommit(false);
      runner.setStopOnError(true);
      runner.setErrorLogWriter(null);
      runner.setLogWriter(null);
      try {
        runner.runScript(reader);
        fail("Expected script runner to fail due to the usage of invalid delimiter.");
      } catch (Exception e) {
        assertTrue(e.getMessage().contains("end-of-line terminator"));
      }
    }
  }

  @Test
  void changingDelimiterShouldNotCauseRunnerFail() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    String resource = "org/apache/ibatis/jdbc/ScriptChangingDelimiter.sql";
    try (Connection conn = ds.getConnection();
         Reader reader = Resources.getResourceAsReader(resource)) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setAutoCommit(false);
      runner.setStopOnError(true);
      runner.setErrorLogWriter(null);
      runner.setLogWriter(null);
      runJPetStoreScripts(runner);
      runner.runScript(reader);
    }
  }

  @Test
  void testLogging() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    try (Connection conn = ds.getConnection()) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setAutoCommit(true);
      runner.setStopOnError(false);
      runner.setErrorLogWriter(null);
      runner.setSendFullScript(false);
      StringWriter sw = new StringWriter();
      PrintWriter logWriter = new PrintWriter(sw);
      runner.setLogWriter(logWriter);

      Reader reader = new StringReader("select userid from account where userid = 'j2ee';");
      runner.runScript(reader);

      assertEquals(
              "select userid from account where userid = 'j2ee'" + LINE_SEPARATOR
                      + LINE_SEPARATOR + "USERID\t" + LINE_SEPARATOR
                      + "j2ee\t" + LINE_SEPARATOR, sw.toString());
    }
  }

  @Test
  void testLoggingFullScipt() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    try (Connection conn = ds.getConnection()) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setAutoCommit(true);
      runner.setStopOnError(false);
      runner.setErrorLogWriter(null);
      runner.setSendFullScript(true);
      StringWriter sw = new StringWriter();
      PrintWriter logWriter = new PrintWriter(sw);
      runner.setLogWriter(logWriter);

      Reader reader = new StringReader("select userid from account where userid = 'j2ee';");
      runner.runScript(reader);

      assertEquals(
              "select userid from account where userid = 'j2ee';" + LINE_SEPARATOR
                      + LINE_SEPARATOR + "USERID\t" + LINE_SEPARATOR
                      + "j2ee\t" + LINE_SEPARATOR, sw.toString());
    }
  }

  private void runJPetStoreScripts(ScriptRunner runner) throws IOException, SQLException {
    runScript(runner, JPETSTORE_DDL);
    runScript(runner, JPETSTORE_DATA);
  }

  private void assertProductsTableExistsAndLoaded() throws IOException, SQLException {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    try (Connection conn = ds.getConnection()) {
      SqlRunner executor = new SqlRunner(conn);
      List<Map<String, Object>> products = executor.selectAll("SELECT * FROM PRODUCT");
      assertEquals(16, products.size());
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  void shouldAcceptDelimiterVariations() throws Exception {
    Connection conn = mock(Connection.class);
    Statement stmt = mock(Statement.class);
    when(conn.createStatement()).thenReturn(stmt);
    when(stmt.getUpdateCount()).thenReturn(-1);
    ScriptRunner runner = new ScriptRunner(conn);

    String sql = "-- @DELIMITER | \n"
        + "line 1;\n"
        + "line 2;\n"
        + "|\n"
        + "//  @DELIMITER  ;\n"
        + "line 3; \n"
        + "-- //@deLimiTer $  blah\n"
        + "line 4$\n"
        + "// //@DELIMITER %\n"
        + "line 5%\n";
    Reader reader = new StringReader(sql);
    runner.runScript(reader);

    verify(stmt, Mockito.times(1)).execute(eq("line 1;" + LINE_SEPARATOR + "line 2;" + LINE_SEPARATOR + LINE_SEPARATOR));
    verify(stmt, Mockito.times(1)).execute(eq("line 3" + LINE_SEPARATOR));
    verify(stmt, Mockito.times(1)).execute(eq("line 4" + LINE_SEPARATOR));
    verify(stmt, Mockito.times(1)).execute(eq("line 5" + LINE_SEPARATOR));
  }

  @Test
  void test() {
    StringBuilder sb = new StringBuilder();
    StringBuilder sb2 = y(sb);
    assertSame(sb, sb2);
  }

  private StringBuilder y(StringBuilder sb) {
    sb.append("ABC");
    return sb;
  }

  @Test
  void shouldAcceptMultiCharDelimiter() throws Exception {
    Connection conn = mock(Connection.class);
    Statement stmt = mock(Statement.class);
    when(conn.createStatement()).thenReturn(stmt);
    when(stmt.getUpdateCount()).thenReturn(-1);
    ScriptRunner runner = new ScriptRunner(conn);

    String sql = "-- @DELIMITER || \n"
        + "line 1;\n"
        + "line 2;\n"
        + "||\n"
        + "//  @DELIMITER  ;\n"
        + "line 3; \n";
    Reader reader = new StringReader(sql);
    runner.runScript(reader);

    verify(stmt, Mockito.times(1)).execute(eq("line 1;" + LINE_SEPARATOR + "line 2;" + LINE_SEPARATOR + LINE_SEPARATOR));
    verify(stmt, Mockito.times(1)).execute(eq("line 3" + LINE_SEPARATOR));
  }
}
