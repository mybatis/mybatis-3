package org.apache.ibatis.submitted.one_parameterprovider;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OneParameterProviderTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void initDatabase() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:one_parameterprovider", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/one_parameterprovider/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/one_parameterprovider/ibatisConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      sqlSessionFactory.getConfiguration().setLazyLoadingEnabled(true);
      sqlSessionFactory.getConfiguration().addMapper(AccountMapper.class);
      sqlSessionFactory.getConfiguration().addMapper(AccountantMapper.class);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testOneParameterProvider() {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
    try {
      AccountMapper accountMapper = sqlSession.getMapper(AccountMapper.class);

      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, 2013);

      Account account = accountMapper.select(cal.getTime(), 1L);

      assertNotNull(account);
      assertNotNull(account.getAccountant());
      assertEquals(new Long(2), account.getAccountant().getId());

      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
  }
}
