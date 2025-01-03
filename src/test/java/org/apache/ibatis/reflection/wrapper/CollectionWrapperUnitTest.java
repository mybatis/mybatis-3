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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see CollectionWrapper
 */
@ExtendWith(MockitoExtension.class)
class CollectionWrapperUnitTest extends ObjectWrapperBase {

  @Mock
  private Collection<Object> collection;

  @Mock
  private PropertyTokenizer tokenizer;

  private ObjectWrapper wrapper;

  @BeforeEach
  void setup() {
    MetaObject metaObject = SystemMetaObject.forObject(collection);
    this.wrapper = new CollectionWrapper(metaObject, collection);
  }

  @Test
  @Override
  void shouldGet() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.get(tokenizer));
  }

  @Test
  @Override
  void shouldSet() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.set(tokenizer, null));
  }

  @Test
  @Override
  void shouldFindProperty() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.findProperty("abc", true));
  }

  @Test
  @Override
  void shouldGetGetterNames() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.getGetterNames());
  }

  @Test
  @Override
  void shouldGetSetterNames() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.getSetterNames());
  }

  @Test
  @Override
  void shouldGetGetterType() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.getGetterType("abc"));
  }

  @Test
  @Override
  void shouldGetSetterType() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.getSetterType("abc"));
  }

  @Test
  @Override
  void shouldHasGetter() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.hasGetter("abc"));
  }

  @Test
  @Override
  void shouldHasSetter() {
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> wrapper.hasSetter("abc"));
  }

  @Test
  @Override
  void shouldIsCollection() {
    assertTrue(wrapper.isCollection());
  }

  @Test
  @Override
  void shouldInstantiatePropertyValue() {
    assertThatExceptionOfType(UnsupportedOperationException.class)
        .isThrownBy(() -> wrapper.instantiatePropertyValue("abc", tokenizer, null));
  }

  @Test
  @Override
  void shouldAddElement() {
    wrapper.add("bdc");

    verify(collection).add("bdc");
  }

  @Test
  @Override
  void shouldAddAll() {
    List<Object> list = new ArrayList<>() {
      private static final long serialVersionUID = 1L;

      {
        add("1");
        add("2");
        add("3");
      }
    };
    wrapper.addAll(list);

    verify(collection).addAll(list);
  }
}
