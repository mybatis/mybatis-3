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
package org.apache.ibatis.reflection;

import static org.junit.Assert.*;

import org.apache.ibatis.domain.misc.RichType;
import org.apache.ibatis.domain.misc.generics.GenericConcrete;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaClassTest {

  private RichType rich = new RichType();
  Map<String,RichType> map = new HashMap<String,RichType>() {
    {
      put("richType", rich);
    }
  };

  public MetaClassTest() {
    rich.setRichType(new RichType());
  }

  @Test
  public void shouldTestDataTypeOfGenericMethod() {
    MetaClass meta = MetaClass.forClass(GenericConcrete.class);
    assertEquals(Long.class, meta.getGetterType("id"));
    assertEquals(Long.class, meta.getSetterType("id"));
  }

  @Test
  public void shouldCheckGetterExistance() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    assertTrue(meta.hasGetter("richField"));
    assertTrue(meta.hasGetter("richProperty"));
    assertTrue(meta.hasGetter("richList"));
    assertTrue(meta.hasGetter("richMap"));
    assertTrue(meta.hasGetter("richList[0]"));

    assertTrue(meta.hasGetter("richType"));
    assertTrue(meta.hasGetter("richType.richField"));
    assertTrue(meta.hasGetter("richType.richProperty"));
    assertTrue(meta.hasGetter("richType.richList"));
    assertTrue(meta.hasGetter("richType.richMap"));
    assertTrue(meta.hasGetter("richType.richList[0]"));

    assertFalse(meta.hasGetter("[0]"));
  }

  @Test
  public void shouldCheckSetterExistance() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    assertTrue(meta.hasSetter("richField"));
    assertTrue(meta.hasSetter("richProperty"));
    assertTrue(meta.hasSetter("richList"));
    assertTrue(meta.hasSetter("richMap"));
    assertTrue(meta.hasSetter("richList[0]"));

    assertTrue(meta.hasSetter("richType"));
    assertTrue(meta.hasSetter("richType.richField"));
    assertTrue(meta.hasSetter("richType.richProperty"));
    assertTrue(meta.hasSetter("richType.richList"));
    assertTrue(meta.hasSetter("richType.richMap"));
    assertTrue(meta.hasSetter("richType.richList[0]"));

    assertFalse(meta.hasSetter("[0]"));
  }

  @Test
  public void shouldCheckTypeForEachGetter() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    assertEquals(String.class, meta.getGetterType("richField"));
    assertEquals(String.class, meta.getGetterType("richProperty"));
    assertEquals(List.class, meta.getGetterType("richList"));
    assertEquals(Map.class, meta.getGetterType("richMap"));
    assertEquals(List.class, meta.getGetterType("richList[0]"));

    assertEquals(RichType.class, meta.getGetterType("richType"));
    assertEquals(String.class, meta.getGetterType("richType.richField"));
    assertEquals(String.class, meta.getGetterType("richType.richProperty"));
    assertEquals(List.class, meta.getGetterType("richType.richList"));
    assertEquals(Map.class, meta.getGetterType("richType.richMap"));
    assertEquals(List.class, meta.getGetterType("richType.richList[0]"));
  }

  @Test
  public void shouldCheckTypeForEachSetter() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    assertEquals(String.class, meta.getSetterType("richField"));
    assertEquals(String.class, meta.getSetterType("richProperty"));
    assertEquals(List.class, meta.getSetterType("richList"));
    assertEquals(Map.class, meta.getSetterType("richMap"));
    assertEquals(List.class, meta.getSetterType("richList[0]"));

    assertEquals(RichType.class, meta.getSetterType("richType"));
    assertEquals(String.class, meta.getSetterType("richType.richField"));
    assertEquals(String.class, meta.getSetterType("richType.richProperty"));
    assertEquals(List.class, meta.getSetterType("richType.richList"));
    assertEquals(Map.class, meta.getSetterType("richType.richMap"));
    assertEquals(List.class, meta.getSetterType("richType.richList[0]"));
  }

  @Test
  public void shouldCheckGetterAndSetterNames() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    assertEquals(5, meta.getGetterNames().length);
    assertEquals(5, meta.getSetterNames().length);
  }

  @Test
  public void shouldFindPropertyName() {
    MetaClass meta = MetaClass.forClass(RichType.class);
    assertEquals("richField", meta.findProperty("RICHfield"));
  }

}
