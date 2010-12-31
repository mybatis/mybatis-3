package org.apache.ibatis.submitted.encoding;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class EncodingTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:gname", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/encoding/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/encoding/EncodingConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

//  @Test
//  public void testEncoding1() {
//    SqlSession sqlSession = sqlSessionFactory.openSession();
//    try {
//      EncodingMapper mapper = sqlSession.getMapper(EncodingMapper.class);
//      String answer = mapper.select1();
//      assertEquals("Mara��n", answer);
//    } finally {
//      sqlSession.close();
//    }
//  }

  @Test
  public void testEncoding2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      EncodingMapper mapper = sqlSession.getMapper(EncodingMapper.class);
      String answer = mapper.select2();
      assertEquals("Marañón", answer);
    } finally {
      sqlSession.close();
    }
  }
}
