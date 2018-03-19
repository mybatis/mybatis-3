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

import static com.googlecode.catchexception.apis.BDDCatchException.caughtException;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Arrays;

import com.googlecode.catchexception.apis.BDDCatchException;
import org.apache.ibatis.executor.result.ResultMapException;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ClobTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<String> TYPE_HANDLER = new ClobTypeHandler();

  @Mock
  protected Clob clob;

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
    verify(ps).setClob(Mockito.eq(1), Mockito.any(StringReader.class));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    char[] target = new char[5000];
    Arrays.fill(target, 'a');
    when(rs.getClob("column")).thenReturn(clob);
    when(rs.wasNull()).thenReturn(false);
    when(clob.getCharacterStream()).thenReturn(new StringReader(new String(target)));
    assertEquals(new String(target), TYPE_HANDLER.getResult(rs, "column"));
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
    when(rs.wasNull()).thenReturn(false);
    when(clob.getCharacterStream()).thenReturn(new StringReader("Hello"));
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, 1));
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
    when(cs.getClob(1)).thenReturn(clob);
    when(cs.wasNull()).thenReturn(false);
    when(clob.getCharacterStream()).thenReturn(new StringReader("Hello"));
    assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getClob(1)).thenReturn(null);
    when(cs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
  }

  @Test
  public void shouldThrowSQLExceptionWhenReadError() throws Exception {
    when(cs.getClob(1)).thenReturn(clob);
    Reader reader = mock(Reader.class);
    when(reader.read(any(char[].class))).thenThrow(new IOException("io error"));
    when(clob.getCharacterStream()).thenReturn(reader);
    BDDCatchException.when(TYPE_HANDLER).getResult(cs, 1);
    then(caughtException()).isInstanceOf(ResultMapException.class)
        .hasCauseInstanceOf(SQLException.class)
        .hasRootCauseInstanceOf(IOException.class);
  }

  @Test
  public void shouldIgnoreIOExceptionWhenClose() throws Exception {
    when(rs.getClob(1)).thenReturn(clob);
    when(rs.wasNull()).thenReturn(false);
    Reader reader = spy(new InputStreamReader(new ByteArrayInputStream("Hello".getBytes())));
    doThrow(IOException.class).when(reader).close();
    when(clob.getCharacterStream()).thenReturn(reader);
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, 1));
  }

}