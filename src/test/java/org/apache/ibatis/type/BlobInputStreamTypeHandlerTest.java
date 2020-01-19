/**
 *    Copyright 2009-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.*;
import java.sql.Blob;
import javax.sql.DataSource;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Tests for {@link BlobInputStreamTypeHandler}.
 *
 * @since 3.4.0
 * @author Kazuki Shimizu
 */
class BlobInputStreamTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<InputStream> TYPE_HANDLER = new BlobInputStreamTypeHandler();

  private static SqlSessionFactory sqlSessionFactory;

  @Mock
  protected Blob blob;

  @BeforeAll
  static void setupSqlSessionFactory() throws Exception {
    DataSource dataSource = BaseDataTest.createUnpooledDataSource("org/apache/ibatis/type/jdbc.properties");
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
            "org/apache/ibatis/type/BlobInputStreamTypeHandlerTest.sql");
  }

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    InputStream in = new ByteArrayInputStream("Hello".getBytes());
    TYPE_HANDLER.setParameter(ps, 1, in, null);
    verify(ps).setBlob(1, in);
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    InputStream in = new ByteArrayInputStream("Hello".getBytes());
    when(rs.getBlob("column")).thenReturn(blob);
    when(blob.getBinaryStream()).thenReturn(in);
    assertThat(TYPE_HANDLER.getResult(rs, "column")).isEqualTo(in);

  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getBlob("column")).thenReturn(null);
    assertThat(TYPE_HANDLER.getResult(rs, "column")).isNull();
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    InputStream in = new ByteArrayInputStream("Hello".getBytes());
    when(rs.getBlob(1)).thenReturn(blob);
    when(blob.getBinaryStream()).thenReturn(in);
    assertThat(TYPE_HANDLER.getResult(rs, 1)).isEqualTo(in);
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getBlob(1)).thenReturn(null);
    assertThat(TYPE_HANDLER.getResult(rs, 1)).isNull();
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    InputStream in = new ByteArrayInputStream("Hello".getBytes());
    when(cs.getBlob(1)).thenReturn(blob);
    when(blob.getBinaryStream()).thenReturn(in);
    assertThat(TYPE_HANDLER.getResult(cs, 1)).isEqualTo(in);
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getBlob(1)).thenReturn(null);
    assertThat(TYPE_HANDLER.getResult(cs, 1)).isNull();
  }

  @Test
  void integrationTest() throws IOException {
    try (SqlSession session = sqlSessionFactory.openSession()) {
      Mapper mapper = session.getMapper(Mapper.class);
      // insert (InputStream -> Blob)
      {
        BlobContent blobContent = new BlobContent();
        blobContent.setId(1);
        blobContent.setContent(new ByteArrayInputStream("Hello".getBytes()));
        mapper.insert(blobContent);
        session.commit();
      }
      // select (Blob -> InputStream)
      {
        BlobContent blobContent = mapper.findOne(1);
        assertThat(new BufferedReader(new InputStreamReader(blobContent.getContent())).readLine()).isEqualTo("Hello");
      }
    }

  }

  interface Mapper {
    @Select("SELECT ID, CONTENT FROM TEST_BLOB WHERE ID = #{id}")
    BlobContent findOne(int id);

    @Insert("INSERT INTO TEST_BLOB (ID, CONTENT) VALUES(#{id}, #{content})")
    void insert(BlobContent blobContent);
  }

  static class BlobContent {
    private int id;
    private InputStream content;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public InputStream getContent() {
      return content;
    }

    public void setContent(InputStream content) {
      this.content = content;
    }
  }

}
