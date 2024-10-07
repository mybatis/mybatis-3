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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.ibatis.type.BaseTypeHandlerTest;
import org.apache.ibatis.type.StringBasedOffsetDateTimeTypeHandler;
import org.apache.ibatis.type.TypeHandler;
import org.junit.Test;

public class StringBasedOffsetDateTimeTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<OffsetDateTime> TYPE_HANDLER = new StringBasedOffsetDateTimeTypeHandler();
  private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.of(2018, 5, 8, 9, 47, 55, 0, ZoneOffset.ofHours(13));
  private static final String OFFSET_DATE_TIME_TO_CHAR = OFFSET_DATE_TIME.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, OFFSET_DATE_TIME, null);
    verify(ps).setString(1, OFFSET_DATE_TIME_TO_CHAR);
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    when(rs.getString("column")).thenReturn(OFFSET_DATE_TIME_TO_CHAR);
    assertEquals(OFFSET_DATE_TIME, TYPE_HANDLER.getResult(rs, "column"));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getString("column")).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, "column"));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getString(1)).thenReturn(OFFSET_DATE_TIME_TO_CHAR);
    assertEquals(OFFSET_DATE_TIME, TYPE_HANDLER.getResult(rs, 1));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getString(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
    verify(rs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getString(1)).thenReturn(OFFSET_DATE_TIME_TO_CHAR);
    assertEquals(OFFSET_DATE_TIME, TYPE_HANDLER.getResult(cs, 1));
    verify(cs, never()).wasNull();
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getString(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
    verify(cs, never()).wasNull();
  }
}
