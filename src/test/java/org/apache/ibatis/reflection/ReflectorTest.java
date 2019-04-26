/**
 *    Copyright 2009-2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static com.googlecode.catchexception.apis.BDDCatchException.*;
import static org.assertj.core.api.BDDAssertions.then;

class ReflectorTest {

  @Test
  void testGetSetterType() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assertions.assertEquals(Long.class, reflector.getSetterType("id"));
  }

  @Test
  void testGetGetterType() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assertions.assertEquals(Long.class, reflector.getGetterType("id"));
  }

  @Test
  void shouldNotGetClass() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Section.class);
    Assertions.assertFalse(reflector.hasGetter("class"));
  }

  interface Entity<T> {
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
  void shouldResolveSetterParam() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getSetterType("id"));
  }

  @Test
  void shouldResolveParameterizedSetterParam() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(List.class, reflector.getSetterType("list"));
  }

  @Test
  void shouldResolveArraySetterParam() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    Class<?> clazz = reflector.getSetterType("array");
    assertTrue(clazz.isArray());
    assertEquals(String.class, clazz.getComponentType());
  }

  @Test
  void shouldResolveGetterType() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getGetterType("id"));
  }

  @Test
  void shouldResolveSetterTypeFromPrivateField() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getSetterType("fld"));
  }

  @Test
  void shouldResolveGetterTypeFromPublicField() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(String.class, reflector.getGetterType("pubFld"));
  }

  @Test
  void shouldResolveParameterizedGetterType() {
    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = reflectorFactory.findForClass(Child.class);
    assertEquals(List.class, reflector.getGetterType("list"));
  }

  @Test
  void shouldResolveArrayGetterType() {
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
  void shouldResoleveReadonlySetterWithOverload() {
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
  void shouldSettersWithUnrelatedArgTypesThrowException() {
    @SuppressWarnings("unused")
    class BeanClass {
      public void setTheProp(String arg) {}
      public void setTheProp(Integer arg) {}
    }

    ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    when(reflectorFactory).findForClass(BeanClass.class);
    then(caughtException()).isInstanceOf(ReflectionException.class)
      .hasMessageContaining("theProp")
      .hasMessageContaining("BeanClass")
      .hasMessageContaining("java.lang.String")
      .hasMessageContaining("java.lang.Integer");
  }

  @Test
  void shouldAllowTwoBooleanGetters() throws Exception {
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
