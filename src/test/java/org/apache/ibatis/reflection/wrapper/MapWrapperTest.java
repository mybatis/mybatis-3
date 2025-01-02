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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MapWrapperTest {

  @Test
  void assertBasicOperations() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("a", "100");
    map.put("b", null);
    map.put("my_name", Integer.valueOf(200));
    MetaObject metaObj = MetaObject.forObject(map, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
        new DefaultReflectorFactory());
    assertFalse(metaObj.isCollection());
    assertTrue(metaObj.hasGetter("a"));
    assertTrue(metaObj.hasSetter("a"));
    assertTrue(metaObj.hasGetter("b.anykey"));
    assertEquals("a", metaObj.findProperty("a", false));
    assertEquals("b", metaObj.findProperty("b", false));
    assertEquals("my_name", metaObj.findProperty("my_name", false));
    assertEquals("my_name", metaObj.findProperty("my_name", true));
    assertArrayEquals(new String[] { "a", "b", "my_name" }, metaObj.getGetterNames());
    assertArrayEquals(new String[] { "a", "b", "my_name" }, metaObj.getSetterNames());
    assertEquals(String.class, metaObj.getGetterType("a"));
    assertEquals(Object.class, metaObj.getGetterType("b"));
    assertEquals(Integer.class, metaObj.getGetterType("my_name"));
    assertEquals(String.class, metaObj.getSetterType("a"));
    assertEquals(Object.class, metaObj.getSetterType("b"));
    assertEquals(Integer.class, metaObj.getSetterType("my_name"));
    assertEquals("100", metaObj.getValue("a"));
    assertNull(metaObj.getValue("b"));
    assertEquals(Integer.valueOf(200), metaObj.getValue("my_name"));
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
    metaObj.setValue("a", Long.valueOf(900L));
    assertEquals(Long.valueOf(900L), map.get("a"));
  }

  @Test
  void assertNonExistentKey() {
    Map<String, Object> map = new HashMap<>();
    map.put("a", "100");
    MetaObject metaObj = MetaObject.forObject(map, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
        new DefaultReflectorFactory());
    assertEquals("anykey", metaObj.findProperty("anykey", false));
    assertFalse(metaObj.hasGetter("anykey"));
    assertFalse(metaObj.hasGetter("child.anykey"));
    assertEquals(Object.class, metaObj.getGetterType("anykey"));
    assertEquals(Object.class, metaObj.getGetterType("child.anykey"));
    assertTrue(metaObj.hasSetter("anykey"));
    assertTrue(metaObj.hasSetter("child.anykey"));
    assertEquals(Object.class, metaObj.getSetterType("anykey"));
    assertEquals(Object.class, metaObj.getSetterType("child.anykey"));
    assertNull(metaObj.getValue("anykey"));

    metaObj.setValue("anykey", Integer.valueOf(200));
    metaObj.setValue("child.anykey", Integer.valueOf(300));
    assertEquals(3, map.size());
    assertEquals(Integer.valueOf(200), map.get("anykey"));
    @SuppressWarnings("unchecked")
    Map<String, Object> childMap = (Map<String, Object>) map.get("child");
    assertEquals(Integer.valueOf(300), childMap.get("anykey"));
  }

  @Test
  void assertChildBean() {
    Map<String, Object> map = new HashMap<>();
    TestBean bean = new TestBean();
    bean.setPropA("aaa");
    map.put("bean", bean);
    MetaObject metaObj = MetaObject.forObject(map, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
        new DefaultReflectorFactory());
    assertTrue(metaObj.hasGetter("bean.propA"));
    assertEquals(String.class, metaObj.getGetterType("bean.propA"));
    assertEquals(String.class, metaObj.getSetterType("bean.propA"));
    assertEquals("aaa", metaObj.getValue("bean.propA"));

    assertNull(metaObj.getValue("bean.propB"));
    metaObj.setValue("bean.propB", "bbb");
    assertEquals("bbb", bean.getPropB());

    metaObj.setValue("bean.propA", "ccc");
    assertEquals("ccc", bean.getPropA());
  }

  static class TestBean {
    private String propA;
    private String propB;

    public String getPropA() {
      return propA;
    }

    public void setPropA(String propA) {
      this.propA = propA;
    }

    public String getPropB() {
      return propB;
    }

    public void setPropB(String propB) {
      this.propB = propB;
    }
  }

  @Test
  void accessIndexedList() {
    Map<String, Object> map = new HashMap<>();
    List<String> list = Arrays.asList("a", "b", "c");
    map.put("list", list);
    MetaObject metaObj = MetaObject.forObject(map, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
        new DefaultReflectorFactory());
    assertEquals("b", metaObj.getValue("list[1]"));

    metaObj.setValue("list[2]", "x");
    assertEquals("x", list.get(2));

    try {
      metaObj.setValue("list[3]", "y");
      fail();
    } catch (IndexOutOfBoundsException e) {
      // pass
    }

    assertTrue(metaObj.hasGetter("list[1]"));
    assertTrue(metaObj.hasSetter("list[1]"));
    // this one looks wrong
    // assertFalse(metaObj.hasSetter("list[3]"));
  }

  @Test
  void accessIndexedMap() {
    Map<String, Object> map = new HashMap<>();
    Map<String, String> submap = new HashMap<>();
    submap.put("a", "100");
    submap.put("b", "200");
    submap.put("c", "300");
    map.put("submap", submap);
    MetaObject metaObj = MetaObject.forObject(map, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
        new DefaultReflectorFactory());
    assertEquals("200", metaObj.getValue("submap[b]"));

    metaObj.setValue("submap[c]", "999");
    assertEquals("999", submap.get("c"));

    metaObj.setValue("submap[d]", "400");
    assertEquals(4, submap.size());
    assertEquals("400", submap.get("d"));

    assertTrue(metaObj.hasGetter("submap[b]"));
    assertTrue(metaObj.hasGetter("submap[anykey]"));
    assertTrue(metaObj.hasSetter("submap[d]"));
    assertTrue(metaObj.hasSetter("submap[anykey]"));
  }

  @ParameterizedTest
  @CsvSource({ "abc[def]", "abc.def", "abc.def.ghi", "abc[d.ef].ghi" })
  void customMapWrapper(String key) {
    Map<String, Object> map = new HashMap<>();
    MetaObject metaObj = MetaObject.forObject(map, new DefaultObjectFactory(), new FlatMapWrapperFactory(),
        new DefaultReflectorFactory());
    metaObj.setValue(key, "1");
    assertEquals("1", map.get(key));
    assertEquals("1", metaObj.getValue(key));
  }

  static class FlatMapWrapperFactory implements ObjectWrapperFactory {
    @Override
    public boolean hasWrapperFor(Object object) {
      return object instanceof Map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
      return new FlatMapWrapper(metaObject, (Map<String, Object>) object, metaObject.getObjectFactory());
    }
  }

  static class FlatMapWrapper extends MapWrapper {
    public FlatMapWrapper(MetaObject metaObject, Map<String, Object> map, ObjectFactory objectFactory) {
      super(metaObject, map);
    }

    @Override
    public Object get(PropertyTokenizer prop) {
      String key;
      if (prop.getChildren() == null) {
        key = prop.getIndexedName();
      } else {
        key = prop.getIndexedName() + "." + prop.getChildren();
      }
      return map.get(key);
    }

    @Override
    public void set(PropertyTokenizer prop, Object value) {
      String key;
      if (prop.getChildren() == null) {
        key = prop.getIndexedName();
      } else {
        key = prop.getIndexedName() + "." + prop.getChildren();
      }
      map.put(key, value);
    }
  }

}
