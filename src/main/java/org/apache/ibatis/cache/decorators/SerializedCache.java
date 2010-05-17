package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;

import java.io.*;
import java.util.concurrent.locks.ReadWriteLock;

public class SerializedCache implements Cache {

  private Cache delegate;

  public SerializedCache(Cache delegate) {
    this.delegate = delegate;
  }

  public String getId() {
    return delegate.getId();
  }

  public int getSize() {
    return delegate.getSize();
  }

  public void putObject(Object key, Object object) {
    if (object == null || object instanceof Serializable) {
      delegate.putObject(key, serialize((Serializable) object));
    } else {
      throw new CacheException("SharedCache failed to make a copy of a non-serializable object: " + object);
    }
  }

  public Object getObject(Object key) {
    Object object = delegate.getObject(key);
    return object == null ? null : deserialize((byte[]) object);
  }

  public Object removeObject(Object key) {
    return delegate.removeObject(key);
  }

  public void clear() {
    delegate.clear();
  }

  public ReadWriteLock getReadWriteLock() {
    return delegate.getReadWriteLock();
  }

  public int hashCode() {
    return delegate.hashCode();
  }

  public boolean equals(Object obj) {
    return delegate.equals(obj);
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
      throw new CacheException("Error serializing object.  Cause: " + e, e);
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
      throw new CacheException("Error deserializing object.  Cause: " + e, e);
    }
    return result;
  }


}
