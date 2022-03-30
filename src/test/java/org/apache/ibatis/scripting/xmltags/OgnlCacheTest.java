/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.scripting.xmltags;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class OgnlCacheTest {
  @Test
  void concurrentAccess() throws Exception {
    class DataClass {
      @SuppressWarnings("unused")
      private int id;
    }
    int run = 1000;
    Map<String, Object> context = new HashMap<>();
    List<Future<Object>> futures = new ArrayList<>();
    context.put("data", new DataClass());
    ExecutorService executor = Executors.newCachedThreadPool();
    IntStream.range(0, run).forEach(i -> {
      futures.add(executor.submit(() -> {
        return OgnlCache.getValue("data.id", context);
      }));
    });
    for (int i = 0; i < run; i++) {
      assertNotNull(futures.get(i).get());
    }
    executor.shutdown();
  }
}
