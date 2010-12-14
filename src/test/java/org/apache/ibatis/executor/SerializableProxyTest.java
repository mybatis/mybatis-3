package org.apache.ibatis.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.ibatis.executor.loader.DeserializedObjectProxy;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.loader.ResultObjectProxy;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.junit.Test;

import domain.blog.Author;
import domain.blog.Section;

public class SerializableProxyTest {
    
  private Author author = new Author(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);

  @Test
  public void shouldCreateAProxyWithTheSamePropertiesAsOriginal() throws Exception {
    Object proxy = ResultObjectProxy.createProxy(author, new ResultLoaderMap(), true, new DefaultObjectFactory());
    Object proxy2 = deserialize(serialize((Serializable) proxy));
    assertEquals(author, proxy2);
  }
  
  @Test
  public void shouldSerizaliceAFullLoadedObjectToOriginalClass() throws Exception {
    Object proxy = ResultObjectProxy.createProxy(author, new ResultLoaderMap(), true, new DefaultObjectFactory());
    Object proxy2 = deserialize(serialize((Serializable) proxy));
    assertEquals(author.getClass(), proxy2.getClass());
  }

  @Test(expected=ExecutorException.class)
  public void shouldFailCallingAnUnloadedProperty() throws Exception {
    // yes, it must go in uppercase
    Author author2 = (Author) DeserializedObjectProxy.createProxy(author, new String[] {"ID"}, new DefaultObjectFactory());
    author2.getId();
  }

  @Test
  public void shouldLetCallALoadedProperty() throws Exception {
    Author author2 = (Author) DeserializedObjectProxy.createProxy(author, new String[] {}, new DefaultObjectFactory());
    assertEquals(999, author2.getId());
  }

  @Test
  public void shouldSerizalizeADeserlizaliedProxy() throws Exception {
    Object proxy = DeserializedObjectProxy.createProxy(author, new String[0], new DefaultObjectFactory());
    Author author2 = (Author) deserialize(serialize((Serializable) proxy));
    assertEquals(author, author2);
    assertFalse(author.getClass().equals(author2.getClass()));
  }

  @Test
  public void shouldGenerateWriteReplace() throws Exception {
    try {
      author.getClass().getDeclaredMethod("writeReplace");
      fail("Author should not have a writeReplace method");
    } catch (NoSuchMethodException e) {
      // ok
    }
    Object proxy = ResultObjectProxy.createProxy(author, new ResultLoaderMap(), true, new DefaultObjectFactory());
    proxy.getClass().getDeclaredMethod("writeReplace");
  }
  
  @Test
  public void shouldNotGenerateWriteReplaceItThereIsAlreadyOne() throws Exception {
    AuthorWithWriteReplaceMethod beanWithWriteReplace = new AuthorWithWriteReplaceMethod(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);
    Object proxy = ResultObjectProxy.createProxy(beanWithWriteReplace, new ResultLoaderMap(), true, new DefaultObjectFactory());
    try {
      beanWithWriteReplace.getClass().getDeclaredMethod("writeReplace");
    } catch (NoSuchMethodException e) {
      fail("Bean should declare a writeReplace method");
    }
    Method m = proxy.getClass().getDeclaredMethod("writeReplace");
    assertFalse(m.isAccessible()); //generated method is public so this one should be protected
  }

  @Test
  public void shouldCreateAProxyForAPartiallyLoadedBean() throws Exception {
    ResultLoaderMap loader = new ResultLoaderMap();
    loader.addLoader("id", null, null);
    Object proxy = ResultObjectProxy.createProxy(author, loader, true, new DefaultObjectFactory());
    Author author2 = (Author) deserialize(serialize((Serializable) proxy));
    assertTrue(author2.getClass().getName().contains("CGLIB"));
  }

  @Test
  public void shouldNotCreateAProxyForAFullyLoadedBean() throws Exception {
    ResultLoaderMap loader = new ResultLoaderMap();
    Object proxy = ResultObjectProxy.createProxy(author, loader, true, new DefaultObjectFactory());
    Author author2 = (Author) deserialize(serialize((Serializable) proxy));
    assertEquals(author.getClass(), author2.getClass());
  }

  @Test(expected=ExecutorException.class)
  public void shouldNotLetReadUnloadedPropertyAfterSerialization() throws Exception {
    ResultLoaderMap loader = new ResultLoaderMap();
    loader.addLoader("id", null, null);
    Object proxy = ResultObjectProxy.createProxy(author, loader, true, new DefaultObjectFactory());
    Author author2 = (Author) deserialize(serialize((Serializable) proxy));
    author2.getId();
  }

  @Test(expected=ExecutorException.class)
  public void shouldNotLetReadUnloadedPropertyAfterTwoSerializations() throws Exception {
    ResultLoaderMap loader = new ResultLoaderMap();
    loader.addLoader("id", null, null);
    Object proxy = ResultObjectProxy.createProxy(author, loader, true, new DefaultObjectFactory());
    Author author2 = (Author) deserialize(serialize(deserialize(serialize((Serializable) proxy))));
    author2.getId();
  }

  @Test
  public void shouldLetReadALoadedPropertyAfterSerialization() throws Exception {
    ResultLoaderMap loader = new ResultLoaderMap();
    Object proxy = ResultObjectProxy.createProxy(author, loader, true, new DefaultObjectFactory());
    byte[] ser = serialize((Serializable) proxy);
    Author author2 = (Author) deserialize(ser);
    assertEquals(999, author2.getId());
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
  
  public static class AuthorWithWriteReplaceMethod extends Author {
      
    public AuthorWithWriteReplaceMethod() {        
    }
    
    public AuthorWithWriteReplaceMethod(Integer id, String username, String password, String email, String bio, Section section) {
        super(id, username, password, email, bio, section);
    }

    protected Object writeReplace() throws ObjectStreamException {
      return this;
    }
  }
}
