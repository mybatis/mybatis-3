/**
 *    Copyright 2009-2018 the original author or authors.
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

package org.apache.ibatis.type;

import static org.junit.Assert.*;

import java.io.Reader;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Collections;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.util.SocketUtil;

@Category(EmbeddedPostgresqlTests.class)
public class SqlxmlTypeHandlerTest {
  private static final EmbeddedPostgres postgres = new EmbeddedPostgres();

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setUp() throws Exception {
    // Launch PostgreSQL server. Download / unarchive if necessary.
    String url = postgres.start(
        EmbeddedPostgres.cachedRuntimeConfig(Paths.get(System.getProperty("java.io.tmpdir"), "pgembed")), "localhost",
        SocketUtil.findFreePort(), "postgres_sqlxml", "postgres", "root", Collections.emptyList());

    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(), new UnpooledDataSource(
        "org.postgresql.Driver", url, null));
    configuration.setEnvironment(environment);
    configuration.setUseGeneratedKeys(true);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    try (SqlSession session = sqlSessionFactory.openSession();
        Connection conn = session.getConnection();
        Reader reader = Resources
            .getResourceAsReader("org/apache/ibatis/type/SqlxmlTypeHandlerTest.sql")) {
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
  public void shouldReturnXmlAsString() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      XmlBean bean = mapper.select(1);
      assertEquals("<title>XML data</title>",
          bean.getContent());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldReturnNull() throws Exception {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      Mapper mapper = session.getMapper(Mapper.class);
      XmlBean bean = mapper.select(2);
      assertNull(bean.getContent());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldInsertXmlString() throws Exception {
    final Integer id = 100;
    final String content = "<books><book><title>Save XML</title></book><book><title>Get XML</title></book></books>";
    // Insert
    {
      SqlSession session = sqlSessionFactory.openSession();
      try {
        Mapper mapper = session.getMapper(Mapper.class);
        XmlBean bean = new XmlBean();
        bean.setId(id);
        bean.setContent(content);
        mapper.insert(bean);
        session.commit();
      } finally {
        session.close();
      }
    }
    // Select to verify
    {
      SqlSession session = sqlSessionFactory.openSession();
      try {
        Mapper mapper = session.getMapper(Mapper.class);
        XmlBean bean = mapper.select(id);
        assertEquals(content, bean.getContent());
      } finally {
        session.close();
      }
    }
  }

  interface Mapper {
    @Select("select id, content from mbtest.test_sqlxml where id = #{id}")
    XmlBean select(Integer id);

    @Insert("insert into mbtest.test_sqlxml (id, content) values (#{id}, #{content,jdbcType=SQLXML})")
    void insert(XmlBean bean);
  }

  public static class XmlBean {
    private Integer id;

    private String content;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }
}
