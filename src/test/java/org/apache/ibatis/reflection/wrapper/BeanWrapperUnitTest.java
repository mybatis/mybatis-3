/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection.wrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.domain.misc.RichType;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see BeanWrapper
 */
class BeanWrapperUnitTest extends ObjectWrapperBase {

  private RichType richType;

  private ObjectWrapper wrapper;

  @BeforeEach
  void setup() {
    this.richType = new RichType();
    this.wrapper = new BeanWrapper(SystemMetaObject.forObject(richType), richType);
  }

  @Test
  @Override
  void shouldGet() {
    richType.setRichProperty("mybatis");

    Object value = wrapper.get(new PropertyTokenizer("richProperty"));

    assertEquals("mybatis", value);
  }

  @Test
  void shouldGetWhichContainsDelim() {
    RichType nested = new RichType();
    nested.setRichProperty("mybatis");
    richType.setRichType(nested);

    Object value = wrapper.get(new PropertyTokenizer("richType.richProperty"));

    assertEquals("mybatis", value);
  }

  @Test
  void shouldGetWhichContainsIndex() {
    richType.setRichList(Arrays.asList(1L, "abc"));
    richType.setRichMap(new HashMap<String, Object>() {
      private static final long serialVersionUID = 1L;

      {
        put("key1", "value1");
        put("key2", "value2");
      }
    });

    assertEquals("abc", wrapper.get(new PropertyTokenizer("richList[1]")));
    assertEquals("value2", wrapper.get(new PropertyTokenizer("richMap[key2]")));

  }

  @Test
  @Override
  void shouldSet() {
    wrapper.set(new PropertyTokenizer("richProperty"), "mybatis");

    assertEquals("mybatis", richType.getRichProperty());
  }

  @Test
  void shouldSetWhichContainsDelim() {
    wrapper.set(new PropertyTokenizer("richType.richProperty"), "mybatis");

    assertEquals("mybatis", richType.getRichType().getRichProperty());
  }

  @Test
  void shouldSetWhichContainsIndex() {
    List<Object> list = Arrays.asList(1L, 2L);
    richType.setRichList(list);

    wrapper.set(new PropertyTokenizer("richList[0]"), "mybatis");

    assertEquals("mybatis", list.get(0));
  }

  @Test
  @Override
  void shouldFindProperty() {
    String property = wrapper.findProperty("richProperty", false);

    assertEquals("richProperty", property);
  }

  @Test
  void shouldFindPropertyContainsDelim() {
    String property = wrapper.findProperty("richType.richProperty", false);

    assertEquals("richType.richProperty", property);
  }

  @Test
  void shouldFindPropertyContainsIndex() {
    String property = wrapper.findProperty("richList[0]", false);

    assertNull(property);
  }

  @Test
  @Override
  void shouldGetGetterNames() {
    String[] getterNames = wrapper.getGetterNames();

    assertThat(getterNames).containsExactlyInAnyOrder("richType", "richProperty", "richList", "richMap", "richField");
  }

  @Test
  @Override
  void shouldGetSetterNames() {
    String[] setterNames = wrapper.getSetterNames();

    assertThat(setterNames).containsExactlyInAnyOrder("richType", "richProperty", "richList", "richMap", "richField");
  }

  @Test
  @Override
  void shouldGetGetterType() {
    assertEquals(RichType.class, wrapper.getGetterType("richType"));
    assertEquals(String.class, wrapper.getGetterType("richField"));
    assertEquals(String.class, wrapper.getGetterType("richProperty"));
    assertEquals(Map.class, wrapper.getGetterType("richMap"));
    assertEquals(List.class, wrapper.getGetterType("richList"));
  }

  @Test
  @Override
  void shouldGetSetterType() {
    assertEquals(RichType.class, wrapper.getSetterType("richType"));
    assertEquals(String.class, wrapper.getSetterType("richField"));
    assertEquals(String.class, wrapper.getSetterType("richProperty"));
    assertEquals(Map.class, wrapper.getSetterType("richMap"));
    assertEquals(List.class, wrapper.getSetterType("richList"));
  }

  @Test
  @Override
  void shouldHasGetter() {
    assertTrue(wrapper.hasGetter("richType"));
    assertTrue(wrapper.hasGetter("richField"));
    assertTrue(wrapper.hasGetter("richProperty"));
    assertTrue(wrapper.hasGetter("richMap"));
    assertTrue(wrapper.hasGetter("richList"));
  }

  @Test
  @Override
  void shouldHasSetter() {
    assertTrue(wrapper.hasGetter("richType"));
    assertTrue(wrapper.hasGetter("richField"));
    assertTrue(wrapper.hasGetter("richProperty"));
    assertTrue(wrapper.hasGetter("richMap"));
    assertTrue(wrapper.hasGetter("richList"));
  }

  @Test
  @Override
  void shouldIsCollection() {
    assertFalse(wrapper.isCollection());
  }

  @Test
  @Override
  void shouldInstantiatePropertyValue() {
    // Nothing
  }

  @Test
  @Override
  void shouldAddElement() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.add("1"));
  }

  @Test
  @Override
  void shouldAddAll() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.addAll(new ArrayList<>()));
  }

}
