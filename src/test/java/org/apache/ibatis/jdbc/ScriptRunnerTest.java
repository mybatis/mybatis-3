/*
 *    Copyright 2009-2012 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.junit.Ignore;
import org.junit.Test;

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
  }


  @Test
  public void storedProceduresShouldBeCreatedInExecuteLineByLineMode() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection conn = ds.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setAutoCommit(true);
    runner.setSendFullScript(false);
    runner.setStopOnError(true);
    runner.setErrorLogWriter(null);
    runner.setLogWriter(null);
    runJPetStoreScripts(runner);

    String resource = "org/apache/ibatis/jdbc/CreateStoredProceduresAndFunctions.sql";
    Reader reader = Resources.getResourceAsReader(resource);

    try {
      runner.runScript(reader);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    
    verifyStoredProcedure(conn);
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
    } finally {
      ds.forceCloseAll();
    }
  }
  
  private void verifyStoredProcedure(Connection conn) throws SQLException {

	    SqlRunner stmtRunner = new SqlRunner(conn);
	    List<Map<String, Object>> results = null;
	    
	    results = stmtRunner.selectAll("SELECT * FROM product WHERE productId like 'AV-XB-%'", new Object[0]);
	    assertEquals("Number of expected results doesn't match" ,3 , results.size());
	    
	    stmtRunner.run("CALL delete_product('AV-XB-%');");
	    
	    results = stmtRunner.selectAll("SELECT * FROM product WHERE productId like 'AV-XB-%'", new Object[0]);
	    assertEquals("Error in stored procedure.", 0, results.size());
	    
  }

}
