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
package org.apache.ibatis.submitted.initialized_collection_property;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.util.List;

class AuthorDAOTest {

  private static SqlSessionFactory factory;

  @BeforeAll
  static void testGetMessageForEmptyDatabase() throws Exception {
    final String resource = "org/apache/ibatis/submitted/initialized_collection_property/mybatis-config.xml";
    try (Reader reader = Resources.getResourceAsReader(resource)) {
      factory = new SqlSessionFactoryBuilder().build(reader);
    }

    BaseDataTest.runScript(factory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/submitted/initialized_collection_property/create.sql");
  }

  @Test
  void shouldNotOverwriteCollectionOnNestedResultMap() {
    try (SqlSession session = factory.openSession()) {
      List<Author> authors = session.selectList("getAllAuthors");
      assertEquals(1, authors.size());
      assertEquals(4, authors.get(0).getPosts().size());
    }
  }

  @Disabled // issue #75 nested selects overwrite collections
  @Test
  void shouldNotOverwriteCollectionOnNestedQuery() {
    try (SqlSession session = factory.openSession()) {
      List<Author> authors = session.selectList("getAllAuthorsNestedQuery");
      assertEquals(1, authors.size());
      assertEquals(4, authors.get(0).getPosts().size());
    }
  }

}
