/*
 *    Copyright 2009-2014 the original author or authors.
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
package org.apache.ibatis.submitted.cache;

import java.io.Reader;
import java.sql.Connection;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// issue #524
public class CacheTest {

  private static SqlSessionFactory sqlSessionFactory;

  @Before
  public void setUp() throws Exception {
    // create a SqlSessionFactory
    Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cache/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    SqlSession session = sqlSessionFactory.openSession();
    Connection conn = session.getConnection();
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/cache/CreateDB.sql");
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(reader);
    reader.close();
    session.close();
  }
  
  /*
   * Test Plan: 
   *  1) SqlSession 1 executes "select * from A".
   *  2) SqlSession 1 closes.
   *  3) SqlSession 2 executes "delete from A where id = 1"
   *  4) SqlSession 2 executes "select * from A"
   *
   * Assert:
   *   Step 4 returns 1 row. (This case fails when caching is enabled.)
   */
  @Test
  public void testplan1() {
    SqlSession sqlSession1 = sqlSessionFactory.openSession(false);
    try {
      PersonMapper pm = sqlSession1.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }
    finally {
      sqlSession1.close();
    }
    
    SqlSession sqlSession2 = sqlSessionFactory.openSession(false);
    try {
      PersonMapper pm = sqlSession2.getMapper(PersonMapper.class);
      pm.delete(1);
      Assert.assertEquals(1, pm.findAll().size());
    }
    finally {
      sqlSession2.commit();
      sqlSession2.close();
    }
  }
  
  /*
   * Test Plan: 
   *  1) SqlSession 1 executes "select * from A".
   *  2) SqlSession 1 closes.
   *  3) SqlSession 2 executes "delete from A where id = 1"
   *  4) SqlSession 2 executes "select * from A"
   *  5) SqlSession 2 rollback
   *  6) SqlSession 3 executes "select * from A"
   *
   * Assert:
   *   Step 6 returns 2 rows. 
   */
  @Test
  public void testplan2() {
    SqlSession sqlSession1 = sqlSessionFactory.openSession(false);
    try {
      PersonMapper pm = sqlSession1.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }
    finally {
      sqlSession1.close();
    }
    
    SqlSession sqlSession2 = sqlSessionFactory.openSession(false);
    try {
      PersonMapper pm = sqlSession2.getMapper(PersonMapper.class);
      pm.delete(1);
    }
    finally {
      sqlSession2.rollback();
      sqlSession2.close();
    }
    
    SqlSession sqlSession3 = sqlSessionFactory.openSession(false);
    try {
      PersonMapper pm = sqlSession3.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }
    finally {
      sqlSession3.close();
    }
  }
  
  /*
   * Test Plan with Autocommit on:
   *  1) SqlSession 1 executes "select * from A".
   *  2) SqlSession 1 closes.
   *  3) SqlSession 2 executes "delete from A where id = 1"
   *  4) SqlSession 2 closes.
   *  5) SqlSession 2 executes "select * from A".
   *  6) SqlSession 3 closes.
   *
   * Assert:
   *   Step 6 returns 1 row. 
   */
  @Test
  public void testplan3() {
    SqlSession sqlSession1 = sqlSessionFactory.openSession(true);
    try {
      PersonMapper pm = sqlSession1.getMapper(PersonMapper.class);
      Assert.assertEquals(2, pm.findAll().size());
    }
    finally {
      sqlSession1.close();
    }
    
    SqlSession sqlSession2 = sqlSessionFactory.openSession(true);
    try {
      PersonMapper pm = sqlSession2.getMapper(PersonMapper.class);
      pm.delete(1);
    }
    finally {
      sqlSession2.close();
    }
    
    SqlSession sqlSession3 = sqlSessionFactory.openSession(true);
    try {
      PersonMapper pm = sqlSession3.getMapper(PersonMapper.class);
      Assert.assertEquals(1, pm.findAll().size());
    }
    finally {
      sqlSession3.close();
    }
  }

}
