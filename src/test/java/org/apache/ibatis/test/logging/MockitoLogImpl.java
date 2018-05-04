/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.test.logging;

import org.apache.ibatis.logging.Log;
import org.mockito.Mockito;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Utility class that verify logging.
 *
 * @author Kazuki Shimizu
 * @since 3.5.0
 */
public class MockitoLogImpl implements Log {

  public static final Map<String, Log> logs = new LinkedHashMap<>();

  private Log log;

  public MockitoLogImpl(String name) {
    this.log = Mockito.mock(Log.class, name);
    logs.put(name, log);
  }

  public static void reset() {
    logs.values().forEach(Mockito::reset);
  }

  @Override
  public boolean isDebugEnabled() {
    return log.isDebugEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return log.isTraceEnabled();
  }

  @Override
  public void error(String s, Throwable e) {
    log.error(s, e);
  }

  @Override
  public void error(String s) {
    log.error(s);
  }

  @Override
  public void debug(String s) {
    log.debug(s);
  }

  @Override
  public void trace(String s) {
    log.trace(s);
  }

  @Override
  public void warn(String s) {
    log.warn(s);
  }

}
