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
package org.apache.ibatis.submitted.insert_with_Void;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/*
 * @author johhud1
 */
public class InsertWithVoidReturnTypeTest {

    private final static String SQL_MAP_CONFIG = "org/apache/ibatis/submitted/insert_with_Void/sqlmap.xml";
    private static SqlSession session;
    private static Connection conn;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        final SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(SQL_MAP_CONFIG));
        session = factory.openSession();
        conn = session.getConnection();

        BaseDataTest.runScript(factory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/insert_with_Void/create-schema-mysql.sql");
    }

    @Before
    public void setUp() {
        final FooMapper mapper = session.getMapper(FooMapper.class);
        mapper.deleteAllFoo();
        session.commit();
    }

    @Test
    public void testInsertWithVoidResturnType() {
        final FooMapper mapper = session.getMapper(FooMapper.class);
        final Foo inserted = new Foo(1L, 3);
        mapper.insertFoo(inserted);

        final Foo selected = mapper.selectFoo();

        Assert.assertEquals(inserted.getField1(), selected.getField1());
        Assert.assertEquals(inserted.getField2(), selected.getField2());

    }

    @AfterClass
    public static void tearDownAfterClass() {
        try {
            conn.close();
        } catch (SQLException e) {
            Assert.fail(e.getMessage());
        }
        session.close();
    }

}
