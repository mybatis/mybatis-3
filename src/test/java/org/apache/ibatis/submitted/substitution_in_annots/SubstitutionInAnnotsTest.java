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
package org.apache.ibatis.submitted.substitution_in_annots;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.BeforeClass;
import org.junit.Test;


public class SubstitutionInAnnotsTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Class.forName("org.hsqldb.jdbcDriver");
   	Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:annots", "sa", "");
   	Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/substitution_in_annots/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(c);
    runner.setLogWriter(null);
    runner.setErrorLogWriter(null);
    runner.runScript(reader);
    c.commit();
    reader.close();

    Configuration configuration = new Configuration();
    Environment environment = new Environment("test", new JdbcTransactionFactory(), new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:annots", null));
    configuration.setEnvironment(environment);
    
    configuration.addMapper(SubstitutionInAnnotsMapper.class);
    
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Test
  public void testSubstitutionWithXml() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SubstitutionInAnnotsMapper mapper = sqlSession.getMapper(SubstitutionInAnnotsMapper.class);
      assertEquals("Barney", mapper.getPersonNameByIdWithXml(4));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSubstitutionWithAnnotsValue() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SubstitutionInAnnotsMapper mapper = sqlSession.getMapper(SubstitutionInAnnotsMapper.class);
      assertEquals("Barney", mapper.getPersonNameByIdWithAnnotsValue(4));
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void testSubstitutionWithAnnotsParameter() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SubstitutionInAnnotsMapper mapper = sqlSession.getMapper(SubstitutionInAnnotsMapper.class);
      assertEquals("Barney", mapper.getPersonNameByIdWithAnnotsParameter(4));
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSubstitutionWithAnnotsParamAnnot() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      SubstitutionInAnnotsMapper mapper = sqlSession.getMapper(SubstitutionInAnnotsMapper.class);
      assertEquals("Barney", mapper.getPersonNameByIdWithAnnotsParamAnnot(4));
    } finally {
      sqlSession.close();
    }
  }

}
