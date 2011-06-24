package org.apache.ibatis.submitted.deferload_common_property;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class CommonPropertyDeferLoadError {
    
    private static SqlSessionFactory sqlSessionFactory;
    private static SqlSessionFactory lazyLoadSqlSessionFactory;
    
    @BeforeClass
    public static void initDatabase() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:deferload_common_property", "sa",
                    "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/deferload_common_property/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/deferload_common_property/ibatisConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/deferload_common_property/lazyLoadIbatisConfig.xml");
            lazyLoadSqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    @Test
    public void testDeferLoadAfterResultHandler() {
    	SqlSession sqlSession = sqlSessionFactory.openSession();
    	try {
    		class MyResultHandler implements ResultHandler {
    			List<Child> children = new ArrayList<Child>();
				public void handleResult(ResultContext context) {
					Child child = (Child)context.getResultObject();
					children.add(child);
				}
    		};
    		MyResultHandler myResultHandler = new MyResultHandler();
    		sqlSession.select("org.apache.ibatis.submitted.deferload_common_property.ChildMapper.selectAll", myResultHandler);
    		for (Child child: myResultHandler.children) {
				assertNotNull(child.getFather());
    		}
    	} finally {
    		sqlSession.close();
    	}
    }
    @Test
    public void testDeferLoadDuringResultHandler() {
    	SqlSession sqlSession = sqlSessionFactory.openSession();
    	try {
    		class MyResultHandler implements ResultHandler {
				public void handleResult(ResultContext context) {
					Child child = (Child)context.getResultObject();
					assertNotNull(child.getFather());
				}
    		};
    		sqlSession.select("org.apache.ibatis.submitted.deferload_common_property.ChildMapper.selectAll", new MyResultHandler());
    	} finally {
    		sqlSession.close();
    	}
    }
    @Test
    public void testDeferLoadAfterResultHandlerWithLazyLoad() {
    	SqlSession sqlSession = lazyLoadSqlSessionFactory.openSession();
    	try {
    		class MyResultHandler implements ResultHandler {
    			List<Child> children = new ArrayList<Child>();
				public void handleResult(ResultContext context) {
					Child child = (Child)context.getResultObject();
					children.add(child);
				}
    		};
    		MyResultHandler myResultHandler = new MyResultHandler();
    		sqlSession.select("org.apache.ibatis.submitted.deferload_common_property.ChildMapper.selectAll", myResultHandler);
    		for (Child child: myResultHandler.children) {
				assertNotNull(child.getFather());
    		}
    	} finally {
    		sqlSession.close();
    	}
    }
    @Test
    public void testDeferLoadDuringResultHandlerWithLazyLoad() {
    	SqlSession sqlSession = lazyLoadSqlSessionFactory.openSession();
    	try {
    		class MyResultHandler implements ResultHandler {
				public void handleResult(ResultContext context) {
					Child child = (Child)context.getResultObject();
					assertNotNull(child.getFather());
				}
    		};
    		sqlSession.select("org.apache.ibatis.submitted.deferload_common_property.ChildMapper.selectAll", new MyResultHandler());
    	} finally {
    		sqlSession.close();
    	}
    }
}
