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
package org.apache.ibatis.submitted.usesjava8.multiple_resultsets;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.test.EmbeddedPostgresqlTests;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.util.SocketUtil;

/*
 * This class contains tests for multiple results.  
 * It is based on Jeff's ref cursor tests.
 */
@Category(EmbeddedPostgresqlTests.class)
public class MultipleResultTest {

  private static final EmbeddedPostgres postgres = new EmbeddedPostgres();

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // Launch PostgreSQL server. Download / unarchive if necessary.
    String url = postgres.start(EmbeddedPostgres.cachedRuntimeConfig(Paths.get(System.getProperty("java.io.tmpdir"), "pgembed")), "localhost", SocketUtil.findFreePort(), "multiple_resultsets", "postgres", "root", Collections.emptyList());

    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(), new UnpooledDataSource(
        "org.postgresql.Driver", url, null));
    configuration.setEnvironment(environment);
    configuration.setMapUnderscoreToCamelCase(true);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    try (SqlSession session = sqlSessionFactory.openSession();
        Connection conn = session.getConnection();
        Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/usesjava8/multiple_resultsets/CreateDB.sql")) {
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
  public void shouldGetMultipleResultSetsWithOneStatement() throws IOException {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<List<?>> results = mapper.getUsersAndGroups();
      Assert.assertEquals(2, results.size());

      Assert.assertEquals(6, results.get(0).size());
      OrderDetail detail = (OrderDetail)results.get(0).get(0);
      Assert.assertEquals(1, detail.getOrderId());
      Assert.assertEquals(1, detail.getLineNumber());
      Assert.assertEquals(1, detail.getQuantity());
      Assert.assertEquals("Pen", detail.getItemDescription());

      Assert.assertEquals(2, results.get(1).size());
      OrderHeader header = (OrderHeader)results.get(1).get(0);
      Assert.assertEquals(1, header.getOrderId());
      Assert.assertEquals("Fred", header.getCustName());
    }
  }

  @Test
  public void shouldSkipNullResultSet() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      try {
        Mapper mapper = sqlSession.getMapper(Mapper.class);
        List<List<?>> results = mapper.multiResultsWithUpdate();
        Assert.assertEquals(2, results.size());

        Assert.assertEquals(6, results.get(0).size());
        OrderDetail detail = (OrderDetail) results.get(0).get(0);
        Assert.assertEquals(1, detail.getOrderId());
        Assert.assertEquals(1, detail.getLineNumber());
        Assert.assertEquals(1, detail.getQuantity());
        Assert.assertEquals("Pen", detail.getItemDescription());

        Assert.assertEquals(2, results.get(1).size());
        OrderHeader header = (OrderHeader) results.get(1).get(0);
        Assert.assertEquals(1, header.getOrderId());
        Assert.assertEquals("Fred", header.getCustName());

        results = mapper.getUsersAndGroups();
        Assert.assertEquals(7, results.get(0).size());
        detail = (OrderDetail) results.get(0).get(6);
        Assert.assertEquals(2, detail.getOrderId());
        Assert.assertEquals(4, detail.getLineNumber());
        Assert.assertEquals(5, detail.getQuantity());
        Assert.assertEquals("Eraser", detail.getItemDescription());
      } finally {
        sqlSession.rollback(true);
      }
    }
  }
}
