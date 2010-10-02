package org.apache.ibatis.logging.jdk14;

import org.apache.ibatis.logging.Log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Jdk14LoggingImpl implements Log {

  private Logger log;

  public Jdk14LoggingImpl(Class<?> clazz) {
    log = Logger.getLogger(clazz.getName());
  }

  public boolean isDebugEnabled() {
    return log.isLoggable(Level.FINE);
  }

  public void error(String s, Throwable e) {
    log.log(Level.SEVERE, s, e);
  }

  public void error(String s) {
    log.log(Level.SEVERE, s);
  }

  public void debug(String s) {
    log.log(Level.FINE, s);
  }

  public void warn(String s) {
    log.log(Level.WARNING, s);
  }


}
