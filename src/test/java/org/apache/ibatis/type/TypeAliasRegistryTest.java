/**
 *    Copyright 2009-2017 the original author or authors.
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

import static com.googlecode.catchexception.apis.BDDCatchException.caughtException;
import static com.googlecode.catchexception.apis.BDDCatchException.when;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.apache.ibatis.builder.CustomTypeAliasGenerator;
import org.apache.ibatis.domain.dummy.Bar;
import org.apache.ibatis.domain.dummy.Foo;
import org.junit.Test;

public class TypeAliasRegistryTest {

  @Test
  public void shouldRegisterAndResolveTypeAlias() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    typeAliasRegistry.registerAlias("rich", "org.apache.ibatis.domain.misc.RichType");

    assertEquals("org.apache.ibatis.domain.misc.RichType", typeAliasRegistry.resolveAlias("rich").getName());
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
  
  @Test
  public void shouldBeSimpleClassNameIfTypeAliasGeneratorIsNotSpecified() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    
    typeAliasRegistry.registerAlias(Foo.class);
    typeAliasRegistry.registerAlias(Bar.class);
    assertEquals(Foo.class, typeAliasRegistry.getTypeAliases().get("foo"));
    assertEquals(Bar.class, typeAliasRegistry.getTypeAliases().get("bar"));
    
    typeAliasRegistry.registerAlias(Foo.Type.class);
    assertEquals(Foo.Type.class, typeAliasRegistry.getTypeAliases().get("type"));
    
    when(typeAliasRegistry).registerAlias(Bar.Type.class);
    
    then(caughtException())
    .isInstanceOf(TypeException.class)
    .hasMessageContaining("The alias 'Type' is already mapped to the value");
  }
  
  @Test
  public void shouldBeAbleToRegisterDifferentNestedTypesHavingSameSimpleNameUsingCustomTypeAliasGenerator() {
    TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    typeAliasRegistry.setTypeAliasGenerator(CustomTypeAliasGenerator.class);
    
    typeAliasRegistry.registerAlias(Foo.class);
    typeAliasRegistry.registerAlias(Bar.class);
    assertEquals(Foo.class, typeAliasRegistry.getTypeAliases().get("foo"));
    assertEquals(Bar.class, typeAliasRegistry.getTypeAliases().get("bar"));
    
    typeAliasRegistry.registerAlias(Foo.Type.class);
    typeAliasRegistry.registerAlias(Bar.Type.class);
    assertNull(typeAliasRegistry.getTypeAliases().get("type"));
    assertEquals(Foo.Type.class, typeAliasRegistry.getTypeAliases().get("foo$type"));
    assertEquals(Bar.Type.class, typeAliasRegistry.getTypeAliases().get("bar$type"));
  }
}
