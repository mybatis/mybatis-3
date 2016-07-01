/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.reflection;

import java.io.Serializable;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PrefixlessReflectorTest {

  @Test
  public void testGetSetterType() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assert.assertEquals(Long.class, reflector.getSetterType("foo"));
    Assert.assertEquals(Long.class, reflector.getSetterType("bar"));
  }

  @Test
  public void testGetGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assert.assertEquals(Long.class, reflector.getGetterType("foo"));
    Assert.assertEquals(Long.class, reflector.getGetterType("bar"));
  }

  @Test
  public void shouldNotGetClass() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assert.assertFalse(reflector.hasGetter("class"));
  }

  @Test
  public void shouldNotGetNotify() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assert.assertFalse(reflector.hasGetter("notify"));
  }

  @Test
  public void shouldNotSetEquals() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assert.assertFalse(reflector.hasSetter("equals"));
  }

  /**
   * The class got both prefixed and non-prefixed setters and getters
   *
   * @param <T>
   */
  interface Entity<T> {
    T getFoo();
    void setFoo(T foo);
    T bar();
    void bar(T bar);
  }

  static abstract class AbstractEntity implements Entity<Long> {

    private Long foo;
    private Long bar;

    @Override
    public Long getFoo() {
      return foo;
    }

    @Override
    public void setFoo(Long foo) {
      this.foo = foo;
    }

    @Override
    public Long bar() {
      return bar;
    }

    @Override
    public void bar(Long bar) {
      this.bar = bar;
    }
  }

  static class Section extends AbstractEntity implements Entity<Long> {
  }

  @Test
  public void shouldResolveSetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getSetterType("foo"));
    assertEquals(String.class, reflector.getSetterType("bar"));
  }

  @Test
  public void shouldResolveParameterizedSetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(List.class, reflector.getSetterType("fooList"));
    assertEquals(List.class, reflector.getSetterType("barList"));
  }

  @Test
  public void shouldResolveArraySetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    Class<?> clazz = reflector.getSetterType("fooArray");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
    clazz = reflector.getSetterType("barArray");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
  }

  @Test
  public void shouldResolveGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getGetterType("foo"));
    assertEquals(String.class, reflector.getGetterType("bar"));
  }

  @Test
  public void shouldResolveSetterTypeFromPrivateField() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getSetterType("fooFld"));
    assertEquals(String.class, reflector.getSetterType("barFld"));
  }

  @Test
  public void shouldResolveGetterTypeFromPublicField() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getGetterType("pubFld"));
  }

  @Test
  public void shouldResolveParameterizedGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(List.class, reflector.getGetterType("fooList"));
    assertEquals(List.class, reflector.getGetterType("barList"));
  }

  @Test
  public void shouldResolveArrayGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    Class<?> clazz = reflector.getGetterType("fooArray");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
    clazz = reflector.getGetterType("barArray");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
  }

  /**
   * The class got both prefixed and non-prefixed setters and getters
   *
   * @param <T>
   */
  static abstract class Parent<T extends Serializable> {
    protected T foo;
    protected T bar;
    protected List<T> fooList;
    protected List<T> barList;
    protected T[] fooArray;
    protected T[] barArray;
    private T fooFld;
    private T barFld;
    public T pubFld;
    public T getFoo() {
      return foo;
    }
    public void setFoo(T foo) {
      this.foo = foo;
    }
    public T bar() {
      return bar;
    }
    public void bar(T bar) {
      this.bar = bar;
    }
    public List<T> getFooList() {
      return fooList;
    }
    public void setFooList(List<T> fooList) {
      this.fooList = fooList;
    }
    public List<T> barList() {
      return barList;
    }
    public void barList(List<T> barList) {
      this.barList = barList;
    }
    public T[] getFooArray() {
      return fooArray;
    }
    public void setFooArray(T[] fooArray) {
      this.fooArray = fooArray;
    }
    public T[] barArray() {
      return barArray;
    }
    public void barArray(T[] barArray) {
      this.barArray = barArray;
    }
    public T getFooFld() {
      return fooFld;
    }
    public T barFld() {
      return barFld;
    }
  }

  static class Child extends Parent<String> {
  }

  @Test(expected = ReflectionException.class)
  public void testDualGetSetterType() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Dia.class);
  }

  @Test(expected = ReflectionException.class)
  public void testDualGetGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Dia.class);
  }

  /**
   * class got setters and getters of both types for the same value
   *
   * Attempt to build a reflector around it will fail
   */
  static class Dia {
    private String val;

    public String getVal() {
      return val;
    }

    public void setVal(String val) {
      this.val = val;
    }

    public String val() {
      return val;
    }

    public void val(String val) {
      this.val = val;
    }
  }


  @Test
  public void testBackGetSetterType() throws Exception {
    ReflectorFactory reflectorFactory = new PrefixlessReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(SelfReferencing.class);
    Assert.assertEquals(String.class, reflector.getSetterType("val"));
  }

  static class SelfReferencing {
    private String val;

    /**
     * Setter that returns back a reference to the pojo
     *
     * @param val value to set
     * @return self-reference
     */
    public SelfReferencing val(String val) {
      this.val = val;
      return this;
    }
  }
}
