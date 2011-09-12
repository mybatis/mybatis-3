package org.apache.ibatis.submitted.order_prefix_removed;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OrderPrefixRemoved {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void initDatabase() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:order_prefix_removed", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/order_prefix_removed/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/order_prefix_removed/ibatisConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testOrderPrefixNotRemoved() {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
    try {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);

      Person person = personMapper.select(new String("slow"));

      assertNotNull(person);
      
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
  }
}
