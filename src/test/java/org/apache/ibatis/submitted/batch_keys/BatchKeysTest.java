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
package org.apache.ibatis.submitted.batch_keys;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

import org.junit.Assert;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

public class BatchKeysTest {

  private SqlSessionFactory sqlSessionFactory;

  @Before
  public void setUp() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:batch_keys", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/batch_keys/CreateDB.sql");

      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(new PrintWriter(System.err));
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/batch_keys/Config.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  public void testJdbc3Support() throws Exception {
    Connection conn = sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection();
    PreparedStatement stmt = conn.prepareStatement("insert into users2 values(null, 'Pocoyo')", Statement.RETURN_GENERATED_KEYS);
    stmt.addBatch();
    stmt.executeBatch();
    ResultSet rs = stmt.getGeneratedKeys();
    if (rs.next()) {
      ResultSetMetaData rsmd = rs.getMetaData();
      int colCount = rsmd.getColumnCount();
      do {
        for (int i = 1; i <= colCount; i++) {
          String key = rs.getString(i);
          System.out.println("key " + i + " is " + key);
        }
      } while (rs.next());
    } else {
      System.out.println("There are no generated keys.");
    }
    stmt.close();
    conn.close();
  }

  @Test
  public void testInsert() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    try {
      User user1 = new User(null, "Pocoyo");
      sqlSession.insert("insert", user1);
      User user2 = new User(null, "Valentina");
      sqlSession.insert("insert", user2);
      sqlSession.flushStatements();
      assertEquals(new Integer(50), user1.getId());
      assertEquals(new Integer(50), user2.getId());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
    try {
      sqlSession = sqlSessionFactory.openSession();
      List<User> users = sqlSession.selectList("select");
      Assert.assertTrue(users.size() == 2);
    } finally {
      sqlSession.close();
    }
  }


  @Test
  public void testInsertJdbc3() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    try {
      User user1 = new User(null, "Pocoyo");
      sqlSession.insert("insertIdentity", user1);
      User user2 = new User(null, "Valentina");
      sqlSession.insert("insertIdentity", user2);
      sqlSession.flushStatements();
      assertEquals(Integer.valueOf(0), user1.getId());
      assertEquals(Integer.valueOf(1), user2.getId());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }

    try {
      sqlSession = sqlSessionFactory.openSession();
      List<User> users = sqlSession.selectList("selectIdentity");
      Assert.assertTrue(users.size() == 2);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testInsertWithMapper() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    try {
      Mapper userMapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User(null, "Pocoyo");
      userMapper.insert(user1);
      User user2 = new User(null, "Valentina");
      userMapper.insert(user2);
      sqlSession.flushStatements();
      assertEquals(new Integer(50), user1.getId());
      assertEquals(new Integer(50), user2.getId());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }

    try {
      sqlSession = sqlSessionFactory.openSession();
      List<User> users = sqlSession.selectList("select");
      Assert.assertTrue(users.size() == 2);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testInsertMapperJdbc3() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
    try {
      Mapper userMapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User(null, "Pocoyo");
      userMapper.insertIdentity(user1);
      User user2 = new User(null, "Valentina");
      userMapper.insertIdentity(user2);
      sqlSession.flushStatements();
      assertEquals(Integer.valueOf(0), user1.getId());
      assertEquals(Integer.valueOf(1), user2.getId());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }

    try {
      sqlSession = sqlSessionFactory.openSession();
      List<User> users = sqlSession.selectList("selectIdentity");
      Assert.assertTrue(users.size() == 2);
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testInsertMapperNoBatchJdbc3() throws Exception {
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      Mapper userMapper = sqlSession.getMapper(Mapper.class);
      User user1 = new User(null, "Pocoyo");
      userMapper.insertIdentity(user1);
      assertEquals(Integer.valueOf(0), user1.getId());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }

    try {
      sqlSession = sqlSessionFactory.openSession();
      List<User> users = sqlSession.selectList("selectIdentity");
      Assert.assertTrue(users.size() == 1);
    } finally {
      sqlSession.close();
    }
  }
  
}
