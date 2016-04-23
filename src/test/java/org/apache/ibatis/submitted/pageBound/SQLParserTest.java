package org.apache.ibatis.submitted.pageBound;


import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.Dialect.Db2Dialect;
import org.apache.ibatis.mapping.Dialect.MysqlPageDialect;
import org.apache.ibatis.mapping.Dialect.OraclePageDialect;
import org.apache.ibatis.mapping.Dialect.SqlServer2012Dialect;
import org.apache.ibatis.mapping.Dialect.PostgreSQLDialect;
import org.apache.ibatis.session.PageBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLParserTest {

	  private static SqlSessionFactory sqlSessionFactory;

	  @BeforeClass
	  public static void setUp() throws Exception {
	    // create a SqlSessionFactory
	    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/pageBound/mybatis-config.xml");
	    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
	    reader.close();

	    // populate in-memory database
	    SqlSession session = sqlSessionFactory.openSession();
	    Connection conn = session.getConnection();
	    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/pageBound/CreateDB.sql");
	    ScriptRunner runner = new ScriptRunner(conn);
	    runner.setLogWriter(null);
	    runner.runScript(reader);
	    reader.close();
	    session.close();
	  }
	
	@Test
    public void mysqlParser() {
		SqlSession session = sqlSessionFactory.openSession();
		String sql = "select id, name, age from user where age=? order by id";
		BoundSql boundSql = new BoundSql(session.getConfiguration(), sql, null, null);
		PageBounds pageBounds = new PageBounds(1,3);
		MappedStatement mappedStatement = session.getConfiguration().getMappedStatement("org.apache.ibatis.submitted.pageBound.UserMapper.selectAll");
		MysqlPageDialect dialect = new MysqlPageDialect(mappedStatement, boundSql, pageBounds);
		System.out.println("================mysql=================");
		System.out.println(dialect.bulidListSql());
		System.out.println(dialect.bulidCountSql());
    }
	
    @Test
    public void oracleParser() {
		SqlSession session = sqlSessionFactory.openSession();
		String sql = "select id, name, age from user where age=? order by id";
		BoundSql boundSql = new BoundSql(session.getConfiguration(), sql, null, null);
		PageBounds pageBounds = new PageBounds(1,3);
		MappedStatement mappedStatement = session.getConfiguration().getMappedStatement("org.apache.ibatis.submitted.pageBound.UserMapper.selectAll");
		OraclePageDialect dialect = new OraclePageDialect(mappedStatement, boundSql, pageBounds);
		System.out.println("================oracle=================");
		System.out.println(dialect.bulidListSql());
		System.out.println(dialect.bulidCountSql());
    }
	
	@Test
    public void sqlServer2012Parser() {
		SqlSession session = sqlSessionFactory.openSession();
		String sql = "select id, name, age from user where age=? order by id";
		BoundSql boundSql = new BoundSql(session.getConfiguration(), sql, null, null);
		PageBounds pageBounds = new PageBounds(1,3);
		MappedStatement mappedStatement = session.getConfiguration().getMappedStatement("org.apache.ibatis.submitted.pageBound.UserMapper.selectAll");
		SqlServer2012Dialect dialect = new SqlServer2012Dialect(mappedStatement, boundSql, pageBounds);
		System.out.println("================sqlServer2012=================");
		System.out.println(dialect.bulidListSql());
		System.out.println(dialect.bulidCountSql());
    }
	
	@Test
    public void db2Parser() {
		SqlSession session = sqlSessionFactory.openSession();
		String sql = "select id, name, age from user where age=? order by id";
		BoundSql boundSql = new BoundSql(session.getConfiguration(), sql, null, null);
		PageBounds pageBounds = new PageBounds(1,3);
		MappedStatement mappedStatement = session.getConfiguration().getMappedStatement("org.apache.ibatis.submitted.pageBound.UserMapper.selectAll");
		Db2Dialect dialect = new Db2Dialect(mappedStatement, boundSql, pageBounds);
		System.out.println("================db2=================");
		System.out.println(dialect.bulidListSql());
		System.out.println(dialect.bulidCountSql());
    }
	
	@Test
    public void postgreSQLParser() {
		SqlSession session = sqlSessionFactory.openSession();
		String sql = "select id, name, age from user where age=? order by id";
		BoundSql boundSql = new BoundSql(session.getConfiguration(), sql, null, null);
		PageBounds pageBounds = new PageBounds(1,3);
		MappedStatement mappedStatement = session.getConfiguration().getMappedStatement("org.apache.ibatis.submitted.pageBound.UserMapper.selectAll");
		PostgreSQLDialect dialect = new PostgreSQLDialect(mappedStatement, boundSql, pageBounds);
		System.out.println("================postgreSQL=================");
		System.out.println(dialect.bulidListSql());
		System.out.println(dialect.bulidCountSql());
    }
}
