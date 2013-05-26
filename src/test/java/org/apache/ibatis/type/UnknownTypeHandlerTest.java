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

public class UnknownTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Object> TYPE_HANDLER = new UnknownTypeHandler(new TypeHandlerRegistry());

  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, "Hello", null);
    verify(ps).setString(1, "Hello");
  }

  @Test
  public void shouldGetResultFromResultSet() throws Exception {
    when(rs.getMetaData()).thenReturn(rsmd);
    when(rsmd.getColumnCount()).thenReturn(1);
    when(rsmd.getColumnName(1)).thenReturn("column");
    when(rsmd.getColumnClassName(1)).thenReturn(String.class.getName());
    when(rsmd.getColumnType(1)).thenReturn(JdbcType.VARCHAR.TYPE_CODE);
    when(rs.getString("column")).thenReturn("Hello");
    when(rs.wasNull()).thenReturn(false);
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, "column"));
  }

  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getObject(1)).thenReturn("Hello");
    when(cs.wasNull()).thenReturn(false);
    assertEquals("Hello", TYPE_HANDLER.getResult(cs, 1));
  }

}