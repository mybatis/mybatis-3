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

class RepeatableUpdateTest {

  @Test
  void hsql() throws IOException, SQLException {
    SqlSessionFactory sqlSessionFactory;
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/repeatable/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, "development-hsql");
    }

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/repeatable/CreateDB.sql");

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.updateUserName(1);

      User user = mapper.getUser(1);
      Assertions.assertEquals("User1 HSQL", user.getName());
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

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      mapper.updateUserNameUsingProvider(1);

      User user = mapper.getUser(1);
      Assertions.assertEquals("User1 HSQL", user.getName());
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

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      mapper.updateUserName(1);

      User user = mapper.getUser(1);
      Assertions.assertEquals("User1 DERBY", user.getName());
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

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      mapper.updateUserNameUsingProvider(1);

      User user = mapper.getUser(1);
      Assertions.assertEquals("User1 DERBY", user.getName());
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

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      mapper.updateUserName(1);

      User user = mapper.getUser(1);
      Assertions.assertEquals("User1 DEFAULT", user.getName());
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

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);

      mapper.updateUserNameUsingProvider(1);

      User user = mapper.getUser(1);
      Assertions.assertEquals("User1 DEFAULT", user.getName());
    }
  }

}
