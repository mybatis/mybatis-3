/*
 *    Copyright 2009-2023 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.scripting.xmltags.ExpressionEvaluator;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorTest {

  private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

  @Test
  void shouldCompareStringsReturnTrue() {
    boolean value = evaluator.evaluateBoolean("username == 'cbegin'",
        new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));
    assertTrue(value);
  }

  @Test
  void shouldCompareStringsReturnFalse() {
    boolean value = evaluator.evaluateBoolean("username == 'norm'",
        new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));
    assertFalse(value);
  }

  @Test
  void shouldReturnTrueIfNotNull() {
    boolean value = evaluator.evaluateBoolean("username",
        new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));
    assertTrue(value);
  }

  @Test
  void shouldReturnFalseIfNull() {
    boolean value = evaluator.evaluateBoolean("password",
        new Author(1, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));
    assertFalse(value);
  }

  @Test
  void shouldReturnTrueIfNotZero() {
    boolean value = evaluator.evaluateBoolean("id",
        new Author(1, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));
    assertTrue(value);
  }

  @Test
  void shouldReturnFalseIfZero() {
    boolean value = evaluator.evaluateBoolean("id",
        new Author(0, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));
    assertFalse(value);
  }

  @Test
  void shouldReturnFalseIfZeroWithScale() {
    class Bean {
      @SuppressWarnings("unused")
      public double d = 0.0d;
    }
    assertFalse(evaluator.evaluateBoolean("d", new Bean()));
  }

  @Test
  void shouldIterateOverIterable() {
    final HashMap<String, String[]> parameterObject = new HashMap<>() {
      private static final long serialVersionUID = 1L;
      {
        put("array", new String[] { "1", "2", "3" });
      }
    };
    final Iterable<?> iterable = evaluator.evaluateIterable("array", parameterObject);
    int i = 0;
    for (Object o : iterable) {
      i++;
      assertEquals(String.valueOf(i), o);
    }
  }

  @Test
  public void shouldReturnFalseIfEmptyCollection() {
    Map<String, Object> ctx = new HashMap<>();
    ctx.put("c1", Collections.emptyList());
    ctx.put("c2", Collections.emptySet());
    assertFalse(evaluator.evaluateBoolean("c1", ctx));
    assertFalse(evaluator.evaluateBoolean("c2", ctx));
  }

  @Test
  public void shouldReturnFalseIfEmptyArray() {
    Map<String, Object> ctx = new HashMap<>();
    ctx.put("array1", new int[] {});
    ctx.put("array2", new short[] {});
    ctx.put("array3", new byte[] {});
    ctx.put("array4", new float[] {});
    ctx.put("array5", new double[] {});
    ctx.put("array6", new byte[] {});
    ctx.put("array7", new char[] {});
    ctx.put("array8", new Object[] {});

    assertFalse(evaluator.evaluateBoolean("array1", ctx));
    assertFalse(evaluator.evaluateBoolean("array2", ctx));
    assertFalse(evaluator.evaluateBoolean("array3", ctx));
    assertFalse(evaluator.evaluateBoolean("array4", ctx));
    assertFalse(evaluator.evaluateBoolean("array5", ctx));
    assertFalse(evaluator.evaluateBoolean("array6", ctx));
    assertFalse(evaluator.evaluateBoolean("array7", ctx));
    assertFalse(evaluator.evaluateBoolean("array8", ctx));
  }

  @Test
  public void shouldReturnFalseIfBlankStr() {
    Map<String, Object> ctx = new HashMap<>();
    ctx.put("c1", "");
    ctx.put("c2", " ");
    ctx.put("c3", "\t");
    assertFalse(evaluator.evaluateBoolean("c1", ctx));
    assertFalse(evaluator.evaluateBoolean("c2", ctx));
    assertFalse(evaluator.evaluateBoolean("c3", ctx));
  }
}
