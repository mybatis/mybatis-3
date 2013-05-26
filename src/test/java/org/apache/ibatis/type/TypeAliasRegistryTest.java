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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.math.BigDecimal;

public class TypeAliasRegistryTest {

  @Test
  public void shouldRegisterAndResolveTypeAlias() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    typeAliasRegistry.registerAlias("rich", "domain.misc.RichType");

    assertEquals("domain.misc.RichType", typeAliasRegistry.resolveAlias("rich").getName());
  }

  @Test
  public void shouldFetchArrayType() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    assertEquals(Byte[].class, typeAliasRegistry.resolveAlias("byte[]"));
  }

  @Test
  public void shouldBeAbleToRegisterSameAliasWithSameTypeAgain() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    typeAliasRegistry.registerAlias("String", String.class);
    typeAliasRegistry.registerAlias("string", String.class);
  }

  @Test(expected = TypeException.class)
  public void shouldNotBeAbleToRegisterSameAliasWithDifferentType() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    typeAliasRegistry.registerAlias("string", BigDecimal.class);
  }

  @Test
  public void shouldBeAbleToRegisterAliasWithNullType() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    typeAliasRegistry.registerAlias("foo", (Class<?>) null);
    assertNull(typeAliasRegistry.resolveAlias("foo"));
  }

  @Test
  public void shouldBeAbleToRegisterNewTypeIfRegisteredTypeIsNull() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    typeAliasRegistry.registerAlias("foo", (Class<?>) null);
    typeAliasRegistry.registerAlias("foo", String.class);
  }

}
