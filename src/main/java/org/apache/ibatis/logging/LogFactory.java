/*
 *    Copyright 2009-2012 The MyBatis Team
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

import java.lang.reflect.Constructor;

import org.apache.ibatis.io.Resources;

public final class LogFactory {

  /** 
   * Marker to be used by logging implementations that support markers 
   */
  public static final String MARKER = "MYBATIS";
  /**
   * Name for the global logger. Reference java.util.Logger.GLOBAL_LOGGER_NAME
   * directly once we drop JDK 5 compatibility.
   */
  public static final String GLOBAL_LOGGER_NAME = "global";

  private static Constructor<? extends Log> logConstructor;

  static {
    tryImplementation(new Runnable() {
      public void run() {
        useSlf4jLogging();
      }
    });
    tryImplementation(new Runnable() {
      public void run() {
        useCommonsLogging();
      }
    });
    tryImplementation(new Runnable() {
      public void run() {
        useLog4JLogging();
      }
    });
    tryImplementation(new Runnable() {
      public void run() {
        useJdkLogging();
      }
    });
    tryImplementation(new Runnable() {
      public void run() {
        useNoLogging();
      }
    });
  }

  private LogFactory() {
    // disable construction
  }

  public static Log getLog(Class<?> aClass) {
    return getLog(aClass.getName());
  }

  public static Log getLog(String logger) {
    try {
      return logConstructor.newInstance(new Object[]{logger});
    } catch (Throwable t) {
      throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
    }
  }

  public static synchronized void useSlf4jLogging() {
    setImplementation("org.apache.ibatis.logging.slf4j.Slf4jImpl");
  }

  public static synchronized void useCommonsLogging() {
    setImplementation("org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl");
  }

  public static synchronized void useLog4JLogging() {
    setImplementation("org.apache.ibatis.logging.log4j.Log4jImpl");
  }

  public static synchronized void useJdkLogging() {
    setImplementation("org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl");
  }

  public static synchronized void useStdOutLogging() {
    setImplementation("org.apache.ibatis.logging.stdout.StdOutImpl");
  }

  public static synchronized void useNoLogging() {
    setImplementation("org.apache.ibatis.logging.nologging.NoLoggingImpl");
  }

  private static void tryImplementation(Runnable runnable) {
    if (logConstructor == null) {
      try {
        runnable.run();
      } catch (Throwable t) {
        //ignore
      }
    }
  }

  private static void setImplementation(String implClassName) {
    try {
      @SuppressWarnings("unchecked")
      Class<? extends Log> implClass = (Class<? extends Log>) Resources.classForName(implClassName);
      Constructor<? extends Log> candidate = implClass.getConstructor(new Class[]{String.class});
      Log log = candidate.newInstance(new Object[]{LogFactory.class.getName()});
      log.debug("Logging initialized using '" + implClassName + "' adapter.");
      logConstructor = candidate;
    } catch (Throwable t) {
      throw new LogException("Error setting Log implementation.  Cause: " + t, t);
    }
  }

}
