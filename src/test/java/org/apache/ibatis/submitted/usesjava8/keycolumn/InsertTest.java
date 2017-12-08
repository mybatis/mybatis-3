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
package org.apache.ibatis.submitted.usesjava8.keycolumn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.test.EmbeddedPostgresqlTests;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.util.SocketUtil;

/**
 * @author Jeff Butler
 */
@Category(EmbeddedPostgresqlTests.class)
public class InsertTest {

  private static final EmbeddedPostgres postgres = new EmbeddedPostgres();

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // Launch PostgreSQL server. Download / unarchive if necessary.
    String url = postgres.start(EmbeddedPostgres.cachedRuntimeConfig(Paths.get(System.getProperty("java.io.tmpdir"), "pgembed")), "localhost", SocketUtil.findFreePort(), "keycolumn", "postgres", "root", Collections.emptyList());

    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(), new UnpooledDataSource(
        "org.postgresql.Driver", url, null));
    configuration.setEnvironment(environment);
    configuration.addMapper(InsertMapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    try (SqlSession session = sqlSessionFactory.openSession();
        Connection conn = session.getConnection();
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/usesjava8/keycolumn/CreateDB.sql")) {
      ScriptRunner runner = new ScriptRunner(conn);
      runner.setLogWriter(null);
      runner.runScript(reader);
    }
  }

  @AfterClass
  public static void tearDown() {
    postgres.stop();
  }

  @Test
  public void testInsertAnnotated() throws Exception {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");

      int rows = mapper.insertNameAnnotated(name);

      assertNotNull(name.getId());
      assertEquals(1, rows);
    }
  }

  @Test
  public void testInsertMapped() throws Exception {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");

      int rows = mapper.insertNameMapped(name);

      assertNotNull(name.getId());
      assertEquals(1, rows);
    }
  }

  @Test
  public void testInsertMappedBatch() throws Exception {
    try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
      InsertMapper mapper = sqlSession.getMapper(InsertMapper.class);
      Name name = new Name();
      name.setFirstName("Fred");
      name.setLastName("Flintstone");
      mapper.insertNameMapped(name);
      Name name2 = new Name();
      name2.setFirstName("Wilma");
      name2.setLastName("Flintstone");
      mapper.insertNameMapped(name2);
      List<BatchResult> batchResults = sqlSession.flushStatements();
      assertNotNull(name.getId());
      assertNotNull(name2.getId());
      assertEquals(1, batchResults.size());
    }
  }

}
