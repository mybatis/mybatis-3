/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.multidb;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiDbTest {

  protected static SqlSessionFactory sqlSessionFactory;
  protected static SqlSessionFactory sqlSessionFactory2;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:multidb", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multidb/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/multidb/MultiDbConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void shouldExecuteHsqlQuery() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      String answer = mapper.select1(1);
      assertEquals("hsql", answer);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldExecuteCommonQuery() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      String answer = mapper.select2(1);
      assertEquals("common", answer);
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void shouldExecuteHsqlQueryWithDynamicIf() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      String answer = mapper.select3(1);
      assertEquals("hsql", answer);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldExecuteHsqlQueryWithInclude() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      String answer = mapper.select4(1);
      assertEquals("hsql", answer);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldInsertInCommonWithSelectKey() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      mapper.insert(new User(2, "test"));
      String answer = mapper.select2(1);
      assertEquals("common", answer);
    } finally {
      sqlSession.close();
    }
  }  
  
  @Test
  public void shouldInsertInCommonWithSelectKey2() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      MultiDbMapper mapper = sqlSession.getMapper(MultiDbMapper.class);
      mapper.insert2(new User(2, "test"));
      String answer = mapper.select2(1);
      assertEquals("common", answer);
    } finally {
      sqlSession.close();
    }
  }  

}