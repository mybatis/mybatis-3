package org.apache.ibatis.logging;

import java.lang.reflect.Constructor;

import org.apache.ibatis.io.Resources;

public class LogFactory {

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

  public static Log getLog(Class<?> aClass) {
    try {
      return logConstructor.newInstance(new Object[]{aClass});
    } catch (Throwable t) {
      throw new LogException("Error creating logger for class " + aClass + ".  Cause: " + t, t);
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
      Constructor<? extends Log> candidate = implClass.getConstructor(new Class[]{Class.class});
      Log log = candidate.newInstance(new Object[]{LogFactory.class});
      log.debug("Logging initialized using '" + implClassName + "' adapter.");
      logConstructor = candidate;
    } catch (Throwable t) {
      throw new LogException("Error setting Log implementation.  Cause: " + t, t);
    }
  }

}
