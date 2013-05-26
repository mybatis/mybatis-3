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

  @Test
  public void testMap() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      List<Map<String, Object>> list = sqlSession.selectList("org.apache.ibatis.submitted.camel.doSelectMap");
      Assert.assertTrue(list.size() > 0);
      Assert.assertTrue(list.get(0).containsKey("LAST_NAME"));
    } finally {
      sqlSession.close();
    }
  }
	
}
