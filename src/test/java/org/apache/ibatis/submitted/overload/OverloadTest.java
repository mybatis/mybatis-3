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
package org.apache.ibatis.submitted.overload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class OverloadTest {

    protected static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:overload", "sa", "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/overload/CreateDB.sql");

            final ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/overload/mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testOverloadedMapperMethod() {
        final SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            final OverloadedMapper mapper = sqlSession.getMapper(OverloadedMapper.class);
            final List<String> resultsByLastName = mapper.find("Flintstone");
            final List<String> resultsByNullFirstNameAndLastName = mapper.find(null, "Flintstone");
            assertNotEquals(0, resultsByLastName.size());
            assertEquals(resultsByLastName.size(), resultsByNullFirstNameAndLastName.size());

            final List<String> resultsByNullLastNameAndFirstName = mapper.find("Fred", null);
            assertNotEquals(0, resultsByNullLastNameAndFirstName.size());

        } finally {
            sqlSession.close();
        }
    }
}
