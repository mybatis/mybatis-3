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
package org.apache.ibatis.type;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLXML;
import java.util.Collections;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;
import ru.yandex.qatools.embed.postgresql.util.SocketUtil;

@Tag("EmbeddedPostgresqlTests")
class SqlxmlTypeHandlerTest extends BaseTypeHandlerTest {
  private static final TypeHandler<String> TYPE_HANDLER = new SqlxmlTypeHandler();
  private static final EmbeddedPostgres postgres = new EmbeddedPostgres();

  private static SqlSessionFactory sqlSessionFactory;

  @Mock
  private SQLXML sqlxml;

  @Mock
  private Connection connection;

  @BeforeAll
  static void setUp() throws Exception {
    // Launch PostgreSQL server. Download / unarchive if necessary.
    String url = postgres.start(
        EmbeddedPostgres.cachedRuntimeConfig(Paths.get(System.getProperty("java.io.tmpdir"), "pgembed")), "localhost",
        SocketUtil.findFreePort(), "postgres_sqlxml", "postgres", "root", Collections.emptyList());

    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(), new UnpooledDataSource(
        "org.postgresql.Driver", url, null));
    configuration.setEnvironment(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/type/SqlxmlTypeHandlerTest.sql");
  }

  @AfterAll
  static void tearDown() {
    postgres.stop();
  }

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    when(connection.createSQLXML()).thenReturn(sqlxml);
    when(ps.getConnection()).thenReturn(connection);
    String xml = "<message>test</message>";
    TYPE_HANDLER.setParameter(ps, 1, xml, null);
    verify(ps).setSQLXML(1, sqlxml);
    verify(sqlxml).setString(xml);
    verify(sqlxml).free();
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    String xml = "<message>test</message>";
    when(sqlxml.getString()).thenReturn(xml);
    when(rs.getSQLXML("column")).thenReturn(sqlxml);
    assertEquals(xml, TYPE_HANDLER.getResult(rs, "column"));
    verify(sqlxml).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getSQLXML("column")).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    String xml = "<message>test</message>";
    when(sqlxml.getString()).thenReturn(xml);
    when(rs.getSQLXML(1)).thenReturn(sqlxml);
    assertEquals(xml, TYPE_HANDLER.getResult(rs, 1));
    verify(sqlxml).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getSQLXML(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    String xml = "<message>test</message>";
    when(sqlxml.getString()).thenReturn(xml);
    when(cs.getSQLXML(1)).thenReturn(sqlxml);
    assertEquals(xml, TYPE_HANDLER.getResult(cs, 1));
    verify(sqlxml).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getSQLXML(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
  }

  @Test
  void shouldReturnXmlAsString() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      XmlBean bean = mapper.select(1);
      assertEquals("<title>XML data</title>",
          bean.getContent());
    }
  }

  @Test
  void shouldReturnNull() {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      XmlBean bean = mapper.select(2);
      assertNull(bean.getContent());
    }
  }

  @Test
  void shouldInsertXmlString() {
    final Integer id = 100;
    final String content = "<books><book><title>Save XML</title></book><book><title>Get XML</title></book></books>";
    // Insert
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      XmlBean bean = new XmlBean();
      bean.setId(id);
      bean.setContent(content);
      mapper.insert(bean);
      session.commit();
    }
    // Select to verify
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      XmlBean bean = mapper.select(id);
      assertEquals(content, bean.getContent());
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
