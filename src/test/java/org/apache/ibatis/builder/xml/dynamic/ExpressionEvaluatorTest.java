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
package org.apache.ibatis.builder.xml.dynamic;

import domain.blog.Author;
import domain.blog.Section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.ibatis.scripting.xmltags.ExpressionEvaluator;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ExpressionEvaluatorTest {

  private ExpressionEvaluator evaluator = new ExpressionEvaluator();

  @Test
  public void shouldCompareStringsReturnTrue() {
    boolean value = evaluator.evaluateBoolean("username == 'cbegin'", new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldCompareStringsReturnFalse() {
    boolean value = evaluator.evaluateBoolean("username == 'norm'", new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));
    assertEquals(false, value);
  }

  @Test
  public void shouldReturnTrueIfNotNull() {
    boolean value = evaluator.evaluateBoolean("username", new Author(1, "cbegin", "******", "cbegin@apache.org", "N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldReturnFalseIfNull() {
    boolean value = evaluator.evaluateBoolean("password", new Author(1, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));
    assertEquals(false, value);
  }

  @Test
  public void shouldReturnTrueIfNotZero() {
    boolean value = evaluator.evaluateBoolean("id", new Author(1, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldReturnFalseIfZero() {
    boolean value = evaluator.evaluateBoolean("id", new Author(0, "cbegin", null, "cbegin@apache.org", "N/A", Section.NEWS));
    assertEquals(false, value);
  }

  @Test
  public void shouldIterateOverIterable() {
    final HashMap<String, String[]> parameterObject = new HashMap<String, String[]>() {{
      put("array", new String[]{"1", "2", "3"});
    }};
    final Iterable<?> iterable = evaluator.evaluateIterable("array", parameterObject);
    int i = 0;
    for (Object o : iterable) {
      assertEquals(String.valueOf(++i), o);
    }
  }

  @Test
  public void concurrentEval() throws InterruptedException {
    final CountDownLatch start = new CountDownLatch(1);
    final CountDownLatch count = new CountDownLatch(100);

    final AtomicInteger errorCount = new AtomicInteger();

    final List<String> list = new ArrayList<String>();
    list.add("one");
    list.add("two");

    for (int i = 0; i < 100; i++) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            start.await();
          } catch (Exception ignored) {
          }

          for (int j = 0; j < 100 ; j++) {
            try {
              evaluator.evaluateBoolean("size() > 0", Collections.unmodifiableList(list));
            } catch (Exception e) {
              errorCount.incrementAndGet();
            }
          }

          count.countDown();
        }
      }).start();
    }

    start.countDown();
    count.await();

    assertEquals(0, errorCount.get());
  }


}
