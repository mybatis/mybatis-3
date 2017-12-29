/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.custom_collection_handling;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

public class CustomCollectionHandlingTest {

    /**
     * Custom collections with nested resultMap.
     *
     * @throws Exception
     */
    @Test
    public void testSelectListWithNestedResultMap() throws Exception {
        String xmlConfig = "org/apache/ibatis/submitted/custom_collection_handling/MapperConfig.xml";
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            List<Person> list = sqlSession.selectList("org.apache.ibatis.submitted.custom_collection_handling.PersonMapper.findWithResultMap");
            assertEquals(2, list.size());
            assertEquals(2, list.get(0).getContacts().size());
            assertEquals(1, list.get(1).getContacts().size());
            assertEquals("3 Wall Street", list.get(0).getContacts().get(1).getAddress());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * Custom collections with nested select.
     *
     * @throws Exception
     */
    @Test
    public void testSelectListWithNestedSelect() throws Exception {
        String xmlConfig = "org/apache/ibatis/submitted/custom_collection_handling/MapperConfig.xml";
        SqlSessionFactory sqlSessionFactory = getSqlSessionFactoryXmlConfig(xmlConfig);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            List<Person> list = sqlSession.selectList("org.apache.ibatis.submitted.custom_collection_handling.PersonMapper.findWithSelect");
            assertEquals(2, list.size());
            assertEquals(2, list.get(0).getContacts().size());
            assertEquals(1, list.get(1).getContacts().size());
            assertEquals("3 Wall Street", list.get(0).getContacts().get(1).getAddress());
        } 
        finally {
            sqlSession.close();
        }
    }

    private SqlSessionFactory getSqlSessionFactoryXmlConfig(String resource) throws Exception {
        Reader configReader = Resources.getResourceAsReader(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
        configReader.close();

        Connection conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
        initDb(conn);
        conn.close();

        return sqlSessionFactory;
    }

    private static void initDb(Connection conn) throws IOException, SQLException {
        try {
            Reader scriptReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/custom_collection_handling/CreateDB.sql");
            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(new PrintWriter(System.err));
            runner.runScript(scriptReader);
            conn.commit();
            scriptReader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
}
