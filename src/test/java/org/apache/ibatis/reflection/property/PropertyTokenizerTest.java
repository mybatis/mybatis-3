package org.apache.ibatis.reflection.property;

import org.junit.jupiter.api.Test;

/**
 * @author Hobo
 * @date 2022/8/28
 */
public class PropertyTokenizerTest {

  @Test
  public void test() {
    System.out.println(new PropertyTokenizer("a.test()"));
    System.out.println(new PropertyTokenizer("a.test()"));
    System.out.println(new PropertyTokenizer("a[0].test()"));
  }
}
