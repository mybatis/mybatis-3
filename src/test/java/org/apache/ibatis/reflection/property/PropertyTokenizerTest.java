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
package org.apache.ibatis.reflection.property;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see PropertyTokenizer
 */
class PropertyTokenizerTest {

  @Test
  void shouldParsePropertySuccessfully() {
    String fullname = "id";
    PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);

    assertEquals("id", tokenizer.getIndexedName());
    assertEquals("id", tokenizer.getName());

    assertNull(tokenizer.getChildren());
    assertNull(tokenizer.getIndex());
    assertFalse(tokenizer.hasNext());
    assertNull(tokenizer.getIndex());

    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(tokenizer::remove)
        .withMessage("Remove is not supported, as it has no meaning in the context of properties.");
  }

  @Test
  void shouldParsePropertyWhichContainsDelimSuccessfully() {
    String fullname = "person.id";
    PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);

    assertEquals("person", tokenizer.getIndexedName());
    assertEquals("person", tokenizer.getName());
    assertTrue(tokenizer.hasNext());
    assertEquals("id", tokenizer.getChildren());

    assertNull(tokenizer.getIndex());

    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(tokenizer::remove)
        .withMessage("Remove is not supported, as it has no meaning in the context of properties.");
  }

  @Test
  void shouldParsePropertyWhichContainsIndexSuccessfully() {
    String fullname = "array[0]";
    PropertyTokenizer tokenizer = new PropertyTokenizer(fullname);

    assertEquals("array[0]", tokenizer.getIndexedName());
    assertEquals("array", tokenizer.getName());
    assertEquals("0", tokenizer.getIndex());

    assertFalse(tokenizer.hasNext());
    assertNull(tokenizer.getChildren());

    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(tokenizer::remove)
        .withMessage("Remove is not supported, as it has no meaning in the context of properties.");
  }
}
