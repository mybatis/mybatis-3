/**
 *    Copyright 2009-2018 the original author or authors.
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

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.reflection.invoker.Invoker;
import org.junit.Assert;
import org.junit.Test;
import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

public class ReflectorTest {

  @Test
  public void testGetSetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assert.assertEquals(Long.class, reflector.getSetterType("id"));
  }

  @Test
  public void testGetGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assert.assertEquals(Long.class, reflector.getGetterType("id"));
  }

  @Test
  public void shouldNotGetClass() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assert.assertFalse(reflector.hasGetter("class"));
  }

  static interface Entity<T> {
    T getId();
    void setId(T id);
  }

  static abstract class AbstractEntity implements Entity<Long> {

    private Long id;

    @Override
    public Long getId() {
      return id;
    }

    @Override
    public void setId(Long id) {
      this.id = id;
    }
  }

  static class Section extends AbstractEntity implements Entity<Long> {
  }

  @Test
  public void shouldResolveSetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getSetterType("id"));
  }

  @Test
  public void shouldResolveParameterizedSetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(List.class, reflector.getSetterType("list"));
  }

  @Test
  public void shouldResolveArraySetterParam() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    Class<?> clazz = reflector.getSetterType("array");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
  }

  @Test
  public void shouldResolveGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getGetterType("id"));
  }

  @Test
  public void shouldResolveSetterTypeFromPrivateField() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getSetterType("fld"));
  }

  @Test
  public void shouldResolveGetterTypeFromPublicField() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getGetterType("pubFld"));
  }

  @Test
  public void shouldResolveParameterizedGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(List.class, reflector.getGetterType("list"));
  }

  @Test
  public void shouldResolveArrayGetterType() throws Exception {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    Class<?> clazz = reflector.getGetterType("array");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
  }

  static abstract class Parent<T extends Serializable> {
    protected T id;
    protected List<T> list;
    protected T[] array;
    private T fld;
    public T pubFld;
    public T getId() {
      return id;
    }
    public void setId(T id) {
      this.id = id;
    }
    public List<T> getList() {
      return list;
    }
    public void setList(List<T> list) {
      this.list = list;
    }
    public T[] getArray() {
      return array;
    }
    public void setArray(T[] array) {
      this.array = array;
    }
    public T getFld() {
      return fld;
    }
  }

  static class Child extends Parent<String> {
  }

  @Test
  public void shouldResoleveReadonlySetterWithOverload() throws Exception {
    class BeanClass implements BeanInterface<String> {
      @Override
      public void setId(String id) {
        // Do nothing
      }
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(BeanClass.class);
    assertEquals(String.class, reflector.getSetterType("id"));
  }

  interface BeanInterface<T> {
    void setId(T id);
  }

  @Test
  public void shouldSettersWithUnrelatedArgTypesThrowException() throws Exception {
    @SuppressWarnings("unused")
    class BeanClass {
      public void setProp1(String arg) {}
      public void setProp2(String arg) {}
      public void setProp2(Integer arg) {}
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(BeanClass.class);

    List<String> setableProps = Arrays.asList(reflector.getSetablePropertyNames());
    assertTrue(setableProps.contains("prop1"));
    assertTrue(setableProps.contains("prop2"));
    assertEquals("prop1", reflector.findPropertyName("PROP1"));
    assertEquals("prop2", reflector.findPropertyName("PROP2"));

    assertEquals(String.class, reflector.getSetterType("prop1"));
    assertNotNull(reflector.getSetInvoker("prop1"));

    when(reflector).getSetterType("prop2");
    then(caughtException()).isInstanceOf(ReflectionException.class)
      .hasMessageContaining("prop2")
      .hasMessageContaining("BeanClass")
      .hasMessageContaining("java.lang.String")
      .hasMessageContaining("java.lang.Integer");

    when(reflector).getSetInvoker("prop2");
    then(caughtException()).isInstanceOf(ReflectionException.class)
      .hasMessageContaining("prop2")
      .hasMessageContaining("BeanClass")
      .hasMessageContaining("java.lang.String")
      .hasMessageContaining("java.lang.Integer");
  }

  @Test
  public void shouldTwoGettersForNonBooleanPropertyThrowException() throws Exception {
    @SuppressWarnings("unused")
    class BeanClass {
      public Integer getProp1() {return 1;}
      public int getProp2() {return 0;}
      public int isProp2() {return 0;}
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(BeanClass.class);

    List<String> getableProps = Arrays.asList(reflector.getGetablePropertyNames());
    assertTrue(getableProps.contains("prop1"));
    assertTrue(getableProps.contains("prop2"));
    assertEquals("prop1", reflector.findPropertyName("PROP1"));
    assertEquals("prop2", reflector.findPropertyName("PROP2"));

    assertEquals(Integer.class, reflector.getGetterType("prop1"));
    Invoker getInvoker = reflector.getGetInvoker("prop1");
    assertEquals(Integer.valueOf(1), getInvoker.invoke(new BeanClass(), null));

    when(reflector).getGetterType("prop2");
    then(caughtException()).isInstanceOf(ReflectionException.class)
      .hasMessageContaining("prop2")
      .hasMessageContaining("BeanClass");

    when(reflector).getGetInvoker("prop2");
    then(caughtException()).isInstanceOf(ReflectionException.class)
      .hasMessageContaining("prop2")
      .hasMessageContaining("BeanClass");
  }

  @Test
  public void shouldTwoGettersWithDifferentTypesThrowException() throws Exception {
    @SuppressWarnings("unused")
    class BeanClass {
      public Integer getProp1() {return 1;}
      public Integer getProp2() {return 1;}
      public boolean isProp2() {return false;}
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(BeanClass.class);

    List<String> getableProps = Arrays.asList(reflector.getGetablePropertyNames());
    assertTrue(getableProps.contains("prop1"));
    assertTrue(getableProps.contains("prop2"));
    assertEquals("prop1", reflector.findPropertyName("PROP1"));
    assertEquals("prop2", reflector.findPropertyName("PROP2"));

    assertEquals(Integer.class, reflector.getGetterType("prop1"));
    Invoker getInvoker = reflector.getGetInvoker("prop1");
    assertEquals(Integer.valueOf(1), getInvoker.invoke(new BeanClass(), null));

    when(reflector).getGetterType("prop2");
    then(caughtException()).isInstanceOf(ReflectionException.class)
      .hasMessageContaining("prop2")
      .hasMessageContaining("BeanClass");

    when(reflector).getGetInvoker("prop2");
    then(caughtException()).isInstanceOf(ReflectionException.class)
      .hasMessageContaining("prop2")
      .hasMessageContaining("BeanClass");
  }

  @Test
  public void shouldAllowTwoBooleanGetters() throws Exception {
    @SuppressWarnings("unused")
    class Bean {
      // JavaBean Spec allows this (see #906)
      public boolean isBool() {return true;}
      public boolean getBool() {return false;}
      public void setBool(boolean bool) {}
    }
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Bean.class);
    assertTrue((Boolean)reflector.getGetInvoker("bool").invoke(new Bean(), new Byte[0]));
  }
}
