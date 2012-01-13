/*
 *    Copyright 2009-2011 The MyBatis Team
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
package org.apache.ibatis.submitted.mapperparams;

import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class MapperParamsTest {

  protected static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:mapperparams", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/mapperparams/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/mapperparams/mybatis-config.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();

    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Ignore // see issue #5
  @Test
  public void shouldFailWithNonExistentParam() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {      
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Integer count = mapper.countFail(1, "John");
      assertTrue(count == 1);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void shouldUseOrdinalPositions() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {      
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Integer count = mapper.countOrdinalPositions(1, "John");
      assertTrue(count == 1);
    } finally {
      sqlSession.close();
    }
  }
  
  @Test
  public void shouldUseNew31Names() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {      
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Integer count = mapper.countNew31Names(1, "John");
      assertTrue(count == 1);
    } finally {
      sqlSession.close();
    }
  }
  
  @Ignore // see issue 165
  @Test
  public void shouldNotFailWithValue() {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {      
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Integer count = mapper.countWithValue("names", 1, "John");
      assertTrue(count == 1);
    } finally {
      sqlSession.close();
    }
  }

}