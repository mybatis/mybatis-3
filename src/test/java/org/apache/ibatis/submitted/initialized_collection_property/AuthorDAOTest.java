/*
 *    Copyright 2009-2011 The MyBatis Team
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
import org.apache.ibatis.submitted.initialized_collection_property.test.Author;
import org.apache.ibatis.submitted.initialized_collection_property.test.AuthorDAO;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.Reader;
import java.sql.Connection;
import java.util.List;

public class AuthorDAOTest {


  @Test
  public void testGetMessageForEmptyDatabase() throws Exception {
    final String resource = "org/apache/ibatis/submitted/initialized_collection_property/ibatis-config.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);

    SqlSession session = factory.openSession();

    Connection conn = session.getConnection();
    ScriptRunner runner = new ScriptRunner(conn);
    runner.setLogWriter(null);
    runner.setErrorLogWriter(null);
    reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/initialized_collection_property/db/create.sql");
    runner.runScript(reader);
    session.close();

    AuthorDAO dao = new AuthorDAO(factory);
    List<Author> authors = dao.getAuthors();
    assertEquals(1, authors.size());
  }

}
