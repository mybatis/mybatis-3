package org.apache.ibatis.jdbc;

import org.apache.ibatis.BaseDataTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public class SqlRunnerTest extends BaseDataTest {

  @Test
  public void shouldSelectOne() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    SqlRunner exec = new SqlRunner(connection);
    Map row = exec.selectOne("SELECT * FROM PRODUCT WHERE PRODUCTID = ?", "FI-SW-01");
    assertEquals("FI-SW-01", row.get("PRODUCTID"));
  }

  @Test
  public void shouldSelectList() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    SqlRunner exec = new SqlRunner(connection);
    List rows = exec.selectAll("SELECT * FROM PRODUCT");
    assertEquals(16, rows.size());
  }

  @Test
  public void shouldInsert() throws Exception {
    DataSource ds = createUnpooledDataSource(BLOG_PROPERTIES);
    runScript(ds, BLOG_DDL);
    Connection connection = ds.getConnection();
    SqlRunner exec = new SqlRunner(connection);
    exec.setUseGeneratedKeySupport(true);
    int id = exec.insert("INSERT INTO author (username, password, email, bio) VALUES (?,?,?,?)", "someone", "******", "someone@apache.org", Null.LONGVARCHAR);
    Map row = exec.selectOne("SELECT * FROM author WHERE username = ?", "someone");
    connection.rollback();
    assertTrue(SqlRunner.NO_GENERATED_KEY != id);
    assertEquals("someone", row.get("USERNAME"));
  }

  @Test
  public void shouldUpdateCategory() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    SqlRunner exec = new SqlRunner(connection);
    int count = exec.update("update product set category = ? where productid = ?", "DOGS", "FI-SW-01");
    Map row = exec.selectOne("SELECT * FROM PRODUCT WHERE PRODUCTID = ?", "FI-SW-01");
    assertEquals("DOGS", row.get("CATEGORY"));
    assertEquals(1, count);
  }

  @Test
  public void shouldDeleteOne() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    runScript(ds, JPETSTORE_DDL);
    runScript(ds, JPETSTORE_DATA);
    Connection connection = ds.getConnection();
    SqlRunner exec = new SqlRunner(connection);
    int count = exec.delete("delete from item");
    List rows = exec.selectAll("SELECT * FROM ITEM");
    assertEquals(28, count);
    assertEquals(0, rows.size());
  }

  @Test
  public void shouldDemonstrateDDLThroughRunMethod() throws Exception {
    DataSource ds = createUnpooledDataSource(JPETSTORE_PROPERTIES);
    Connection connection = ds.getConnection();
    SqlRunner exec = new SqlRunner(connection);
    exec.run("CREATE TABLE BLAH(ID INTEGER)");
    exec.run("insert into BLAH values (1)");
    List rows = exec.selectAll("SELECT * FROM BLAH");
    exec.run("DROP TABLE BLAH");
    assertEquals(1, rows.size());
  }


}
