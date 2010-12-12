package org.apache.ibatis.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.loader.ResultObjectProxy;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.junit.Test;

import domain.blog.Author;
import domain.blog.Section;

public class SerializableProxyTest {

  @Test
  public void shouldDemonstrateSerializableEnhancer() throws Exception {
    Author author = new Author(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);
    Object proxy = ResultObjectProxy.createProxy(author, new ResultLoaderMap(), true, new DefaultObjectFactory());
    byte[] bytes = serialize((Serializable) proxy);
    Object proxy2 = deserialize(bytes);
    // serialization/deserialization of a cglib proxy in the same execution always works
    // because the generated class is registerd to the jvm
    // we should check this in different executions of just check the class name
    assertEquals(author, proxy2);
    assertEquals(author.getClass(), proxy2.getClass());
  }

  @Test
  public void shouldGenerateWriteReplace() throws Exception {
    Author author = new Author(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);
    try {
      author.getClass().getDeclaredMethod("writeReplace");
      fail("Author should not have a writeReplace method");
    } catch (Exception e) {
      // ok
    }
    Object proxy = ResultObjectProxy.createProxy(author, new ResultLoaderMap(), true, new DefaultObjectFactory());
    // check that writeReplace method was generated
    proxy.getClass().getDeclaredMethod("writeReplace");
  }
  
  @Test
  public void shouldNotGenerateWriteReplace() throws Exception {
    BeanWithWriteReplace beanWithWriteReplace = new BeanWithWriteReplace(999, "someone");
    Object proxy = ResultObjectProxy.createProxy(beanWithWriteReplace, new ResultLoaderMap(), true, new DefaultObjectFactory());
    try {
      beanWithWriteReplace.getClass().getDeclaredMethod("writeReplace");
    } catch (Exception e) {
      fail("Bean should declare a writeReplace method");
    }
    Method m = proxy.getClass().getDeclaredMethod("writeReplace");
    assertFalse("generated method is public so this should be protected", m.isAccessible());
  }

  @Test(expected=NotSerializableException.class)
  public void shouldNotSerializePartiallyLoadedBean() throws Exception {
    BeanWithWriteReplace beanWithWriteReplace = new BeanWithWriteReplace(999, "someone");
    ResultLoaderMap loader = new ResultLoaderMap();
    loader.addLoader("property", null, null);
    Object proxy = ResultObjectProxy.createProxy(beanWithWriteReplace, loader, true, new DefaultObjectFactory());
    serialize((Serializable) proxy);
  }

  private byte[] serialize(Serializable value) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(value);
    oos.flush();
    oos.close();
    return bos.toByteArray();
  }

  private Serializable deserialize(byte[] value) throws Exception {
    ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) value);
    ObjectInputStream ois = new ObjectInputStream(bis);
    Serializable result = (Serializable) ois.readObject();
    ois.close();
    return result;
  }

}
