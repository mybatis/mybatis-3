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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class ByteObjectArrayTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Byte[]> TYPE_HANDLER = new ByteObjectArrayTypeHandler();

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, new Byte[] { 1, 2, 3 }, null);
    verify(ps).setBytes(1, new byte[] { 1, 2, 3 });
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    byte[] byteArray = new byte[] { 1, 2 };
    when(rs.getBytes("column")).thenReturn(byteArray);
    assertThat(TYPE_HANDLER.getResult(rs, "column")).isEqualTo(new Byte[]{1, 2});
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getBytes("column")).thenReturn(null);
    assertThat(TYPE_HANDLER.getResult(rs, "column")).isNull();
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    byte[] byteArray = new byte[] { 1, 2 };
    when(rs.getBytes(1)).thenReturn(byteArray);
    assertThat(TYPE_HANDLER.getResult(rs, 1)).isEqualTo(new Byte[]{1, 2});
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getBytes(1)).thenReturn(null);
    assertThat(TYPE_HANDLER.getResult(rs, 1)).isNull();
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    byte[] byteArray = new byte[] { 1, 2 };
    when(cs.getBytes(1)).thenReturn(byteArray);
    assertThat(TYPE_HANDLER.getResult(cs, 1)).isEqualTo(new Byte[]{1, 2});
    verify(cs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getBytes(1)).thenReturn(null);
    assertThat(TYPE_HANDLER.getResult(cs, 1)).isNull();
    verify(cs, never()).wasNull();
  }

}
