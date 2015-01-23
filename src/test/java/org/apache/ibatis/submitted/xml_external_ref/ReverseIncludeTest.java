/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.submitted.xml_external_ref;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.Test;

public class ReverseIncludeTest {

  @Test
  public void testReverseIncludeXmlConfig() throws Exception {
    testReverseIncludes(getSqlSessionFactoryXmlConfig());
  }

  @Test
  public void testReverseIncludeJavaConfig() throws Exception {
    testReverseIncludes(getSqlSessionFactoryJavaConfig());
  }

  private void testReverseIncludes(SqlSessionFactory sqlSessionFactory) throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      ReverseIncludePersonMapper personMapper = sqlSession.getMapper(ReverseIncludePersonMapper.class);
      Person person = personMapper.select(1);
      assertEquals((Integer)1, person.getId());
      assertEquals("John", person.getName());

    } finally {
      sqlSession.close();
    }
  }

  private SqlSessionFactory getSqlSessionFactoryXmlConfig() throws Exception {
    Reader configReader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/xml_external_ref/ReverseIncludeMapperConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
    configReader.close();

    Connection conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
    initDb(conn);

    return sqlSessionFactory;
  }

  private SqlSessionFactory getSqlSessionFactoryJavaConfig() throws Exception {
    Class.forName("org.hsqldb.jdbcDriver");
    Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:xmlextref", "sa", "");
    initDb(c);

    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(), new UnpooledDataSource(
        "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:xmlextref", null));
    configuration.setEnvironment(environment);

    configuration.addMapper(ReverseIncludePersonMapper.class);

    return new SqlSessionFactoryBuilder().build(configuration);
  }

  private static void initDb(Connection conn) throws IOException, SQLException {
    try {
      Reader scriptReader = Resources.getResourceAsReader("org/apache/ibatis/submitted/xml_external_ref/CreateDB.sql");
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
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
