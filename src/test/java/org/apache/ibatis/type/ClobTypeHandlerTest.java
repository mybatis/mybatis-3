/*
 *    Copyright 2009-2012 the original author or authors.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.sql.Clob;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class ClobTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<String> TYPE_HANDLER = new ClobTypeHandler();

  @Mock
  protected Clob clob;

  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
    verify(ps).setCharacterStream(Mockito.eq(1), Mockito.any(Reader.class), Mockito.eq(5));
  }

  @Test
  public void shouldGetResultFromResultSet() throws Exception {
    when(rs.getClob("column")).thenReturn(clob);
    when(rs.wasNull()).thenReturn(false);
    when(clob.length()).thenReturn(3l);
    when(clob.getSubString(1, 3)).thenReturn("Hello");
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
  }

  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getClob(1)).thenReturn(clob);
    when(cs.wasNull()).thenReturn(false);
    when(clob.length()).thenReturn(3l);
    when(clob.getSubString(1, 3)).thenReturn("Hello");
    assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
  }

}