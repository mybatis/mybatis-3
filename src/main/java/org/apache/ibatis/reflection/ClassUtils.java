package org.apache.ibatis.reflection;

/**
 * @author machunxiao
 * @create 2018-07-19 22:32
 */
public class ClassUtils {

  public static String classesToString(Class<?>[] classes) {
    if (classes == null || classes.length == 0) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < classes.length; ) {
      builder.append(classes[i].getName());
      i++;
      if (i < classes.length) {
        builder.append(",");
      }
    }
    return builder.toString();
  }

  public static Class<?> getClassByName(String className) {
    if ("boolean".equals(className) || "bool".equals(className)) {
      return boolean.class;
    } else if ("byte".equalsIgnoreCase(className)) {
      return byte.class;
    } else if ("char".equalsIgnoreCase(className)) {
      return char.class;
    } else if ("short".equalsIgnoreCase(className)) {
      return short.class;
    } else if ("float".equalsIgnoreCase(className)) {
      return float.class;
    } else if ("int".equalsIgnoreCase(className)) {
      return int.class;
    } else if ("long".equalsIgnoreCase(className)) {
      return long.class;
    } else if ("double".equalsIgnoreCase(className)) {
      return double.class;
    } else if ("void".equalsIgnoreCase(className)){
      return void.class;
    } else {
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private ClassUtils() {}
}
