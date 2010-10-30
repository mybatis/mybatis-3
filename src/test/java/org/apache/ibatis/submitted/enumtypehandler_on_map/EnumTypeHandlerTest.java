package org.apache.ibatis.submitted.enumtypehandler_on_map;

import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.submitted.enumtypehandler_on_map.Person.Type;
import org.apache.ibatis.submitted.enumtypehandler_on_map.PersonMapper.TypeName;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class EnumTypeHandlerTest {
    
    private static SqlSessionFactory sqlSessionFactory;
    
    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:enumtypehandler_on_map", "sa",
                    "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/enumtypehandler_on_map/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/enumtypehandler_on_map/ibatisConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    @Test
    public void testEnumWithParam() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        List<Person> persons = personMapper.getByType(Person.Type.PERSON, "");
        Assert.assertNotNull("Persons must not be null", persons);
        Assert.assertEquals("Persons must contain exactly 1 person", 1, persons.size());
      sqlSession.close();
    }
    @Test
    public void testEnumWithoutParam() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
        List<Person> persons = personMapper.getByTypeNoParam(new TypeName() {
            public String getName() {
                return "";
            }
            public Type getType() {
                return Person.Type.PERSON;
            }
        });
        Assert.assertNotNull("Persons must not be null", persons);
        Assert.assertEquals("Persons must contain exactly 1 person", 1, persons.size());
      sqlSession.close();
    }
}
