package org.apache.ibatis.type;

import domain.misc.RichType;
import static org.junit.Assert.*;
import org.junit.Test;

public class TypeHandlerRegistryTest {

  private TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

  @Test
  public void shouldRegisterAndRetrieveTypeHandler() {
    TypeHandler stringTypeHandler = typeHandlerRegistry.getTypeHandler(String.class);
    typeHandlerRegistry.register(String.class, JdbcType.LONGVARCHAR, stringTypeHandler);
    assertEquals(stringTypeHandler, typeHandlerRegistry.getTypeHandler(String.class, JdbcType.LONGVARCHAR));

    assertTrue(typeHandlerRegistry.hasTypeHandler(String.class));
    assertFalse(typeHandlerRegistry.hasTypeHandler(RichType.class));
    assertTrue(typeHandlerRegistry.hasTypeHandler(String.class, JdbcType.LONGVARCHAR));
    assertTrue(typeHandlerRegistry.hasTypeHandler(String.class, JdbcType.INTEGER));
    assertTrue(typeHandlerRegistry.getUnknownTypeHandler() instanceof UnknownTypeHandler);
  }

}
