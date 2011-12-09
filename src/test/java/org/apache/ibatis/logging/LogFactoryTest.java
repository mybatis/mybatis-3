/*
 *    Copyright 2009-2011 The MyBatis Team
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
