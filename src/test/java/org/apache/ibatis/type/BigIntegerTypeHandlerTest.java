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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

public class BigIntegerTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<BigInteger> TYPE_HANDLER = new BigIntegerTypeHandler();

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, new BigInteger("707070656505050302797979792923232303"), null);
    verify(ps).setBigDecimal(1, new BigDecimal("707070656505050302797979792923232303"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    when(rs.getBigDecimal("column")).thenReturn(new BigDecimal("707070656505050302797979792923232303"));
    when(rs.wasNull()).thenReturn(false);
    assertEquals(new BigInteger("707070656505050302797979792923232303"), TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getBigDecimal("column")).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getBigDecimal(1)).thenReturn(new BigDecimal("707070656505050302797979792923232303"));
    when(rs.wasNull()).thenReturn(false);
    assertEquals(new BigInteger("707070656505050302797979792923232303"), TYPE_HANDLER.getResult(rs,1 ));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getBigDecimal(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs,1 ));
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getBigDecimal(1)).thenReturn(new BigDecimal("707070656505050302797979792923232303"));
    when(cs.wasNull()).thenReturn(false);
    assertEquals(new BigInteger("707070656505050302797979792923232303"), TYPE_HANDLER.getResult(cs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getBigDecimal(1)).thenReturn(null);
    when(cs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
  }

}
