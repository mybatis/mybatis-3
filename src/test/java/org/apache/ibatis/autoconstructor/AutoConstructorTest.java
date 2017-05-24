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
package org.apache.ibatis.autoconstructor;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

public class AutoConstructorTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    final Reader reader = Resources.getResourceAsReader("org/apache/ibatis/autoconstructor/mybatis-config.xml");
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    reader.close();

    // populate in-memory database
    final SqlSession session = sqlSessionFactory.openSession();
    final Connection conn = session.getConnection();
    final Reader dbReader = Resources.getResourceAsReader("org/apache/ibatis/autoconstructor/CreateDB.sql");
    final ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.runScript(dbReader);
    conn.close();
    dbReader.close();
    session.close();
  }

  @Test
  public void fullyPopulatedSubject() {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      final Object subject = mapper.getSubject(1);
      Assert.assertNotNull(subject);
    } finally {
      sqlSession.close();
    }
  }

  @Test(expected = PersistenceException.class)
  public void primitiveSubjects() {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      mapper.getSubjects();
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void wrapperSubject() {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      verifySubjects(mapper.getWrapperSubjects());
    } finally {
      sqlSession.close();
    }
  }

  @Test
  public void annotatedSubject() {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      verifySubjects(mapper.getAnnotatedSubjects());
    } finally {
      sqlSession.close();
    }
  }

  @Test(expected = PersistenceException.class)
  public void badSubject() {
    final SqlSession sqlSession = sqlSessionFactory.openSession();
    try {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      mapper.getBadSubjects();
    } finally {
      sqlSession.close();
    }
  }

  private void verifySubjects(final List<?> subjects) {
    Assert.assertNotNull(subjects);
    Assertions.assertThat(subjects.size()).isEqualTo(3);
  }
}
