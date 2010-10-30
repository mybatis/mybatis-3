package org.apache.ibatis.submitted.selectkey;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class SelectKeyTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:lname", "sa",
          "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/selectkey/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/selectkey/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testSelectKey() throws Exception {
    // this test checks to make sure that we can have select keys with the same
    // insert id in different namespaces
    String resource = "org/apache/ibatis/submitted/selectkey/MapperConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(reader);
    assertNotNull(sqlMapper);
  }

  @Test
  public void testInsertTable1() {
    SqlSession sqlSession = sqlSessionFactory.openSession();

    try {
      Map parms = new HashMap();
      parms.put("name", "Fred");
      int rows = sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table1.insert", parms);
      assertEquals(1, rows);
      assertEquals(11, parms.get("id"));

    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testInsertTable2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();

    try {
      Map parms = new HashMap();
      parms.put("name", "Fred");
      int rows = sqlSession.insert("org.apache.ibatis.submitted.selectkey.Table2.insert", parms);
      assertEquals(1, rows);
      assertEquals(22, parms.get("id"));

    } finally {
      sqlSession.close();
    }
  }

}
