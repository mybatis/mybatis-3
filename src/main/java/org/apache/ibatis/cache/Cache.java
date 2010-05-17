package org.apache.ibatis.cache;

import java.util.concurrent.locks.ReadWriteLock;

public interface Cache {

  String getId();

  int getSize();

  void putObject(Object key, Object value);

  Object getObject(Object key);

  Object removeObject(Object key);

  void clear();

  ReadWriteLock getReadWriteLock();

}