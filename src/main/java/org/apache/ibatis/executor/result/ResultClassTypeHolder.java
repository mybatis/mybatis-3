package org.apache.ibatis.executor.result;

public class ResultClassTypeHolder {

  // hold the the class type of result
  private static final ThreadLocal<Class> resultTypeTl = new ThreadLocal();

  public static void setResultType(Class clazz) {
    resultTypeTl.set(clazz);
  }

  public static Class getResultType() {
    return resultTypeTl.get();
  }

  public static void clean() {
    resultTypeTl.remove();
  }
}
