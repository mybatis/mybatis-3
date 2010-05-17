package org.apache.ibatis.jdbc;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.StringTypeHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class NullTest {

  @Test
  public void shouldGetTypeAndTypeHandlerForNullStringType() {
    assertEquals(JdbcType.VARCHAR, Null.STRING.getJdbcType());
    assertTrue(Null.STRING.getTypeHandler() instanceof StringTypeHandler);
  }

}
