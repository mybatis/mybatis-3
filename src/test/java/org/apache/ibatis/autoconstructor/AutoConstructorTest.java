/**
 *    Copyright 2009-2018 the original author or authors.
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

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.Reader;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class AutoConstructorTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/autoconstructor/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/autoconstructor/CreateDB.sql");
  }

  @Test
  public void fullyPopulatedSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      final Object subject = mapper.getSubject(1);
      assertNotNull(subject);
    }
  }

  @Test(expected = PersistenceException.class)
  public void primitiveSubjects() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      mapper.getSubjects();
    }
  }

  @Test
  public void annotatedSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      verifySubjects(mapper.getAnnotatedSubjects());
    }
  }

  @Test(expected = PersistenceException.class)
  public void badSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      mapper.getBadSubjects();
    }
  }

  @Test
  public void extensiveSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final AutoConstructorMapper mapper = sqlSession.getMapper(AutoConstructorMapper.class);
      verifySubjects(mapper.getExtensiveSubject());
    }
  }

  private void verifySubjects(final List<?> subjects) {
    assertNotNull(subjects);
    Assertions.assertThat(subjects.size()).isEqualTo(3);
  }
}
