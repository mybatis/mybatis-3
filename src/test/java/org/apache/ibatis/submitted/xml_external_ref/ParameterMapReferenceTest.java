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

public class ParameterMapReferenceTest {

  @Test
  public void testCrossReferenceXmlConfig() throws Exception {
    testCrossReference(getSqlSessionFactoryXmlConfig());
  }

  @Test
  public void testCrossReferenceJavaConfig() throws Exception {
    testCrossReference(getSqlSessionFactoryJavaConfig());
  }

  private void testCrossReference(SqlSessionFactory sqlSessionFactory) throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      ParameterMapReferencePersonMapper personMapper = sqlSession.getMapper(ParameterMapReferencePersonMapper.class);
      Person parameter = new Person();
      parameter.setId(1);
      Person person = personMapper.select(parameter);
      assertEquals((Integer)1, person.getId());

    } finally {
      sqlSession.close();
    }
  }

  private SqlSessionFactory getSqlSessionFactoryXmlConfig() throws Exception {
    Reader configReader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/xml_external_ref/ParameterMapReferenceMapperConfig.xml");
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configReader);
    configReader.close();

    Connection conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
    initDb(conn);
    conn.close();

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

    configuration.addMapper(ParameterMapReferencePersonMapper.class);
    configuration.addMapper(ParameterMapReferencePetMapper.class);
    c.close();

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
