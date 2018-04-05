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
package org.apache.ibatis.type.usesjava8;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Time;
import java.time.OffsetTime;

import org.apache.ibatis.type.BaseTypeHandlerTest;
import org.apache.ibatis.type.OffsetTimeTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.junit.Test;

public class OffsetTimeTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<OffsetTime> TYPE_HANDLER = new OffsetTimeTypeHandler();
  // java.sql.Time doesn't contain millis, so set nano to 0
  private static final OffsetTime OFFSET_TIME = OffsetTime.now().withNano(0);
  private static final Time TIME = Time.valueOf(OFFSET_TIME.toLocalTime());

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, OFFSET_TIME, null);
    verify(ps).setTime(1, TIME);
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    when(rs.getTime("column")).thenReturn(TIME);
    assertEquals(OFFSET_TIME, TYPE_HANDLER.getResult(rs, "column"));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getTime("column")).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, "column"));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getTime(1)).thenReturn(TIME);
    assertEquals(OFFSET_TIME, TYPE_HANDLER.getResult(rs, 1));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getTime(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getTime(1)).thenReturn(TIME);
    assertEquals(OFFSET_TIME, TYPE_HANDLER.getResult(cs, 1));
    verify(cs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getTime(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
    verify(cs, never()).wasNull();
  }
}
