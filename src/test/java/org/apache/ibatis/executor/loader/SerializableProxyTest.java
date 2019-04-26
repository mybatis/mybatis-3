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
package org.apache.ibatis.executor.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class SerializableProxyTest {

  protected Author author = new Author(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);

  ProxyFactory proxyFactory;

  @Test
  void shouldKeepGenericTypes() {
    for (int i = 0; i < 10000; i++) {
      Author pc = new Author();
      Author proxy = (Author) proxyFactory.createProxy(pc, new ResultLoaderMap(), new Configuration(), new DefaultObjectFactory(),
          new ArrayList<>(), new ArrayList<>());
      proxy.getBio();
    }
  }

  @Test
  void shouldSerializeAProxyForABeanWithDefaultConstructor() throws Exception {
    Object proxy = proxyFactory.createProxy(author, new ResultLoaderMap(), new Configuration(), new DefaultObjectFactory(), new ArrayList<>(), new ArrayList<>());
    Object proxy2 = deserialize(serialize((Serializable) proxy));
    assertEquals(author, proxy2);
  }

  @Test
  void shouldSerializeAProxyForABeanWithoutDefaultConstructor() throws Exception {
    AuthorWithoutDefaultConstructor author = new AuthorWithoutDefaultConstructor(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);
    ArrayList<Class<?>> argTypes = new ArrayList<>();
    argTypes.add(Integer.class);
    argTypes.add(String.class);
    argTypes.add(String.class);
    argTypes.add(String.class);
    argTypes.add(String.class);
    argTypes.add(Section.class);
    ArrayList<Object> argValues = new ArrayList<>();
    argValues.add(999);
    argValues.add("someone");
    argValues.add("!@#@!#!@#");
    argValues.add("someone@somewhere.com");
    argValues.add("blah");
    argValues.add(Section.NEWS);
    Object proxy = proxyFactory.createProxy(author, new ResultLoaderMap(), new Configuration(), new DefaultObjectFactory(), argTypes, argValues);
    Object proxy2 = deserialize(serialize((Serializable) proxy));
    assertEquals(author, proxy2);
  }

  @Test
  void shouldSerializeAProxyForABeanWithoutDefaultConstructorAndUnloadedProperties() throws Exception {
    AuthorWithoutDefaultConstructor author = new AuthorWithoutDefaultConstructor(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);
    ArrayList<Class<?>> argTypes = new ArrayList<>();
    argTypes.add(Integer.class);
    argTypes.add(String.class);
    argTypes.add(String.class);
    argTypes.add(String.class);
    argTypes.add(String.class);
    argTypes.add(Section.class);
    ArrayList<Object> argValues = new ArrayList<>();
    argValues.add(999);
    argValues.add("someone");
    argValues.add("!@#@!#!@#");
    argValues.add("someone@somewhere.com");
    argValues.add("blah");
    argValues.add(Section.NEWS);
    ResultLoaderMap loader = new ResultLoaderMap();
    loader.addLoader("id", null, null);
    Object proxy = proxyFactory.createProxy(author, loader, new Configuration(), new DefaultObjectFactory(), argTypes, argValues);
    Object proxy2 = deserialize(serialize((Serializable) proxy));
    assertEquals(author, proxy2);
  }

  @Test
  void shouldSerizaliceAFullLoadedObjectToOriginalClass() throws Exception {
    Object proxy = proxyFactory.createProxy(author, new ResultLoaderMap(), new Configuration(), new DefaultObjectFactory(), new ArrayList<>(), new ArrayList<>());
    Object proxy2 = deserialize(serialize((Serializable) proxy));
    assertEquals(author.getClass(), proxy2.getClass());
  }

  @Test
  void shouldGenerateWriteReplace() throws Exception {
    try {
      author.getClass().getDeclaredMethod("writeReplace");
      fail("Author should not have a writeReplace method");
    } catch (NoSuchMethodException e) {
      // ok
    }
    Object proxy = proxyFactory.createProxy(author, new ResultLoaderMap(), new Configuration(), new DefaultObjectFactory(), new ArrayList<>(), new ArrayList<>());
    Method m = proxy.getClass().getDeclaredMethod("writeReplace");
  }

  @Test
  void shouldNotGenerateWriteReplaceItThereIsAlreadyOne() {
    AuthorWithWriteReplaceMethod beanWithWriteReplace = new AuthorWithWriteReplaceMethod(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);
    try {
      beanWithWriteReplace.getClass().getDeclaredMethod("writeReplace");
    } catch (NoSuchMethodException e) {
      fail("Bean should declare a writeReplace method");
    }
    Object proxy = proxyFactory.createProxy(beanWithWriteReplace, new ResultLoaderMap(), new Configuration(), new DefaultObjectFactory(), new ArrayList<>(), new ArrayList<>());
    Class<?>[] interfaces = proxy.getClass().getInterfaces();
    boolean ownInterfaceFound = false;
    for (Class<?> i : interfaces) {
      if (i.equals(WriteReplaceInterface.class)) {
        ownInterfaceFound = true;
        break;
      }
    }
    assertFalse(ownInterfaceFound);
  }

  @Test
  void shouldNotCreateAProxyForAFullyLoadedBean() throws Exception {
    Object proxy = proxyFactory.createProxy(author, new ResultLoaderMap(), new Configuration(), new DefaultObjectFactory(), new ArrayList<>(), new ArrayList<>());
    Author author2 = (Author) deserialize(serialize((Serializable) proxy));
    assertEquals(author.getClass(), author2.getClass());
  }

  @Test
  void shouldNotLetReadUnloadedPropertyAfterSerialization() throws Exception {
    ResultLoaderMap loader = new ResultLoaderMap();
    loader.addLoader("id", null, null);
    Object proxy = proxyFactory.createProxy(author, loader, new Configuration(), new DefaultObjectFactory(), new ArrayList<>(), new ArrayList<>());
    Author author2 = (Author) deserialize(serialize((Serializable) proxy));
    Assertions.assertThrows(ExecutorException.class, author2::getId);
  }

  @Test
  void shouldNotLetReadUnloadedPropertyAfterTwoSerializations() throws Exception {
    ResultLoaderMap loader = new ResultLoaderMap();
    loader.addLoader("id", null, null);
    Object proxy = proxyFactory.createProxy(author, loader, new Configuration(), new DefaultObjectFactory(), new ArrayList<>(), new ArrayList<>());
    Author author2 = (Author) deserialize(serialize(deserialize(serialize((Serializable) proxy))));
    Assertions.assertThrows(ExecutorException.class, author2::getId);
  }

  @Test
  void shouldLetReadALoadedPropertyAfterSerialization() throws Exception {
    Object proxy = proxyFactory.createProxy(author, new ResultLoaderMap(), new Configuration(), new DefaultObjectFactory(), new ArrayList<>(), new ArrayList<>());
    byte[] ser = serialize((Serializable) proxy);
    Author author2 = (Author) deserialize(ser);
    assertEquals(999, author2.getId());
  }

  byte[] serialize(Serializable value) throws Exception {
    try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(value);
      oos.flush();
      return bos.toByteArray();
    }
  }

  Serializable deserialize(byte[] value) throws Exception {
    try(ByteArrayInputStream bis = new ByteArrayInputStream(value);
    ObjectInputStream ois = new ObjectInputStream(bis)) {
      return (Serializable) ois.readObject();
    }
  }

  public static class AuthorWithWriteReplaceMethod extends Author {

    public AuthorWithWriteReplaceMethod() {
    }

    AuthorWithWriteReplaceMethod(Integer id, String username, String password, String email, String bio, Section section) {
        super(id, username, password, email, bio, section);
    }

    Object writeReplace() throws ObjectStreamException {
      return this;
    }
  }

  public static class AuthorWithoutDefaultConstructor extends Author {

    AuthorWithoutDefaultConstructor(Integer id, String username, String password, String email, String bio, Section section) {
        super(id, username, password, email, bio, section);
    }

    protected Object writeReplace() throws ObjectStreamException {
      return this;
    }
  }

}
