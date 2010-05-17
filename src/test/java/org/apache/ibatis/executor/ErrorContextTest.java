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
