package org.apache.ibatis.adebug;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;

public class Startup {
  
  private Connection conn = null;
  
  @Before
  public void initData() throws Exception {
    Class.forName("org.hsqldb.jdbcDriver");
    conn = DriverManager.getConnection("jdbc:hsqldb:mem:mybatis", "sa", "");
    ScriptRunner scriptRunner = new ScriptRunner(conn);
    scriptRunner.runScript(Resources.getResourceAsReader("org/apache/ibatis/adebug/create-table.sql"));
    scriptRunner.runScript(Resources.getResourceAsReader("org/apache/ibatis/adebug/init-data.sql"));
    
  }
  
  private DataSource getDataSource() {
    UnpooledDataSource dataSource = new UnpooledDataSource();
    dataSource.setDriver("org.hsqldb.jdbcDriver");
    dataSource.setUrl("jdbc:hsqldb:mem:mybatis");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }
  
  @Test
  public void testFromJavaConfig() {
    
    DataSource dataSource = getDataSource();
    TransactionFactory transactionFactory =
      new JdbcTransactionFactory();
    Environment environment =
      new Environment("development", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.getTypeAliasRegistry().registerAlias("user", User.class);
//    configuration.addMappers("org.apache.ibatis.adebug");
    configuration.addMapper(UserMapper.class);
    SqlSessionFactory sqlSessionFactory =
      new SqlSessionFactoryBuilder().build(configuration);
    SqlSession sqlSession = sqlSessionFactory.openSession();
    UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
    List<User> users = userMapper.selectAll();
    users.forEach(System.out::println);
    User user = userMapper.selectByName("张三");
    System.out.println("selectByName: " + user);
  }
  
  // 测试hsqldb
  @Test
  public void test() throws SQLException {
    SqlRunner sqlRunner = new SqlRunner(conn);
    String sql = new SQL() {{
      SELECT("*");
      FROM("user");
    }}.toString();
    List<Map<String, Object>> result = sqlRunner.selectAll(sql);
    result.forEach(System.out::println);
  }
  
  @Test
  public void testMetaObject() {
    User user = new User();
    user.setName("zhangsan");
    MetaObject metaObject = SystemMetaObject.forObject(user);
    System.out.println(metaObject.getValue("name"));
  }
}
