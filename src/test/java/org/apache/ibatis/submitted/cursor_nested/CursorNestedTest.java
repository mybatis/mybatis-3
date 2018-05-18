/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.submitted.cursor_nested;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.util.Iterator;

public class CursorNestedTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        // create a SqlSessionFactory
        try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cursor_nested/mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        }

        // populate in-memory database
        BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
                "org/apache/ibatis/submitted/cursor_nested/CreateDB.sql");
    }

    @Test
    public void shouldGetAllUser() {
        Cursor<User> usersCursor;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            Mapper mapper = sqlSession.getMapper(Mapper.class);
            usersCursor = mapper.getAllUsers();

            Assert.assertFalse(usersCursor.isOpen());
            // Retrieving iterator, fetching is not started
            Iterator<User> iterator = usersCursor.iterator();

            // Check if hasNext, fetching is started
            Assert.assertTrue(iterator.hasNext());
            Assert.assertTrue(usersCursor.isOpen());
            Assert.assertFalse(usersCursor.isConsumed());

            User user = iterator.next();
            Assert.assertEquals(2, user.getGroups().size());
            Assert.assertEquals(3, user.getRoles().size());

            user = iterator.next();
            Assert.assertEquals(1, user.getGroups().size());
            Assert.assertEquals(3, user.getRoles().size());

            user = iterator.next();
            Assert.assertEquals(3, user.getGroups().size());
            Assert.assertEquals(1, user.getRoles().size());

            user = iterator.next();
            Assert.assertEquals(2, user.getGroups().size());
            Assert.assertEquals(2, user.getRoles().size());

            // Check no more elements
            Assert.assertFalse(iterator.hasNext());
            Assert.assertFalse(usersCursor.isOpen());
            Assert.assertTrue(usersCursor.isConsumed());
        }
        Assert.assertFalse(usersCursor.isOpen());
    }

    @Test
    public void testCursorWithRowBound() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            Cursor<User> usersCursor = sqlSession.selectCursor("getAllUsers", null, new RowBounds(2, 1));

            Iterator<User> iterator = usersCursor.iterator();

            Assert.assertTrue(iterator.hasNext());
            User user = iterator.next();
            Assert.assertEquals("User3", user.getName());
            Assert.assertEquals(2, usersCursor.getCurrentIndex());

            Assert.assertFalse(iterator.hasNext());
            Assert.assertFalse(usersCursor.isOpen());
            Assert.assertTrue(usersCursor.isConsumed());
        }
    }
}
