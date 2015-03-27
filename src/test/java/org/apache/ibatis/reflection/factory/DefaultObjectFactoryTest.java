/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.reflection.factory;

import java.util.Arrays;
import java.util.Collections;

import org.apache.ibatis.reflection.ReflectionException;
import org.junit.Assert;
import org.junit.Test;

/**
 * DefaultObjectFactoryTest
 *
 * @author Ryan Lamore
 */
public class DefaultObjectFactoryTest {

  @Test
  public void instantiateClass() throws Exception {
    DefaultObjectFactory defaultObjectFactory = new DefaultObjectFactory();
    TestClass testClass = defaultObjectFactory.instantiateClass(TestClass.class,
        Arrays.<Class<?>>asList(String.class, Integer.class), Arrays.<Object>asList("foo", 0));

    Assert.assertEquals("myInteger didn't match expected", (Integer) 0, testClass.myInteger);
    Assert.assertEquals("myString didn't match expected", "foo", testClass.myString);
  }

  @Test
  public void instantiateClassThrowsProperErrorMsg() {
    DefaultObjectFactory defaultObjectFactory = new DefaultObjectFactory();
    try {
      defaultObjectFactory.instantiateClass(TestClass.class, Collections.<Class<?>>singletonList(String.class), Collections.<Object>singletonList("foo"));
      Assert.fail("Should have thrown ReflectionException");
    } catch (Exception e) {
      Assert.assertTrue("Should be ReflectionException", e instanceof ReflectionException);
      Assert.assertTrue("Should not have trailing commas in types list", e.getMessage().contains("(String)"));
      Assert.assertTrue("Should not have trailing commas in values list", e.getMessage().contains("(foo)"));
    }
  }

}
