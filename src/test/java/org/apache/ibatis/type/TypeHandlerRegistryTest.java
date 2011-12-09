/*
 *    Copyright 2009-2011 The MyBatis Team
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
