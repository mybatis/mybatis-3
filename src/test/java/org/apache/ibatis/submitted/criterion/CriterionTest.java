package org.apache.ibatis.submitted.criterion;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

public class CriterionTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:aname", "sa",
          "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/criterion/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/criterion/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testSimpleSelect() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Criterion criterion = new Criterion();
      criterion.setTest("firstName =");
      criterion.setValue("Fred");
      Parameter parameter = new Parameter();
      parameter.setCriterion(criterion);

      List<Map<String, Object>> answer =
          sqlSession.selectList("org.apache.ibatis.submitted.criterion.simpleSelect", parameter);

      assertEquals(1, answer.size());
    } finally {
      sqlSession.close();
    }
  }
}
