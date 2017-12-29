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
package org.apache.ibatis.submitted.lazy_immutable;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public final class ImmutablePOJOTest {

    private static final Integer POJO_ID = 1;
    private static SqlSessionFactory factory;

    @BeforeClass
    public static void setupClass() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:lazy_immutable", "sa", "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/lazy_immutable/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(new PrintWriter(System.err));
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/lazy_immutable/ibatisConfig.xml");
            factory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    public void testLoadLazyImmutablePOJO() {
        final SqlSession session = factory.openSession();
        try {
            final ImmutablePOJOMapper mapper = session.getMapper(ImmutablePOJOMapper.class);
            final ImmutablePOJO pojo = mapper.getImmutablePOJO(POJO_ID);

            assertEquals(POJO_ID, pojo.getId());
            assertNotNull("Description should not be null.", pojo.getDescription());
            assertFalse("Description should not be empty.", pojo.getDescription().length() == 0);
        } finally {
            session.close();
        }
    }

}
