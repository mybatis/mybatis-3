/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.autoconstructor;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.hamcrest.CoreMatchers;
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
    dbReader.close();
    session.close();
  }

  @Test
  public void fullyPopulatedSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final Mapper mapper = sqlSession.getMapper(Mapper.class);
      final Object subject = mapper.getSubject(1);
      Assert.assertNotNull(subject);
    }
  }

  @Test(expected = PersistenceException.class)
  public void primitiveSubjects() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.getSubjects();
    }
  }

  @Test
  public void wrapperSubject() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      final Mapper mapper = sqlSession.getMapper(Mapper.class);
      final List<WrapperSubject> subjects = mapper.getWrapperSubjects();
      Assert.assertNotNull(subjects);
      Assert.assertThat(subjects.size(), CoreMatchers.equalTo(3));
    }
  }
}
