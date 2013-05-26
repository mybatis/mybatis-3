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

import org.junit.Test;

public class ByteTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Byte> TYPE_HANDLER = new ByteTypeHandler();

  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, (byte) 100, null);
    verify(ps).setByte(1, (byte) 100);
  }

  @Test
  public void shouldGetResultFromResultSet() throws Exception {
    when(rs.getByte("column")).thenReturn((byte) 100);
    when(rs.wasNull()).thenReturn(false);
    assertEquals(new Byte((byte) 100), TYPE_HANDLER.getResult(rs, "column"));
  }

  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getByte(1)).thenReturn((byte) 100);
    when(cs.wasNull()).thenReturn(false);
    assertEquals(new Byte((byte) 100), TYPE_HANDLER.getResult(cs, 1));
  }

}