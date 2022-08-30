package org.apache.ibatis.reflection.property;

import org.junit.jupiter.api.Test;

/**
 * @author Hobo
 * @date 2022/8/28
 */
public class PropertyNamerTest {

  @Test
  public void test() {
    String a = PropertyNamer.methodToProperty("isA");
    System.out.println(a);
    String b = PropertyNamer.methodToProperty("isb");
    System.out.println(b);
    String c = PropertyNamer.methodToProperty("isbc");
    System.out.println(c);
    String d = PropertyNamer.methodToProperty("isbD");
    System.out.println(d);
    String e = PropertyNamer.methodToProperty("isBd");
    System.out.println(e);
    String f = PropertyNamer.methodToProperty("isBD");
    System.out.println(f);
  }
}
