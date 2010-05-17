package org.apache.ibatis.logging;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class LogFactoryTest {

  @Test
  public void shouldUseCommonsLogging() {
    LogFactory.useCommonsLogging();
    logSomething(true);
  }

  @Test
  public void shouldUseLog4J() {
    LogFactory.useLog4JLogging();
    logSomething(true);
  }

  @Test
  public void shouldUseJdKLogging() {
    LogFactory.useJdkLogging();
    logSomething(false);
  }

  @Test
  public void shouldUseStdOut() {
    LogFactory.useStdOutLogging();
    logSomething(true);
  }

  @Test
  public void shouldUseNoLogging() {
    LogFactory.useNoLogging();
    logSomething(false);
  }

  private void logSomething(boolean expectedDebug) {
    Log log = LogFactory.getLog(Object.class);
    log.warn("Warning message.");
    log.debug("Debug message.");
    log.error("Error message.");
    log.error("Error with Exception.", new Exception("Test exception."));
    assertEquals(expectedDebug, log.isDebugEnabled());
  }


}
