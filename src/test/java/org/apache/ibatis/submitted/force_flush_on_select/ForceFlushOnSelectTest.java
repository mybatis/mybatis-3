package org.apache.ibatis.submitted.force_flush_on_select;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ForceFlushOnSelectTest {
    
    private static SqlSessionFactory sqlSessionFactory;
    
    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:force_flush_on_select", "sa", "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/force_flush_on_select/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/force_flush_on_select/ibatisConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    @Test
    public void testShouldFlushLocalSessionCacheOnQuery() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
        try {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            Person person = personMapper.selectById(1);
            person.setFirstName("Simone");
            
            personMapper.update(person);

            Person updatedPerson = personMapper.selectById(1);

            assertEquals("Simone", updatedPerson.getFirstName());

            sqlSession.commit();
        } finally {
        	sqlSession.close();
        }
    }

    @Test
    public void testShouldFlushLocalSessionCacheOnQueryForList() {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
        try {
            PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
            List<Person> people = personMapper.selectAll();

            Person person = people.get(0);
            person.setFirstName("Simone");

            personMapper.update(person);

            people = personMapper.selectAll();

            assertEquals("Simone", people.get(0).getFirstName());

            sqlSession.commit();
        } finally {
        	sqlSession.close();
        }
    }

}
