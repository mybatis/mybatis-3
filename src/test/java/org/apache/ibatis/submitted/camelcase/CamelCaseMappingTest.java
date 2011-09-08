package org.apache.ibatis.submitted.camelcase;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CamelCaseMappingTest {

	protected static SqlSessionFactory sqlSessionFactory;

	@BeforeClass
	public static void setUp() throws Exception {
		Connection conn = null;

		try {
			Class.forName("org.hsqldb.jdbcDriver");
			conn = DriverManager.getConnection("jdbc:hsqldb:mem:gname", "sa", "");
			Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/camelcase/CreateDB.sql");
			ScriptRunner runner = new ScriptRunner(conn);
			runner.setLogWriter(null);
			runner.setErrorLogWriter(null);
			runner.runScript(reader);
			conn.commit();
			reader.close();

			reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/camelcase/MapperConfig.xml");
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			reader.close();

		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testList() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			List<Camel> list = sqlSession.selectList("org.apache.ibatis.submitted.camel.doSelect");
			Assert.assertTrue(list.size() > 0);
			Assert.assertNotNull(list.get(0).getFirstName());
			Assert.assertNull(list.get(0).getLAST_NAME());
		} finally {
			sqlSession.close();
		}
	}

  @SuppressWarnings("unchecked")
  @Test
  public void testMap() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List list = sqlSession.selectList("org.apache.ibatis.submitted.camel.doSelectMap");
      Assert.assertTrue(list.size() > 0);
      Assert.assertTrue(Map.class.cast(list.get(0)).containsKey("LAST_NAME"));
    } finally {
      sqlSession.close();
    }
  }
	
}
