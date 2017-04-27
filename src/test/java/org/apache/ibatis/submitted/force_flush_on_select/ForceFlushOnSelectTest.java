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
package org.apache.ibatis.submitted.force_flush_on_select;

import static org.junit.Assert.*;

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

public class ForceFlushOnSelectTest {

  private static SqlSessionFactory sqlSessionFactory;

  @Before
  public void initDatabase() throws Exception {
    Connection conn = null;

    try {
      Class.forName("org.hsqldb.jdbcDriver");
      conn = DriverManager.getConnection("jdbc:hsqldb:mem:force_flush_on_select", "sa", "");

      Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/force_flush_on_select/CreateDB.sql");
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.setErrorLogWriter(null);
      runner.runScript(reader);
      conn.commit();
      reader.close();

      reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/force_flush_on_select/ibatisConfig.xml");
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
      reader.close();
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }

  @Test
  public void testShouldFlushLocalSessionCacheOnQuery() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
    try {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      personMapper.selectByIdFlush(1);
      updateDatabase(sqlSession.getConnection());
      Person updatedPerson = personMapper.selectByIdFlush(1);
      assertEquals("Simone", updatedPerson.getFirstName());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testShouldNotFlushLocalSessionCacheOnQuery() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
    try {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      personMapper.selectByIdNoFlush(1);
      updateDatabase(sqlSession.getConnection());
      Person updatedPerson = personMapper.selectByIdNoFlush(1);
      assertEquals("John", updatedPerson.getFirstName());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testShouldFlushLocalSessionCacheOnQueryForList() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
    try {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      List<Person> people = personMapper.selectAllFlush();
      updateDatabase(sqlSession.getConnection());
      people = personMapper.selectAllFlush();
      assertEquals("Simone", people.get(0).getFirstName());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testShouldNotFlushLocalSessionCacheOnQueryForList() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
    try {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      List<Person> people = personMapper.selectAllNoFlush();
      updateDatabase(sqlSession.getConnection());
      people = personMapper.selectAllNoFlush();
      assertEquals("John", people.get(0).getFirstName());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
  }

  private void updateDatabase(Connection conn) throws SQLException {
    Statement stmt = conn.createStatement();
    stmt.executeUpdate("UPDATE person SET firstName = 'Simone' WHERE id = 1");
    stmt.close();
  }

  @Test
  public void testUpdateShouldFlushLocalCache() throws SQLException {
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
    try {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person person = personMapper.selectByIdNoFlush(1);
      person.setLastName("Perez"); //it is ignored in update
      personMapper.update(person);
      Person updatedPerson = personMapper.selectByIdNoFlush(1);
      assertEquals("Smith", updatedPerson.getLastName());
      assertNotSame(person, updatedPerson);
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void testSelectShouldFlushLocalCacheIfFlushLocalCacheAtferEachStatementIsTrue() throws SQLException {
    sqlSessionFactory.getConfiguration().setLocalCacheScope(LocalCacheScope.STATEMENT);
    SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE);
    try {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      List<Person> people = personMapper.selectAllNoFlush();
      updateDatabase(sqlSession.getConnection());
      people = personMapper.selectAllFlush();
      assertEquals("Simone", people.get(0).getFirstName());
      sqlSession.commit();
    } finally {
      sqlSession.close();
    }
  }

}
