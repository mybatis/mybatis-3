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

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.sql.Blob;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class BlobTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<byte[]> TYPE_HANDLER = new BlobTypeHandler();

  @Mock
  protected Blob blob;

  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, new byte[] { 1, 2, 3 }, null);
    verify(ps).setBinaryStream(Mockito.eq(1), Mockito.any(InputStream.class), Mockito.eq(3));
  }

  @Test
  public void shouldGetResultFromResultSet() throws Exception {
    when(rs.getBlob("column")).thenReturn(blob);
    when(rs.wasNull()).thenReturn(false);
    when(blob.length()).thenReturn(3l);
    when(blob.getBytes(1, 3)).thenReturn(new byte[] { 1, 2, 3 });
    assertArrayEquals(new byte[] { 1, 2, 3 }, TYPE_HANDLER.getResult(rs, "column"));
  }

  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getBlob(1)).thenReturn(blob);
    when(cs.wasNull()).thenReturn(false);
    when(blob.length()).thenReturn(3l);
    when(blob.getBytes(1, 3)).thenReturn(new byte[] { 1, 2, 3 });
    assertArrayEquals(new byte[] { 1, 2, 3 }, TYPE_HANDLER.getResult(cs, 1));
  }

}
