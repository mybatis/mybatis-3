/**
 *    Copyright 2009-2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ForceFlushOnSelectTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeEach
  void initDatabase() throws Exception {
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/force_flush_on_select/ibatisConfig.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/force_flush_on_select/CreateDB.sql");
  }

  @Test
  void testShouldFlushLocalSessionCacheOnQuery() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      personMapper.selectByIdFlush(1);
      updateDatabase(sqlSession.getConnection());
      Person updatedPerson = personMapper.selectByIdFlush(1);
      assertEquals("Simone", updatedPerson.getFirstName());
      sqlSession.commit();
    }
  }

  @Test
  void testShouldNotFlushLocalSessionCacheOnQuery() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      personMapper.selectByIdNoFlush(1);
      updateDatabase(sqlSession.getConnection());
      Person updatedPerson = personMapper.selectByIdNoFlush(1);
      assertEquals("John", updatedPerson.getFirstName());
      sqlSession.commit();
    }
  }

  @Test
  void testShouldFlushLocalSessionCacheOnQueryForList() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      List<Person> people = personMapper.selectAllFlush();
      updateDatabase(sqlSession.getConnection());
      people = personMapper.selectAllFlush();
      assertEquals("Simone", people.get(0).getFirstName());
      sqlSession.commit();
    }
  }

  @Test
  void testShouldNotFlushLocalSessionCacheOnQueryForList() throws SQLException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      List<Person> people = personMapper.selectAllNoFlush();
      updateDatabase(sqlSession.getConnection());
      people = personMapper.selectAllNoFlush();
      assertEquals("John", people.get(0).getFirstName());
      sqlSession.commit();
    }
  }

  private void updateDatabase(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate("UPDATE person SET firstName = 'Simone' WHERE id = 1");
    }
  }

  @Test
  void testUpdateShouldFlushLocalCache() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      Person person = personMapper.selectByIdNoFlush(1);
      person.setLastName("Perez"); // it is ignored in update
      personMapper.update(person);
      Person updatedPerson = personMapper.selectByIdNoFlush(1);
      assertEquals("Smith", updatedPerson.getLastName());
      assertNotSame(person, updatedPerson);
      sqlSession.commit();
    }
  }

  @Test
  void testSelectShouldFlushLocalCacheIfFlushLocalCacheAtferEachStatementIsTrue() throws SQLException {
    sqlSessionFactory.getConfiguration().setLocalCacheScope(LocalCacheScope.STATEMENT);
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.SIMPLE)) {
      PersonMapper personMapper = sqlSession.getMapper(PersonMapper.class);
      List<Person> people = personMapper.selectAllNoFlush();
      updateDatabase(sqlSession.getConnection());
      people = personMapper.selectAllFlush();
      assertEquals("Simone", people.get(0).getFirstName());
      sqlSession.commit();
    }
  }

}
