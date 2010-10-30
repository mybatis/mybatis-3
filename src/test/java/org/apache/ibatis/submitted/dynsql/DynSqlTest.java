package org.apache.ibatis.submitted.dynsql;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynSqlTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;
    try {
      Reader configReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/dynsql/MapperConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
      configReader.close();
      conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();

      Reader scriptReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/dynsql/CreateDB.sql");
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(scriptReader);
      conn.commit();
      scriptReader.close();

    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testSelect() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Integer> ids = new ArrayList<Integer>();
      ids.add(1);
      ids.add(3);
      ids.add(5);
      Parameter parameter = new Parameter();
      parameter.setEnabled(true);
      parameter.setSchema("ibtest");
      parameter.setIds(ids);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.select", parameter);

      assertTrue(answer.size() == 3);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSelectSimple() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Integer> ids = new ArrayList<Integer>();
      ids.add(1);
      ids.add(3);
      ids.add(5);
      Parameter parameter = new Parameter();
      parameter.setEnabled(true);
      parameter.setSchema("ibtest");
      parameter.setIds(ids);

      List<Map<String, Object>> answer = sqlSession.selectList("org.apache.ibatis.submitted.dynsql.select_simple", parameter);

      assertTrue(answer.size() == 3);
    } finally {
      sqlSession.close();
    }
  }
}
