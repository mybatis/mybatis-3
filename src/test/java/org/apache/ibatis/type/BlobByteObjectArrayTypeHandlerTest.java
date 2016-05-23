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

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.*;
import java.sql.Blob;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class BlobByteObjectArrayTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Byte[]> TYPE_HANDLER = new BlobByteObjectArrayTypeHandler();

  @Mock
  protected Blob blob;

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    final ArgumentCaptor<Integer> positionCaptor = ArgumentCaptor.forClass(Integer.class);
    final ArgumentCaptor<ByteArrayInputStream> byteArrayCaptor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
    final ArgumentCaptor<Integer> lengthCaptor = ArgumentCaptor.forClass(Integer.class);
    doNothing().when(ps).setBinaryStream(positionCaptor.capture(), byteArrayCaptor.capture(), lengthCaptor.capture());
    TYPE_HANDLER.setParameter(ps, 1, new Byte[]{1, 2}, null);
    ByteArrayInputStream actualIn = byteArrayCaptor.getValue();
    assertThat(positionCaptor.getValue(), is(1));
    assertThat(actualIn.read(), is(1));
    assertThat(actualIn.read(), is(2));
    assertThat(actualIn.read(), is(-1));
    assertThat(lengthCaptor.getValue(), is(2));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    byte[] byteArray = new byte[]{1, 2};
    when(rs.getBlob("column")).thenReturn(blob);
    when(rs.wasNull()).thenReturn(false);
    when(blob.length()).thenReturn((long)byteArray.length);
    when(blob.getBytes(1, 2)).thenReturn(byteArray);
    assertThat(TYPE_HANDLER.getResult(rs, "column"), is(new Byte[]{1, 2}));

  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getBlob("column")).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertThat(TYPE_HANDLER.getResult(rs, "column"), nullValue());
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    byte[] byteArray = new byte[]{1, 2};
    when(rs.getBlob(1)).thenReturn(blob);
    when(rs.wasNull()).thenReturn(false);
    when(blob.length()).thenReturn((long)byteArray.length);
    when(blob.getBytes(1, 2)).thenReturn(byteArray);
    assertThat(TYPE_HANDLER.getResult(rs, 1), is(new Byte[]{1, 2}));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getBlob(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertThat(TYPE_HANDLER.getResult(rs, 1), nullValue());
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    byte[] byteArray = new byte[]{1, 2};
    when(cs.getBlob(1)).thenReturn(blob);
    when(cs.wasNull()).thenReturn(false);
    when(blob.length()).thenReturn((long)byteArray.length);
    when(blob.getBytes(1, 2)).thenReturn(byteArray);
    assertThat(TYPE_HANDLER.getResult(cs, 1), is(new Byte[]{1, 2}));
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getBlob(1)).thenReturn(null);
    when(cs.wasNull()).thenReturn(true);
    assertThat(TYPE_HANDLER.getResult(cs, 1), nullValue());
  }

}