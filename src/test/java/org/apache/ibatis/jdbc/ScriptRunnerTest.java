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

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
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

public class ScriptRunnerTest extends BaseDataTest {

  @Test
  @Ignore("This fails with HSQLDB 2.0 due to the create index statements in the schema script")
  public void shouldRunScriptsBySendingFullScriptAtOnce() throws Exception {
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
  public void shouldRunScriptsUsingConnection() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setAutoCommit(true);
    runner.setStopOnError(false);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);
    runJPetStoreScripts(runner);
    conn.close();
    assertProductsTableExistsAndLoaded();
  }

  @Test
  public void shouldRunScriptsUsingProperties() throws Exception {
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
  public void shouldReturnWarningIfEndOfLineTerminatorNotFound() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setAutoCommit(true);
    runner.setStopOnError(false);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);

    String resource = "org/apache/ibatis/jdbc/ScriptMissingEOLTerminator.sql";
    Reader reader = Resources.getResourceAsReader(resource);

    try {
      runner.runScript(reader);
      fail("Expected script runner to fail due to missing end of line terminator.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("end-of-line terminator"));
    }
    reader.close();
    conn.close();
  }

  @Test
  public void commentAferStatementDelimiterShouldNotCauseRunnerFail() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setAutoCommit(true);
    runner.setStopOnError(true);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);
    runJPetStoreScripts(runner);

    String resource = "org/apache/ibatis/jdbc/ScriptCommentAfterEOLTerminator.sql";
    Reader reader = Resources.getResourceAsReader(resource);

    try {
      runner.runScript(reader);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    reader.close();
    conn.close();
  }

  @Test
  public void shouldReturnWarningIfNotTheCurrentDelimiterUsed() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setAutoCommit(false);
    runner.setStopOnError(true);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);

    String resource = "org/apache/ibatis/jdbc/ScriptChangingDelimiterMissingDelimiter.sql";
    Reader reader = Resources.getResourceAsReader(resource);

    try {
      runner.runScript(reader);
      fail("Expected script runner to fail due to the usage of invalid delimiter.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("end-of-line terminator"));
    }
    reader.close();
    conn.close();
  }

  @Test
  public void changingDelimiterShouldNotCauseRunnerFail() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setAutoCommit(false);
    runner.setStopOnError(true);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);
    runJPetStoreScripts(runner);

    String resource = "org/apache/ibatis/jdbc/ScriptChangingDelimiter.sql";
    Reader reader = Resources.getResourceAsReader(resource);

    try {
      runner.runScript(reader);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    reader.close();
    conn.close();
  }

  @Test
  public void testLogging() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
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
    conn.close();

    assertEquals(
            "select userid from account where userid = 'j2ee'" + System.getProperty("line.separator")
                    + System.getProperty("line.separator") + "USERID\t" + System.getProperty("line.separator")
                    + "j2ee\t" + System.getProperty("line.separator"), sw.toString());
  }

  @Test
  public void testLoggingFullScipt() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
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
    conn.close();

    assertEquals(
            "select userid from account where userid = 'j2ee';" + System.getProperty("line.separator")
                    + System.getProperty("line.separator") + "USERID\t" + System.getProperty("line.separator")
                    + "j2ee\t" + System.getProperty("line.separator"), sw.toString());
  }

  private void runJPetStoreScripts(ScriptRunner runner) throws IOException, SQLException {
    runScript(runner, JPETSTORE_DDL);
    runScript(runner, JPETSTORE_DATA);
  }

  private void assertProductsTableExistsAndLoaded() throws IOException, SQLException {
    PooledDataSource ds = createPooledDataSource(JPETSTORE_PROPERTIES);
    try {
      Connection conn = ds.getConnection();
      SqlRunner executor = new SqlRunner(conn);
      List<Map<String, Object>> products = executor.selectAll("SELECT * FROM PRODUCT");
      assertEquals(16, products.size());
      conn.close();
    } finally {
      ds.forceCloseAll();
    }
  }

  @Test
  public void shouldAcceptDelimiterVariations() throws Exception {
    Connection conn = mock(Connection.class);
    Statement stmt = mock(Statement.class);
    when(conn.createStatement()).thenReturn(stmt);
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

    verify(stmt, Mockito.times(1)).execute(eq("line 1;\n" + "line 2;\n\n"));
    verify(stmt, Mockito.times(1)).execute(eq("line 3\n"));
    verify(stmt, Mockito.times(1)).execute(eq("line 4\n"));
    verify(stmt, Mockito.times(1)).execute(eq("line 5\n"));
  }

  @Test
  public void test() throws Exception {
    StringBuilder sb = new StringBuilder();
    StringBuilder sb2 = y(sb);
    assertTrue(sb == sb2);
  }

  private StringBuilder y(StringBuilder sb) {
    sb.append("ABC");
    return sb;
  }

  @Test
  public void shouldAcceptMultiCharDelimiter() throws Exception {
    Connection conn = mock(Connection.class);
    Statement stmt = mock(Statement.class);
    when(conn.createStatement()).thenReturn(stmt);
    ScriptRunner runner = new ScriptRunner(conn);

    String sql = "-- @DELIMITER || \n"
        + "line 1;\n"
        + "line 2;\n"
        + "||\n"
        + "//  @DELIMITER  ;\n"
        + "line 3; \n";
    Reader reader = new StringReader(sql);
    runner.runScript(reader);

    verify(stmt, Mockito.times(1)).execute(eq("line 1;\n" + "line 2;\n\n"));
    verify(stmt, Mockito.times(1)).execute(eq("line 3\n"));
  }
}
