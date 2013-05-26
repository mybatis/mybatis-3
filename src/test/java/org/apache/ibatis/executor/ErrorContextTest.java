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
package org.apache.ibatis.executor;

import org.junit.Test;

public class ErrorContextTest {

  @Test
  public void shouldShowProgressiveErrorContextBuilding() {
    ErrorContext context = ErrorContext.instance();
    context.resource("somefile.xml").activity("some activity").object("some object").message("Here's more info.");
    context.toString().startsWith("### The error occurred in somefile.xml.");
    context.reset();

    context.activity("some activity").object("some object").message("Here's more info.");
    context.toString().startsWith("### The error occurred while some activity.");
    context.reset();

    context.object("some object").message("Here's more info.");
    context.toString().startsWith("### Check some object.");
    context.reset();

    context.message("Here's more info.");
    context.toString().startsWith("### Here's more info.");
    context.reset();

    context.cause(new Exception("test"));
    context.toString().startsWith("### Cause: java.lang.Exception: test");
    context.reset();

  }

}
