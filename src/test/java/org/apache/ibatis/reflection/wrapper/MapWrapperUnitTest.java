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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see MapWrapper
 */
class MapWrapperUnitTest extends ObjectWrapperBase {

  @Mock
  private Map<String, Object> map;

  @Mock
  private List<Integer> list;

  private MetaObject metaObject;

  private ObjectWrapper wrapper;

  @BeforeEach
  void setup() {
    this.metaObject = SystemMetaObject.forObject(map);
    this.wrapper = new MapWrapper(metaObject, map);
  }

  @Test
  @Override
  void shouldGet() {
    when(map.get("key")).thenReturn("value");

    Object value = wrapper.get(new PropertyTokenizer("key"));

    assertEquals("value", value);
    verify(map).get("key");
  }

  @Test
  void shouldNotGetWhichContainsDelim() {
    Author author = new Author(1);
    when(map.get("author")).thenReturn(author);

    Object value = wrapper.get(new PropertyTokenizer("author.id"));

    assertEquals(1, value);
  }

  @Test
  void shouldGetWhichContainsIndex() {
    when(list.get(0)).thenReturn(1);
    when(map.get("key")).thenReturn(list);

    Object value = wrapper.get(new PropertyTokenizer("key[0]"));

    assertEquals(1, value);
  }

  @Test
  @Override
  void shouldSet() {
    wrapper.set(new PropertyTokenizer("key"), "value");

    verify(map).put("key", "value");
  }

  @Test
  void shouldSetWhichContainsDelim() {
    wrapper.set(new PropertyTokenizer("author.id"), 1);

    verify(map).put("author", new HashMap<>() {
      private static final long serialVersionUID = 1L;

      {
        put("id", 1);
      }
    });
  }

  @Test
  void shouldSetWhichContainsIndex() {
    when(map.get("key")).thenReturn(list);

    wrapper.set(new PropertyTokenizer("key[0]"), 1);

    verify(list).set(0, 1);
  }

  @Test
  @Override
  void shouldFindProperty() {
    assertEquals("abc", wrapper.findProperty("abc", true));
    assertEquals("abc", wrapper.findProperty("abc", false));
  }

  @Test
  @Override
  void shouldGetGetterNames() {
    Set<String> sets = new HashSet<>() {
      private static final long serialVersionUID = 1L;

      {
        add("key1");
        add("key2");
      }
    };
    when(map.keySet()).thenReturn(sets);

    String[] getterNames = wrapper.getGetterNames();

    assertEquals(2, getterNames.length);
    assertThat(getterNames).containsExactlyInAnyOrder("key1", "key2");
  }

  @Test
  @Override
  void shouldGetSetterNames() {
    Set<String> sets = new HashSet<>() {
      private static final long serialVersionUID = 1L;

      {
        add("key1");
        add("key2");
      }
    };
    when(map.keySet()).thenReturn(sets);

    String[] setterNames = wrapper.getSetterNames();

    assertEquals(2, setterNames.length);
    assertThat(setterNames).containsExactlyInAnyOrder("key1", "key2");
  }

  @Test
  @Override
  void shouldGetGetterType() {
    when(map.get("key")).thenReturn("abc");

    Class<?> type = wrapper.getGetterType("key");

    assertEquals(String.class, type);
  }

  @Test
  void shouldGetGetterTypeWhichContainsIndex() {
    when(map.get("key")).thenReturn(list);

    Class<?> collectionType = wrapper.getGetterType("key");
    Class<?> type = wrapper.getGetterType("key[0]");

    assertEquals(list.getClass(), collectionType);
    assertEquals(Object.class, type);
  }

  @Test
  @Override
  void shouldGetSetterType() {
    when(map.get("key")).thenReturn("abc");

    Class<?> type = wrapper.getSetterType("key");

    assertEquals(String.class, type);
  }

  @Test
  void shouldGetSetterTypeWhichContainsIndex() {
    when(map.get("key")).thenReturn(list);

    Class<?> collectionType = wrapper.getSetterType("key");
    Class<?> type = wrapper.getSetterType("key[0]");

    assertEquals(list.getClass(), collectionType);
    assertEquals(Object.class, type);
  }

  @Test
  @Override
  void shouldHasGetter() {
    when(map.containsKey("key")).thenReturn(false);

    assertFalse(wrapper.hasGetter("key"));

    when(map.containsKey("key")).thenReturn(true);

    assertTrue(wrapper.hasGetter("key"));
  }

  @Test
  @Override
  void shouldHasSetter() {
    assertTrue(wrapper.hasSetter("abc"));
  }

  @Test
  @Override
  void shouldIsCollection() {
    assertFalse(wrapper.isCollection());
  }

  @Test
  @Override
  void shouldInstantiatePropertyValue() {
    MetaObject result = wrapper.instantiatePropertyValue("abc", new PropertyTokenizer("key"),
        SystemMetaObject.DEFAULT_OBJECT_FACTORY);

    assertFalse(result.hasGetter("key"));
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
