package org.apache.ibatis.type;

import org.apache.ibatis.builder.CustomLongTypeHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CostomLongTypeHandlerTest extends BaseTypeHandlerTest{
  private static final TypeHandler<Long> TYPE_HANDLER = new CustomLongTypeHandler();

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, 100L, null);
    verify(ps).setLong(1, 100L);
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    when(rs.getLong("column")).thenReturn(100L, 0L);
    assertEquals(Long.valueOf(100L), TYPE_HANDLER.getResult(rs, "column"));
    assertEquals(Long.valueOf(0L), TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getLong("column")).thenReturn(0L);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getLong(1)).thenReturn(100L, 0L);
    assertEquals(Long.valueOf(100L), TYPE_HANDLER.getResult(rs, 1));
    assertEquals(Long.valueOf(0L), TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getLong(1)).thenReturn(0L);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getLong(1)).thenReturn(100L, 0L);
    assertEquals(Long.valueOf(100L), TYPE_HANDLER.getResult(cs, 1));
    assertEquals(Long.valueOf(0L), TYPE_HANDLER.getResult(cs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getLong(1)).thenReturn(0L);
    when(cs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
  }
}
