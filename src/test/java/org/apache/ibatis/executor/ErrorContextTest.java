/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ErrorContextTest {

  private static final String SOME_FILE_XML = "somefile.xml";
  private static final String SOME_ACTIVITY = "some activity";
  private static final String SOME_OBJECT = "some object";
  private static final String MORE_INFO = "Here's more info.";
  private static final String EXCEPTION_MESSAGE = "test";

  @Test
  void shouldShowProgressiveErrorContextBuilding() {
    ErrorContext context = ErrorContext.instance();

    context.resource(SOME_FILE_XML).activity(SOME_ACTIVITY).object(SOME_OBJECT).message(MORE_INFO);
    String contextString = context.toString();
    Assertions.assertTrue(contextString.contains("### " + MORE_INFO));
    Assertions.assertTrue(contextString.contains("### The error may exist in " + SOME_FILE_XML));
    Assertions.assertTrue(contextString.contains("### The error may involve " + SOME_OBJECT));
    Assertions.assertTrue(contextString.contains("### The error occurred while " + SOME_ACTIVITY));
    context.reset();

    context.activity(SOME_ACTIVITY).object(SOME_OBJECT).message(MORE_INFO);
    contextString = context.toString();
    Assertions.assertTrue(contextString.contains("### " + MORE_INFO));
    Assertions.assertTrue(contextString.contains("### The error occurred while " + SOME_ACTIVITY));
    context.reset();

    context.object(SOME_OBJECT).message(MORE_INFO);
    contextString = context.toString();
    Assertions.assertTrue(contextString.contains("### " + MORE_INFO));
    Assertions.assertTrue(contextString.contains("### The error may involve " + SOME_OBJECT));
    context.reset();

    context.message(MORE_INFO);
    contextString = context.toString();
    Assertions.assertTrue(contextString.contains("### " + MORE_INFO));
    context.reset();

    context.cause(new Exception(EXCEPTION_MESSAGE));
    contextString = context.toString();
    Assertions.assertTrue(contextString.contains("### Cause: java.lang.Exception: " + EXCEPTION_MESSAGE));
    context.reset();

  }

  @Test
  void verifyStoreRecall() throws Exception {
    ErrorContext outer = ErrorContext.instance();
    ErrorContext inner = ErrorContext.instance().store();
    assertEquals(inner, ErrorContext.instance());
    ErrorContext recalled = ErrorContext.instance().recall();
    assertEquals(outer, recalled);
    assertEquals(outer, ErrorContext.instance());
  }
}
