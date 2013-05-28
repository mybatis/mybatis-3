/*
 * Copyright 2009-2013 The MyBatis Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.submitted.cursorlist_nested;

import java.io.Reader;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.executor.resultset.CursorList;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class CursorListNestedTest {

	private static SqlSessionFactory sqlSessionFactory;

	@BeforeClass
	public static void setUp() throws Exception {
		// create a SqlSessionFactory
		Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cursorlist_nested/mybatis-config.xml");
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		reader.close();

		// populate in-memory database
		SqlSession session = sqlSessionFactory.openSession();
		Connection conn = session.getConnection();
		reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cursorlist_nested/CreateDB.sql");
		ScriptRunner runner = new ScriptRunner(conn);
		runner.setLogWriter(null);
		runner.runScript(reader);
		reader.close();
		session.close();
	}

	@Test
	public void shouldGetAllUser() {
		SqlSession sqlSession = sqlSessionFactory.openSession();
		try {
			Mapper mapper = sqlSession.getMapper(Mapper.class);
			List<User> users = mapper.getAllUsers();

			Assert.assertTrue(users instanceof CursorList);

			CursorList<User> cursorList = (CursorList<User>) users;
			Assert.assertFalse(cursorList.isFetchStarted());

			// Retrieving iterator, fetching is not started
			Iterator<User> iterator = users.iterator();
			Assert.assertFalse(cursorList.isFetchStarted());

			// Check if hasNext, fetching is started
			Assert.assertTrue(iterator.hasNext());
			Assert.assertTrue(cursorList.isFetchStarted());
			Assert.assertFalse(cursorList.isResultSetExhausted());

			User user = iterator.next();
			Assert.assertEquals(2, user.getGroups().size());
			Assert.assertEquals(3, user.getRoles().size());

			user = iterator.next();
			Assert.assertEquals(1, user.getGroups().size());
			Assert.assertEquals(3, user.getRoles().size());

			user = iterator.next();
			Assert.assertEquals(3, user.getGroups().size());
			Assert.assertEquals(1, user.getRoles().size());

      // Check no more elements
      Assert.assertTrue(!iterator.hasNext());
			Assert.assertTrue(cursorList.isFetchStarted());
			Assert.assertTrue(cursorList.isResultSetExhausted());

		} finally {
			sqlSession.close();
		}
	}

}
