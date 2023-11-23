/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * This provides reentrant locking support for our code base. Future worlds like Loom virtual threads don't like
 * synchronised calls since they pin the VT to the carrier thread. Word on the street is that locks are preferred to
 * synchronised.
 */

public class LockKit {

  private static final Map<Object, ReentrantLock> locks = new ConcurrentHashMap<>();

  /**
   * obtain a reentrant lock
   *
   * @param holder
   *
   * @return
   */
  public static ReentrantLock obtainLock(Object holder) {
    return locks.computeIfAbsent(holder, (key) -> new ReentrantLock());
  }

  /**
   * A class to run code inside a reentrant lock
   */
  public static class ReentrantLock {
    private final Lock lock = new java.util.concurrent.locks.ReentrantLock();

    /**
     * Sometimes you need to directly lock things like for checked exceptions
     * <p>
     * It's on you to unlock it!
     */
    public void lock() {
      lock.lock();
    }

    public void unlock() {
      lock.unlock();
    }

    public void runLocked(Runnable codeToRun) {
      lock.lock();
      try {
        codeToRun.run();
      } finally {
        lock.unlock();
      }
    }

    public <E> E callLocked(Supplier<E> codeToRun) {
      lock.lock();
      try {
        return codeToRun.get();
      } finally {
        lock.unlock();
      }
    }
  }

  /**
   * Will allow for lazy computation of some values just once
   */
  public static class ComputedOnce {

    private volatile boolean beenComputed = false;
    private final ReentrantLock lock = new ReentrantLock();

    public boolean hasBeenComputed() {
      return beenComputed;
    }

    public void runOnce(Runnable codeToRunOnce) {
      if (beenComputed) {
        return;
      }
      lock.runLocked(() -> {
        // double lock check
        if (beenComputed) {
          return;
        }
        try {
          codeToRunOnce.run();
          beenComputed = true;
        } finally {
          // even for exceptions we will say its computed
          beenComputed = true;
        }
      });
    }
  }
}
