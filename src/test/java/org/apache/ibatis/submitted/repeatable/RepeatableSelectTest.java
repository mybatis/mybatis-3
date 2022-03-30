/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.repeatable;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

class RepeatableSelectTest {

  @Test
  void hsql() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-hsql");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    User user;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      user = mapper.getUser(1);
      sqlSession.commit();
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("HSQL", user.getDatabaseName());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertNotSame(user, mapper.getUser(1));
    }
  }

  @Test
  void hsqlUsingProvider() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-hsql");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    User user;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      user = mapper.getUserUsingProvider(1);
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("HSQL", user.getDatabaseName());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertSame(user, mapper.getUserUsingProvider(1));
    }
  }

  @Test
  void derby() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-derby");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    User user;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      user = mapper.getUser(1);
      sqlSession.commit();
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("DERBY", user.getDatabaseName());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertNotSame(user, mapper.getUser(1));
    }
  }

  @Test
  void derbyUsingProvider() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-derby");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    User user;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      user = mapper.getUserUsingProvider(1);
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("DERBY", user.getDatabaseName());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertSame(user, mapper.getUserUsingProvider(1));
    }
  }

  @Test
  void h2() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-h2");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    User user;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      user = mapper.getUser(1);
      sqlSession.commit();
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("DEFAULT", user.getDatabaseName());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertSame(user, mapper.getUser(1));
    }
  }

  @Test
  void h2UsingProvider() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-h2");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    User user;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      user = mapper.getUserUsingProvider(1);
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("DEFAULT", user.getDatabaseName());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertNotSame(user, mapper.getUserUsingProvider(1));
    }
  }

  @Test
  void usingBoth() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-derby");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    User user;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      user = mapper.getUserUsingBoth(1);
      sqlSession.commit();
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("DERBY", user.getDatabaseName());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertNotSame(user, mapper.getUser(1));
    }
  }

  @Test
  void usingBothProvider() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-hsql");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    User user;
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      user = mapper.getUserUsingBoth(1);
      sqlSession.commit();
      Assertions.assertEquals("User1", user.getName());
      Assertions.assertEquals("HSQL", user.getDatabaseName());
    }
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      Assertions.assertNotSame(user, mapper.getUser(1));
    }
  }
}
