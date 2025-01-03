/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection.wrapper;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectionException;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.junit.jupiter.api.Test;

class BeanWrapperTest {

  @Test
  void assertBasicOperations() {
    Bean1 bean = new Bean1();
    MetaObject metaObj = MetaObject.forObject(bean, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
        new DefaultReflectorFactory());
    assertFalse(metaObj.isCollection());
    assertTrue(metaObj.hasGetter("id"));
    assertTrue(metaObj.hasSetter("id"));
    assertTrue(metaObj.hasGetter("bean2.id"));
    assertTrue(metaObj.hasSetter("bean2.id"));
    assertEquals("id", metaObj.findProperty("id", false));
    assertNull(metaObj.findProperty("attr_val", false));
    assertEquals("attrVal", metaObj.findProperty("attr_val", true));
    String[] getterNames = metaObj.getGetterNames();
    Arrays.sort(getterNames);
    assertArrayEquals(new String[] { "attrVal", "bean2", "bean2List", "id", "nums" }, getterNames);
    String[] setterNames = metaObj.getSetterNames();
    Arrays.sort(setterNames);
    assertArrayEquals(new String[] { "attrVal", "bean2", "bean2List", "id", "nums" }, setterNames);
    assertEquals(String.class, metaObj.getGetterType("attrVal"));
    assertEquals(String.class, metaObj.getSetterType("attrVal"));
    assertEquals(String.class, metaObj.getGetterType("bean2.name"));
    assertEquals(String.class, metaObj.getSetterType("bean2.name"));

    assertTrue(metaObj.hasGetter("bean2List[0]"));
    try {
      metaObj.getValue("bean2List[0]");
      fail();
    } catch (ReflectionException e) {
      assertEquals("Cannot get the value 'bean2List[0]' because the property 'bean2List' is null.", e.getMessage());
    }
    try {
      metaObj.setValue("bean2List[0]", new Bean2());
      fail();
    } catch (ReflectionException e) {
      assertEquals("Cannot set the value 'bean2List[0]' because the property 'bean2List' is null.", e.getMessage());
    }
    assertTrue(metaObj.hasSetter("bean2List[0]"));

    List<Bean2> bean2List = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Bean2 bean2 = new Bean2();
      bean2.setId(i);
      bean2.setName("name_" + i);
      bean2List.add(bean2);
    }
    bean.setBean2List(bean2List);

    assertEquals(0, metaObj.getValue("bean2List[0].id"));

    metaObj.setValue("attrVal", "value");
    assertEquals("value", bean.getAttrVal());
    try {
      metaObj.getValue("attrVal[0]");
      fail();
    } catch (ReflectionException e) {
      assertEquals("Cannot get the value 'attrVal[0]' because the property 'attrVal' is not Map, List or Array.",
          e.getMessage());
    }
    try {
      metaObj.setValue("attrVal[0]", "blur");
      fail();
    } catch (ReflectionException e) {
      assertEquals("Cannot set the value 'attrVal[0]' because the property 'attrVal' is not Map, List or Array.",
          e.getMessage());
    }

    metaObj.setValue("bean2List[1].name", "new name 1");
    assertEquals("new name 1", bean.getBean2List().get(1).getName());

    try {
      metaObj.getValue("nums[0]");
      fail();
    } catch (ReflectionException e) {
      // pass
    }
    metaObj.setValue("nums", new Integer[] { 5, 6, 7 });
    assertTrue(metaObj.hasGetter("bean2List[0].child.id"));
    assertTrue(metaObj.hasSetter("bean2List[0].child.id"));

    {
      Bean2 bean2 = new Bean2();
      bean2.setId(100);
      bean2.setName("name_100");
      metaObj.setValue("bean2", bean2);
      assertEquals(String.class, metaObj.getGetterType("bean2.name"));
      assertEquals(String.class, metaObj.getSetterType("bean2.name"));
    }

    try {
      metaObj.setValue("bean2.child.bean2", "bogus");
      fail();
    } catch (ReflectionException e) {
      // pass
    }

    {
      Bean2 bean2 = new Bean2();
      bean2.setId(101);
      bean2.setName("name_101");
      metaObj.setValue("bean2.child.bean2", bean2);
      assertEquals(101, bean.getBean2().getChild().getBean2().getId());
    }

    metaObj.setValue("bean2.child.nums", new Integer[] { 8, 9 });
    metaObj.setValue("bean2.child.nums[0]", 88);
    assertEquals(88, bean.getBean2().getChild().getNums()[0]);

    assertFalse(metaObj.hasSetter("x[0].y"));
    assertFalse(metaObj.hasGetter("x[0].y"));

    try {
      metaObj.getValue("x");
      fail();
    } catch (ReflectionException e) {
      // pass
    }
    // assertEquals(Integer.class, metaObj.getSetterType("my_name"));
    // assertEquals("100", metaObj.getValue("a"));
    // assertNull(metaObj.getValue("b"));
    // assertEquals(Integer.valueOf(200), metaObj.getValue("my_name"));
    try {
      metaObj.add("x");
      fail();
    } catch (UnsupportedOperationException e) {
      // pass
    }
    try {
      metaObj.addAll(Arrays.asList("x", "y"));
      fail();
    } catch (UnsupportedOperationException e) {
      // pass
    }
  }

  static class Bean1 {
    private Integer id;
    private String attrVal;
    private Integer[] nums;
    private Bean2 bean2;
    private List<Bean2> bean2List;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getAttrVal() {
      return attrVal;
    }

    public void setAttrVal(String attrVal) {
      this.attrVal = attrVal;
    }

    public Integer[] getNums() {
      return nums;
    }

    public void setNums(Integer[] nums) {
      this.nums = nums;
    }

    public Bean2 getBean2() {
      return bean2;
    }

    public void setBean2(Bean2 bean2) {
      this.bean2 = bean2;
    }

    public List<Bean2> getBean2List() {
      return bean2List;
    }

    public void setBean2List(List<Bean2> bean2List) {
      this.bean2List = bean2List;
    }
  }

  static class Bean2 {
    private Integer id;
    private String name;
    private Bean1 child;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Bean1 getChild() {
      return child;
    }

    public void setChild(Bean1 child) {
      this.child = child;
    }
  }
}
