/**
 *    Copyright 2009-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
  public void createClass() throws Exception {
    DefaultObjectFactory defaultObjectFactory = new DefaultObjectFactory();
    TestClass testClass = defaultObjectFactory.create(TestClass.class,
        Arrays.<Class<?>>asList(String.class, Integer.class), Arrays.<Object>asList("foo", 0));

    Assert.assertEquals("myInteger didn't match expected", (Integer) 0, testClass.myInteger);
    Assert.assertEquals("myString didn't match expected", "foo", testClass.myString);
  }

  @Test
  public void createClassThrowsProperErrorMsg() {
    DefaultObjectFactory defaultObjectFactory = new DefaultObjectFactory();
    try {
      defaultObjectFactory.create(TestClass.class, Collections.<Class<?>>singletonList(String.class), Collections.<Object>singletonList("foo"));
      Assert.fail("Should have thrown ReflectionException");
    } catch (Exception e) {
      Assert.assertTrue("Should be ReflectionException", e instanceof ReflectionException);
      Assert.assertTrue("Should not have trailing commas in types list", e.getMessage().contains("(String)"));
      Assert.assertTrue("Should not have trailing commas in values list", e.getMessage().contains("(foo)"));
    }
  }

  @Test
  public void creatHashMap() throws  Exception{
     DefaultObjectFactory defaultObjectFactory=new DefaultObjectFactory();
     Map  map= defaultObjectFactory.create(Map.class,null,null);
     Assert.assertTrue("Should be HashMap",map instanceof HashMap);
  }

  @Test
  public void createArrayList() throws Exception {
    DefaultObjectFactory defaultObjectFactory = new DefaultObjectFactory();
    List list = defaultObjectFactory.create(List.class);
    Assert.assertTrue(" list should be ArrayList", list instanceof ArrayList);

    Collection collection = defaultObjectFactory.create(Collection.class);
    Assert.assertTrue(" collection should be ArrayList", collection instanceof ArrayList);

    Iterable iterable = defaultObjectFactory.create(Iterable.class);
    Assert.assertTrue(" iterable should be ArrayList", iterable instanceof ArrayList);
  }


  @Test
  public void createTreeSet() throws Exception {
    DefaultObjectFactory defaultObjectFactory = new DefaultObjectFactory();
    SortedSet sortedSet = defaultObjectFactory.create(SortedSet.class);
    Assert.assertTrue(" sortedSet should be TreeSet", sortedSet instanceof TreeSet);
  }


  @Test
  public void createHashSet() throws Exception {
    DefaultObjectFactory defaultObjectFactory = new DefaultObjectFactory();
    Set set = defaultObjectFactory.create(Set.class);
    Assert.assertTrue(" set should be HashSet", set instanceof HashSet);
  }
}
