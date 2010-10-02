package org.apache.ibatis.logging.stdout;

import org.apache.ibatis.logging.Log;

public class StdOutImpl implements Log {

  public StdOutImpl(Class<?> clazz) {
  }

  public boolean isDebugEnabled() {
    return true;
  }

  public void error(String s, Throwable e) {
    System.err.println(s);
    e.printStackTrace(System.err);
  }

  public void error(String s) {
    System.err.println(s);
  }

  public void debug(String s) {
    System.out.println(s);
  }

  public void warn(String s) {
    System.out.println(s);
  }
}
