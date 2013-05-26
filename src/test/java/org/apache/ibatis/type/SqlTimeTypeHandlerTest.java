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

import java.util.Date;

import org.junit.Test;

public class SqlTimeTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<java.sql.Time> TYPE_HANDLER = new SqlTimeTypeHandler();
  private static final java.sql.Time SQL_TIME = new java.sql.Time(new Date().getTime());

  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, SQL_TIME, null);
    verify(ps).setTime(1, SQL_TIME);
  }

  @Test
  public void shouldGetResultFromResultSet() throws Exception {
    when(rs.getTime("column")).thenReturn(SQL_TIME);
    when(rs.wasNull()).thenReturn(false);
    assertEquals(SQL_TIME, TYPE_HANDLER.getResult(rs, "column"));
  }

  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getTime(1)).thenReturn(SQL_TIME);
    when(cs.wasNull()).thenReturn(false);
    assertEquals(SQL_TIME, TYPE_HANDLER.getResult(cs, 1));
  }

}