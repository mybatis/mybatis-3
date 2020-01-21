/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.submitted.typehandlerregistry_race_condition;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.util.concurrent.Futures;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeHandlerRegistryHasHandlerRaceConditionTest {

  private static final int THREAD_COUNT = 4;

  private ExecutorService executorService;

  @BeforeEach
  void setUp() {
    executorService = Executors.newFixedThreadPool(THREAD_COUNT);
  }

  @AfterEach
  void setDown() {
    executorService.shutdownNow();
  }

  @Test
  void shouldAutoRegisterEnumTypeInMultiThreadEnvironment() {
    for (int iteration = 0; iteration < 1000; iteration++) {
      TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();
      CountDownLatch startLatch = new CountDownLatch(1);

      List<Future<Object>> taskResults = IntStream.range(0, THREAD_COUNT)
        .mapToObj(taskIndex -> executorService.submit(() -> {
          startLatch.await();
          assertTrue(typeHandlerRegistry.hasTypeHandler(TestEnum.class, JdbcType.VARCHAR),
            "TypeHandler not registered");
          return null;
        })).collect(Collectors.toList());

      startLatch.countDown();
      taskResults.forEach(Futures::getUnchecked);
    }
  }

}
