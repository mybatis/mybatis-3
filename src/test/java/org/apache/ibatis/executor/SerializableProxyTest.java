package org.apache.ibatis.executor;

import domain.blog.Author;
import domain.blog.Section;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.executor.loader.ResultObjectProxy;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.*;

public class SerializableProxyTest {

  @Test
  public void shouldDemonstrateSerializableEnhancer() throws Exception {
    Author author = new Author(999, "someone", "!@#@!#!@#", "someone@somewhere.com", "blah", Section.NEWS);
    Object proxy = ResultObjectProxy.createProxy(author, new ResultLoaderMap(), true);
    byte[] bytes = serialize((Serializable) proxy);
    Object proxy2 = deserialize(bytes);
    assertEquals(author.toString(), proxy2.toString());
  }

  private byte[] serialize(Serializable value) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(value);
      oos.flush();
      oos.close();
      return bos.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Error serializing object.  Cause: " + e, e);
    }
  }

  private Serializable deserialize(byte[] value) {
    Serializable result;
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream((byte[]) value);
      ObjectInputStream ois = new ObjectInputStream(bis);
      result = (Serializable) ois.readObject();
      ois.close();
    } catch (Exception e) {
      throw new RuntimeException("Error deserializing object.  Cause: " + e, e);
    }
    return result;
  }

}
