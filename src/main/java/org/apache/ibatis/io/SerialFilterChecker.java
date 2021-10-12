/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.io;

import java.security.Security;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

public final class SerialFilterChecker {
  private static final Log log = LogFactory.getLog(SerialFilterChecker.class);
  /* Property key for the JEP-290 serialization filters */
  private static final String JDK_SERIAL_FILTER = "jdk.serialFilter";
  private static final boolean SERIAL_FILTER_MISSING;
  private static boolean firstInvocation = true;

  static {
    Object serialFilter;
    try {
      Class<?> objectFilterConfig = Class.forName("java.io.ObjectInputFilter$Config");
      serialFilter = objectFilterConfig.getMethod("getSerialFilter").invoke(null);
    } catch (ReflectiveOperationException e) {
      // Java 1.8
      serialFilter = System.getProperty(JDK_SERIAL_FILTER, Security.getProperty(JDK_SERIAL_FILTER));
    }
    SERIAL_FILTER_MISSING = serialFilter == null;
  }

  public static void check() {
    if (firstInvocation && SERIAL_FILTER_MISSING) {
      firstInvocation = false;
      log.warn(
          "As you are using functionality that deserializes object streams, it is recommended to define the JEP-290 serial filter. "
              + "Please refer to https://docs.oracle.com/pls/topic/lookup?ctx=javase15&id=GUID-8296D8E8-2B93-4B9A-856E-0A65AF9B8C66");
    }
  }

  private SerialFilterChecker() {
  }
}
