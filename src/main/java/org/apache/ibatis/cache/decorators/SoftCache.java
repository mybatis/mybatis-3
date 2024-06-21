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
package org.apache.ibatis.cache.decorators;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheDecorator;

/**
 * Soft Reference cache decorator.
 * <p>
 * Thanks to Dr. Heinz Kabutz for his guidance here.
 *
 * @author Clinton Begin
 */
public class SoftCache extends CacheDecorator {
  private final Deque<Object> hardLinksToAvoidGarbageCollection;
  private final ReferenceQueue<Object> queueOfGarbageCollectedEntries;
  private int numberOfHardLinks;
  private final ReentrantLock lock = new ReentrantLock();

  public SoftCache(Cache delegate) {
    super(delegate);
    this.numberOfHardLinks = 256;
    this.hardLinksToAvoidGarbageCollection = new LinkedList<>();
    this.queueOfGarbageCollectedEntries = new ReferenceQueue<>();
  }

  @Override
  public int getSize() {
    removeGarbageCollectedItems();
    return super.getSize();
  }

  public void setSize(int size) {
    this.numberOfHardLinks = size;
  }

  @Override
  public void putObject(Object key, Object value) {
    removeGarbageCollectedItems();
    super.putObject(key, new SoftEntry(key, value, queueOfGarbageCollectedEntries));
  }

  @Override
  public Object getObject(Object key) {
    Object result = null;
    @SuppressWarnings("unchecked") // assumed delegate cache is totally managed by this cache
    SoftReference<Object> softReference = (SoftReference<Object>) super.getObject(key);
    if (softReference != null) {
      result = softReference.get();
      if (result == null) {
        super.removeObject(key);
      } else {
        // See #586 (and #335) modifications need more than a read lock
        lock.lock();
        try {
          hardLinksToAvoidGarbageCollection.addFirst(result);
          if (hardLinksToAvoidGarbageCollection.size() > numberOfHardLinks) {
            hardLinksToAvoidGarbageCollection.removeLast();
          }
        } finally {
          lock.unlock();
        }
      }
    }
    return result;
  }

  @Override
  public Object removeObject(Object key) {
    removeGarbageCollectedItems();
    @SuppressWarnings("unchecked")
    SoftReference<Object> softReference = (SoftReference<Object>) super.removeObject(key);
    return softReference == null ? null : softReference.get();
  }

  @Override
  public void clear() {
    lock.lock();
    try {
      hardLinksToAvoidGarbageCollection.clear();
    } finally {
      lock.unlock();
    }
    removeGarbageCollectedItems();
    super.clear();
  }

  private void removeGarbageCollectedItems() {
    SoftEntry sv;
    while ((sv = (SoftEntry) queueOfGarbageCollectedEntries.poll()) != null) {
      super.removeObject(sv.key);
    }
  }

  private static class SoftEntry extends SoftReference<Object> {
    private final Object key;

    SoftEntry(Object key, Object value, ReferenceQueue<Object> garbageCollectionQueue) {
      super(value, garbageCollectionQueue);
      this.key = key;
    }
  }

}
