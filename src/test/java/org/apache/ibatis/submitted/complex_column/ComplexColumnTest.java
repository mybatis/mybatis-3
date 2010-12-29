package org.apache.ibatis.submitted.complex_column;

import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.Assert;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class ComplexColumnTest {
    
    private static SqlSessionFactory sqlSessionFactory;
    
    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:complex_column", "sa",
                    "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/complex_column/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/complex_column/ibatisConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    @Test
    public void testWithoutComplex() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        Person person = personMapper.getWithoutComplex(2l);
        Assert.assertNotNull("person must not be null", person);
        Assert.assertEquals("Christian", person.getFirstName());
        Assert.assertEquals("Poitras", person.getLastName());
        Person parent = person.getParent();
        Assert.assertNotNull("parent must not be null", parent);
        Assert.assertEquals("John", parent.getFirstName());
        Assert.assertEquals("Smith", parent.getLastName());
      sqlSession.close();
    }
    
    @Test
    public void testWithComplex() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        Person person = personMapper.getWithComplex(2l);
        Assert.assertNotNull("person must not be null", person);
        Assert.assertEquals("Christian", person.getFirstName());
        Assert.assertEquals("Poitras", person.getLastName());
        Person parent = person.getParent();
        Assert.assertNotNull("parent must not be null", parent);
        Assert.assertEquals("John", parent.getFirstName());
        Assert.assertEquals("Smith", parent.getLastName());
      sqlSession.close();

    }

    @Test
    public void testWithComplex2() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        Person person = personMapper.getWithComplex2(2l);
        Assert.assertNotNull("person must not be null", person);
        Assert.assertEquals("Christian", person.getFirstName());
        Assert.assertEquals("Poitras", person.getLastName());
        Person parent = person.getParent();
        Assert.assertNotNull("parent must not be null", parent);
        Assert.assertEquals("John", parent.getFirstName());
        Assert.assertEquals("Smith", parent.getLastName());
      sqlSession.close();

    }

    @Test
    public void testWithComplex3() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        Person person = personMapper.getWithComplex3(2l);
        Assert.assertNotNull("person must not be null", person);
        Assert.assertEquals("Christian", person.getFirstName());
        Assert.assertEquals("Poitras", person.getLastName());
        Person parent = person.getParent();
        Assert.assertNotNull("parent must not be null", parent);
        Assert.assertEquals("John", parent.getFirstName());
        Assert.assertEquals("Smith", parent.getLastName());
      sqlSession.close();

    }
}
