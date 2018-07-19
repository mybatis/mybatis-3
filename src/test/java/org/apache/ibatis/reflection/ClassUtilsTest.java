package org.apache.ibatis.reflection;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author machunxiao
 * @create 2018-07-19 22:37
 */
public class ClassUtilsTest {

  @Test
  public void testClassesToString() {
    Class<?>[] classes = new Class<?>[]{int.class, String.class};
    String s = ClassUtils.classesToString(classes);
    String original = "int,java.lang.String";
    Assert.assertEquals(original, s);
  }

  @Test
  public void testGetClassByName() {
    String[] classNames = {"boolean", "bool", "byte", "short", "java.lang.Short", "int", "char", "float", "long", "double", String.class.getName(), "Void"};
    Class<?>[] originalClasses = new Class<?>[]{
            boolean.class,
            boolean.class,
            byte.class,
            short.class,
            Short.class,
            int.class,
            char.class,
            float.class,
            long.class,
            double.class,
            String.class,
            void.class
    };
    Class<?>[] classes = new Class<?>[classNames.length];
    for (int i = 0; i < classNames.length; i++) {
      classes[i] = ClassUtils.getClassByName(classNames[i]);
    }
    Assert.assertArrayEquals(originalClasses, classes);
  }

}
