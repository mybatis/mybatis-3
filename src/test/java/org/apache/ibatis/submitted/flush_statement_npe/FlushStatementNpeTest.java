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
package org.apache.ibatis.submitted.flush_statement_npe;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class FlushStatementNpeTest {
    
    private static SqlSessionFactory sqlSessionFactory;
    
    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:flush_statement_npe", "sa",
                    "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/flush_statement_npe/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/flush_statement_npe/ibatisConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    @Test
    public void testSameUpdateAfterCommitSimple() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
        try {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.selectById(1);
            person.setFirstName("Simone");
            
            // Execute first update then commit.
            personMapper.update(person);
            sqlSession.commit();
            
            // Execute same update a second time. This used to raise an NPE.
            personMapper.update(person);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    @Test
    public void testSameUpdateAfterCommitReuse() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.REUSE);
        try {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.selectById(1);
            person.setFirstName("Simone");
            
            // Execute first update then commit.
            personMapper.update(person);
            sqlSession.commit();
            
            // Execute same update a second time. This used to raise an NPE.
            personMapper.update(person);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
    @Test
    public void testSameUpdateAfterCommitBatch() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        try {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.selectById(1);
            person.setFirstName("Simone");
            
            // Execute first update then commit.
            personMapper.update(person);
            sqlSession.commit();
            
            // Execute same update a second time. This used to raise an NPE.
            personMapper.update(person);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }
}
