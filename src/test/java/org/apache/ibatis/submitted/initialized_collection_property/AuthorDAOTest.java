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
package org.apache.ibatis.submitted.initialized_collection_property;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

public class AuthorDAOTest {

  private static SqlSessionFactory factory;

  @BeforeClass
  public static void testGetMessageForEmptyDatabase() throws Exception {
    final String resource = "org/apache/ibatis/submitted/initialized_collection_property/mybatis-config.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    factory = new SqlSessionFactoryBuilder().build(reader);

    SqlSession session = factory.openSession();

    Connection conn = session.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.setErrorLogWriter(null);
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/initialized_collection_property/create.sql");
    runner.runScript(reader);
    conn.close();
    session.close();
  }

  @Test
  public void shouldNotOverwriteCollectionOnNestedResultMap() {
    SqlSession session = factory.openSession();
    try {
    List<Author> authors = session.selectList("getAllAuthors");
    assertEquals(1, authors.size());
    assertEquals(4, authors.get(0).getPosts().size());
    } finally {
      session.close();
    }
  }

  @Ignore // issue #75 nested selects overwrite collections
  @Test
  public void shouldNotOverwriteCollectionOnNestedQuery() {
    SqlSession session = factory.openSession();
    try {
    List<Author> authors = session.selectList("getAllAuthorsNestedQuery");
    assertEquals(1, authors.size());
    assertEquals(4, authors.get(0).getPosts().size());
    } finally {
      session.close();
    }
  }

}
