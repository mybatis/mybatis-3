/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.cursor_simple;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.Iterator;

public class CursorSimpleTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        // create a SqlSessionFactory
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cursor_simple/mybatis-config.xml");
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        reader.close();

        // populate in-memory database
        SqlSession session = sqlSessionFactory.openSession();
        Connection conn = session.getConnection();
        reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cursor_simple/CreateDB.sql");
        ScriptRunner runner = new ScriptRunner(conn);
        runner.setLogWriter(null);
        runner.runScript(reader);
        reader.close();
        session.close();
    }

    @Test
    public void shouldGetAllUser() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Mapper mapper = sqlSession.getMapper(Mapper.class);
        Cursor<User> usersCursor = mapper.getAllUsers();
        try {
            Assert.assertFalse(usersCursor.isOpen());

            Iterator<User> iterator = usersCursor.iterator();

            // Check if hasNext, fetching is started
            Assert.assertTrue(iterator.hasNext());
            Assert.assertTrue(usersCursor.isOpen());
            Assert.assertFalse(usersCursor.isConsumed());

            User user = iterator.next();
            Assert.assertEquals("User1", user.getName());
            Assert.assertEquals(0, usersCursor.getCurrentIndex());

            user = iterator.next();
            Assert.assertEquals("User2", user.getName());
            Assert.assertEquals(1, usersCursor.getCurrentIndex());

            user = iterator.next();
            Assert.assertEquals("User3", user.getName());
            Assert.assertEquals(2, usersCursor.getCurrentIndex());

            user = iterator.next();
            Assert.assertEquals("User4", user.getName());
            Assert.assertEquals(3, usersCursor.getCurrentIndex());

            // Check no more elements
            Assert.assertFalse(iterator.hasNext());
            Assert.assertFalse(usersCursor.isOpen());
            Assert.assertTrue(usersCursor.isConsumed());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    public void testCursorClosedOnSessionClose() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        Mapper mapper = sqlSession.getMapper(Mapper.class);
        Cursor<User> usersCursor = mapper.getAllUsers();
        try {
            Assert.assertFalse(usersCursor.isOpen());

            Iterator<User> iterator = usersCursor.iterator();

            // Check if hasNext, fetching is started
            Assert.assertTrue(iterator.hasNext());
            Assert.assertTrue(usersCursor.isOpen());
            Assert.assertFalse(usersCursor.isConsumed());

            // Consume only the first result
            User user = iterator.next();
            Assert.assertEquals("User1", user.getName());

            // Check there is still remaining elements
            Assert.assertTrue(iterator.hasNext());
            Assert.assertTrue(usersCursor.isOpen());
            Assert.assertFalse(usersCursor.isConsumed());
        } finally {
            sqlSession.close();
        }

        // The cursor was not fully consumed, but it should be close since we closed the session
        Assert.assertFalse(usersCursor.isOpen());
        Assert.assertFalse(usersCursor.isConsumed());
    }

    @Test
    public void testCursorWithRowBound() {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            Cursor<User> usersCursor = sqlSession.selectCursor("getAllUsers", null, new RowBounds(1, 2));

            Iterator<User> iterator = usersCursor.iterator();

            User user = iterator.next();
            Assert.assertEquals("User2", user.getName());
            Assert.assertEquals(1, usersCursor.getCurrentIndex());

            user = iterator.next();
            Assert.assertEquals("User3", user.getName());
            Assert.assertEquals(2, usersCursor.getCurrentIndex());

            Assert.assertFalse(iterator.hasNext());
            Assert.assertFalse(usersCursor.isOpen());
            Assert.assertTrue(usersCursor.isConsumed());
        } finally {
            sqlSession.close();
        }
    }
}
