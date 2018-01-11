/**
 *    Copyright 2009-2016 the original author or authors.
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Clob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ClobReaderTypeHandler}.
 *
 * @since 3.4.0
 * @author Kazuki Shimizu
 */
public class ClobReaderTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Reader> TYPE_HANDLER = new ClobReaderTypeHandler();

  private static SqlSessionFactory sqlSessionFactory;

  @Mock
  protected Clob clob;

  @BeforeClass
  public static void setupSqlSessionFactory() throws Exception {
    DataSource dataSource = BaseDataTest.createUnpooledDataSource("org/apache/ibatis/type/jdbc.properties");
    BaseDataTest.runScript(dataSource, "org/apache/ibatis/type/ClobReaderTypeHandlerTest.sql");
    TransactionFactory transactionFactory = new JdbcTransactionFactory();
    Environment environment = new Environment("Production", transactionFactory, dataSource);
    Configuration configuration = new Configuration(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
  }

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    Reader reader = new StringReader("Hello");
    TYPE_HANDLER.setParameter(ps, 1, reader, null);
    verify(ps).setClob(1, reader);
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    Reader reader = new StringReader("Hello");
    when(rs.getClob("column")).thenReturn(clob);
    when(rs.wasNull()).thenReturn(false);
    when(clob.getCharacterStream()).thenReturn(reader);
    assertEquals(reader, TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getClob("column")).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getClob(1)).thenReturn(clob);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getClob(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    Reader reader = new StringReader("Hello");
    when(cs.getClob(1)).thenReturn(clob);
    when(cs.wasNull()).thenReturn(false);
    when(clob.getCharacterStream()).thenReturn(reader);
    assertEquals(reader, TYPE_HANDLER.getResult(cs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getClob(1)).thenReturn(null);
    when(cs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
  }


  @Test
  public void integrationTest() throws IOException {
    SqlSession session = sqlSessionFactory.openSession();

    try {
      Mapper mapper = session.getMapper(Mapper.class);
      // insert (Reader -> Clob)
      {
        ClobContent clobContent = new ClobContent();
        clobContent.setId(1);
        clobContent.setContent(new StringReader("Hello"));
        mapper.insert(clobContent);
        session.commit();
      }
      // select (Clob -> Reader)
      {
        ClobContent clobContent = mapper.findOne(1);
        assertThat(new BufferedReader(clobContent.getContent()).readLine()).isEqualTo("Hello");
      }
    } finally {
      session.close();
    }

  }

  interface Mapper {
    @Select("SELECT ID, CONTENT FROM TEST_CLOB WHERE ID = #{id}")
    ClobContent findOne(int id);

    @Insert("INSERT INTO TEST_CLOB (ID, CONTENT) VALUES(#{id}, #{content})")
    void insert(ClobContent blobContent);
  }

  static class ClobContent {
    private int id;
    private Reader content;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public Reader getContent() {
      return content;
    }

    public void setContent(Reader content) {
      this.content = content;
    }
  }

}