/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.builder.xml.dynamic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class DynamicContextTest {

  private final Configuration config = new Configuration();

  @Test
  void shouldCompareStringsReturnTrue() {
    DynamicContext dynamicContext = new DynamicContext(config,
        new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS), Author.class, null, true);
    boolean value = dynamicContext.evaluateBoolean("username == 'cbegin'");
    assertTrue(value);
  }

  @Test
  void shouldCompareStringsReturnFalse() {
    DynamicContext dynamicContext = new DynamicContext(config,
        new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS), Author.class, null, true);
    boolean value = dynamicContext.evaluateBoolean("username == 'norm'");
    assertFalse(value);
  }

  @Test
  void shouldReturnTrueIfNotNull() {
    DynamicContext dynamicContext = new DynamicContext(config,
        new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS), Author.class, null, true);
    boolean value = dynamicContext.evaluateBoolean("username");
    assertTrue(value);
  }

  @Test
  void shouldReturnFalseIfNull() {
    DynamicContext dynamicContext = new DynamicContext(config,
        new Author(1, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS), Author.class, null, true);
    boolean value = dynamicContext.evaluateBoolean("password");
    assertFalse(value);
  }

  @Test
  void shouldReturnTrueIfNotZero() {
    DynamicContext dynamicContext = new DynamicContext(config,
        new Author(1, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS), Author.class, null, true);
    boolean value = dynamicContext.evaluateBoolean("id");
    assertTrue(value);
  }

  @Test
  void shouldReturnFalseIfZero() {
    DynamicContext dynamicContext = new DynamicContext(config,
        new Author(0, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS), Author.class, null, true);
    boolean value = dynamicContext.evaluateBoolean("id");
    assertFalse(value);
  }

  @Test
  void shouldReturnFalseIfZeroWithScale() {
    class Bean {
      @SuppressWarnings("unused")
      public double d = 0.0D;
    }
    DynamicContext dynamicContext = new DynamicContext(config, new Bean(), Bean.class, null, true);
    assertFalse(dynamicContext.evaluateBoolean("d"));
  }

  @Test
  void shouldIterateOverIterable() {
    final HashMap<String, String[]> parameterObject = new HashMap<>() {
      private static final long serialVersionUID = 1L;
      {
        put("array", new String[] { "1", "2", "3" });
      }
    };
    DynamicContext dynamicContext = new DynamicContext(config, parameterObject, HashMap.class, null, true);
    final Iterable<?> iterable = dynamicContext.evaluateIterable("array", false);
    int i = 0;
    for (Object o : iterable) {
      i++;
      assertEquals(String.valueOf(i), o);
    }
  }

}
