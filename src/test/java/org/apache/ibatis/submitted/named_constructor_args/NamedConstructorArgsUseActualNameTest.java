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
package org.apache.ibatis.submitted.named_constructor_args;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Reader;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NamedConstructorArgsUseActualNameTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/named_constructor_args/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    sqlSessionFactory.getConfiguration().addMapper(UseActualNameMapper.class);

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/named_constructor_args/CreateDB.sql");
  }

  @Test
  void argsByActualNames() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UseActualNameMapper mapper = sqlSession.getMapper(UseActualNameMapper.class);
      User user = mapper.mapConstructorWithoutParamAnnos(1);
      assertEquals(Integer.valueOf(1), user.getId());
      assertEquals("User1", user.getName());
    }
  }

  @Test
  void argsByActualNamesXml() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UseActualNameMapper mapper = sqlSession.getMapper(UseActualNameMapper.class);
      User user = mapper.mapConstructorWithoutParamAnnosXml(1);
      assertEquals(Integer.valueOf(1), user.getId());
      assertEquals("User1", user.getName());
    }
  }

}
