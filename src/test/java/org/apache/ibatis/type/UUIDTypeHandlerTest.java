/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.util.UUID;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Girdhar Singh Rathore
 */
public class UUIDTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<UUID> TYPE_HANDLER = new UUIDTypeHandler();

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    UUID uuid = UUID.randomUUID();
    TYPE_HANDLER.setParameter(ps, 1, uuid, null);
    verify(ps).setString(1, uuid.toString());
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    UUID uuid = UUID.randomUUID();
    when(rs.getString("column")).thenReturn(uuid.toString());
    assertEquals(uuid, TYPE_HANDLER.getResult(rs, "column"));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getString("column")).thenReturn(null);
    assertEquals(null, TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    UUID uuid = UUID.randomUUID();
    when(rs.getString(1)).thenReturn(uuid.toString());
    assertEquals(uuid, TYPE_HANDLER.getResult(rs, 1));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    UUID uuid = UUID.randomUUID();
    when(cs.getString(1)).thenReturn(uuid.toString());
    assertEquals(uuid, TYPE_HANDLER.getResult(cs, 1));
    verify(cs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    UUID uuid = UUID.randomUUID();
    when(cs.getString(1)).thenReturn(uuid.toString());
    assertEquals(uuid, TYPE_HANDLER.getResult(cs, 1));
    verify(cs, never()).wasNull();

  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getString(1)).thenReturn(null);
    assertEquals(null, TYPE_HANDLER.getResult(cs, 1));
  }
}
