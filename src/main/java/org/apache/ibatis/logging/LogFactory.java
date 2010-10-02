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
    setImplementation("org.slf4j.LoggerFactory", "org.apache.ibatis.logging.slf4j.Slf4jImpl");
  }

  public static synchronized void useCommonsLogging() {
    setImplementation("org.apache.commons.logging.LogFactory", "org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl");
  }

  public static synchronized void useLog4JLogging() {
    setImplementation("org.apache.log4j.Logger", "org.apache.ibatis.logging.log4j.Log4jImpl");
  }

  public static synchronized void useJdkLogging() {
    setImplementation("java.util.logging.Logger", "org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl");
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
    setImplementation(implClassName, implClassName);
  }

  private static void setImplementation(String testClassName, String implClassName) {
    try {
      Resources.classForName(testClassName);
      @SuppressWarnings("unchecked")
      Class<? extends Log> implClass = (Class<Log>) Resources.classForName(implClassName);
      logConstructor = implClass.getConstructor(new Class[]{Class.class});
    } catch (Throwable t) {
      throw new LogException("Error setting Log implementation.  Cause: " + t, t);
    }
  }

}
