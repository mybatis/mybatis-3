package org.apache.ibatis.submitted.sptests;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class SPTest {
    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:sptest", "sa",
                    "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/sptests/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setDelimiter("]");
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/sptests/MapperConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    /**
     * This test shows how to use input and output parameters
     * in a stored procedure.
     * This procedure does not return a result set.
     * 
     * This test shows using a multi-property parameter.
     */
    @Test
    public void testAdderAsSelect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Parameter parameter = new Parameter();
            parameter.setAddend1(2);
            parameter.setAddend2(3);
            
            SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
            spMapper.adder(parameter);
            
            assertEquals((Integer) 5, parameter.getSum());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * This test shows how to use input and output parameters
     * in a stored procedure.
     * This procedure does not return a result set.
     * 
     * Currently this test will fail because of a MyBatis cache issue.
     * 
     * This test shows using a multi-property parameter.
     */
    @Test
    @Ignore("until MyBatis cache issue fixed")
    public void testAdderAsSelectDoubleCall1() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Parameter parameter = new Parameter();
            parameter.setAddend1(2);
            parameter.setAddend2(3);
            
            SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
            
            spMapper.adder(parameter);
            assertEquals((Integer) 5, parameter.getSum());
            
            // this fails because there are two calls to the same SP with the same
            // parms in the same session.
            parameter = new Parameter();
            parameter.setAddend1(2);
            parameter.setAddend2(3);
            spMapper.adder(parameter);
            assertEquals((Integer) 5, parameter.getSum());
            
        } finally {
            sqlSession.close();
        }
    }

    /**
     * This test shows how to use input and output parameters
     * in a stored procedure.
     * This procedure does not return a result set.
     * 
     * This test shows using a multi-property parameter.
     * 
     * This test shows that the cache problem does not manifest if
     * the input parameters are different.
     */
    @Test
    public void testAdderAsSelectDoubleCall2() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Parameter parameter = new Parameter();
            parameter.setAddend1(2);
            parameter.setAddend2(3);
            
            SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
            
            spMapper.adder(parameter);
            assertEquals((Integer) 5, parameter.getSum());
            
            parameter = new Parameter();
            parameter.setAddend1(4);
            parameter.setAddend2(5);
            spMapper.adder(parameter);
            assertEquals((Integer) 9, parameter.getSum());
            
        } finally {
            sqlSession.close();
        }
    }

    /**
     * This test shows that the cache problem is not in effect if
     * stored procedures with out parms are defined as <update> rather
     * then <select>.  Of course, this only works if you are not returning
     * a result set.
     * 
     * This test shows using a multi-property parameter.
     */
    @Test
    public void testAdderAsUpdate() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Parameter parameter = new Parameter();
            parameter.setAddend1(2);
            parameter.setAddend2(3);
            
            SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
            
            spMapper.adder2(parameter);
            assertEquals((Integer) 5, parameter.getSum());
            
            parameter = new Parameter();
            parameter.setAddend1(2);
            parameter.setAddend2(3);
            spMapper.adder2(parameter);
            assertEquals((Integer) 5, parameter.getSum());
            
        } finally {
            sqlSession.close();
        }
    }
    
    /**
     * This test shows how to use an input parameter and return a result
     * set from a stored procedure.
     * 
     * This test shows using a single value parameter.
     */
    @Test
    @Ignore("until hsqldb 2.0.1 is released")
    public void testCallWithResultSet1() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
            
            Name name = spMapper.getName(1);
            assertNotNull(name);
            assertEquals("Wilma", name.getFirstName());
        } finally {
            sqlSession.close();
        }
    }

    /**
     * This test shows how to use a input and output parameters and
     * return a result set from a stored procedure.
     * 
     * This test shows using a single value parameter.
     */
    @Test
    @Ignore("until hsqldb 2.0.1 is released")
    public void testCallWithResultSet2() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
            
            Map<String, Object> parms = new HashMap<String, Object>();
            parms.put("lowestId", 1);
            List<Name> names = spMapper.getNames(parms);
            assertEquals(3, names.size());
            assertEquals(3, parms.get("totalRows"));
        } finally {
            sqlSession.close();
        }
    }

    /**
     * This test shows how to use a input and output parameters and
     * return a result set from a stored procedure.
     * 
     * This test shows using a Map parameter.
     * 
     * This test shows that the cache problem does not manifest if
     * the input parameters are different.
     */
    @Test
    @Ignore("until hsqldb 2.0.1 is released")
    public void testCallWithResultSet3() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
            
            Map<String, Object> parms = new HashMap<String, Object>();
            parms.put("lowestId", 2);
            List<Name> names = spMapper.getNames(parms);
            assertEquals(2, parms.get("totalRows"));
            assertEquals(2, names.size());
            
            parms = new HashMap<String, Object>();
            parms.put("lowestId", 3);
            names = spMapper.getNames(parms);
            assertEquals(1, names.size());
            assertEquals(1, parms.get("totalRows"));
        } finally {
            sqlSession.close();
        }
    }

    /**
     * This test shows how to use a input and output parameters and
     * return a result set from a stored procedure.
     * 
     * This test shows using a Map parameter.
     * 
     * The cache problem is in effect with this test.
     */
    @Test
    @Ignore("until hsqldb 2.0.1 is released, and MyBatis cache issue fixed")
    public void testCallWithResultSet4() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            SPMapper spMapper = sqlSession.getMapper(SPMapper.class);
            
            Map<String, Object> parms = new HashMap<String, Object>();
            parms.put("lowestId", 2);
            List<Name> names = spMapper.getNames(parms);
            assertEquals(2, parms.get("totalRows"));
            assertEquals(2, names.size());
            
            parms = new HashMap<String, Object>();
            parms.put("lowestId", 2);
            names = spMapper.getNames(parms);
            assertEquals(2, names.size());
            // fails because of cache problem
            assertEquals(2, parms.get("totalRows"));
        } finally {
            sqlSession.close();
        }
    }
}
