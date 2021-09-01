/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.parent_childs;

import java.io.Reader;
import java.util.List;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ParentChildTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create a SqlSessionFactory
    try (
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/parent_childs/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
        "org/apache/ibatis/submitted/parent_childs/CreateDB.sql");
  }

  @Test
  void shouldGet2Parents() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Parent> parents = mapper.getParents();
      Assertions.assertEquals(2, parents.size());
      Parent firstParent = parents.get(0);
      Assertions.assertEquals("Jose", firstParent.getName());
      Assertions.assertEquals(2, firstParent.getChilds().size());
      Parent secondParent = parents.get(1);
      Assertions.assertEquals("Juan", secondParent.getName());
      Assertions.assertEquals(0, secondParent.getChilds().size()); // note an empty list is inyected
    }
  }

  // issue #1848
  @Test
  void shouldGet2ParentsWithConstructor() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<Parent> parents = mapper.getParentsWithConstructor();
      Assertions.assertEquals(2, parents.size());
      Parent firstParent = parents.get(0);
      Assertions.assertEquals("Jose", firstParent.getName());
      Assertions.assertEquals(2, firstParent.getChilds().size());
      Parent secondParent = parents.get(1);
      Assertions.assertEquals("Juan", secondParent.getName());
      Assertions.assertEquals(0, secondParent.getChilds().size()); // note an empty list is inyected
    }
  }

}
