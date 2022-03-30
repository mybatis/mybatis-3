/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.io;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test for VFS getInstance method in multi-thread environment
 *
 * @author jasonleaster
 */
class VFSTest {

  @Test
  void getInstanceShouldNotBeNull() {
    VFS vsf = VFS.getInstance();
    Assertions.assertNotNull(vsf);
  }

  @Test
  void getInstanceShouldNotBeNullInMultiThreadEnv() throws InterruptedException {
    final int threadCount = 3;

    Thread[] threads = new Thread[threadCount];
    InstanceGetterProcedure[] procedures = new InstanceGetterProcedure[threadCount];

    for (int i = 0; i < threads.length; i++) {
      String threadName = "Thread##" + i;

      procedures[i] = new InstanceGetterProcedure();
      threads[i] = new Thread(procedures[i], threadName);
    }

    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    // All caller got must be the same instance
    for (int i = 0; i < threadCount - 1; i++) {
      Assertions.assertEquals(procedures[i].instanceGot, procedures[i + 1].instanceGot);
    }
  }

  @Test
  void getExistMethod() {
    Method method = VFS.getMethod(VFS.class, "list", String.class);
    Assertions.assertNotNull(method);
  }

  @Test
  void getNotExistMethod() {
    Method method = VFS.getMethod(VFS.class, "listXxx", String.class);
    Assertions.assertNull(method);
  }

  @Test
  void invoke() throws IOException, NoSuchMethodException {
    VFS vfs = VFS.invoke(VFS.class.getMethod("getInstance"), VFS.class);
    Assertions.assertEquals(vfs, VFS.getInstance());

    Assertions.assertThrows(RuntimeException.class, () -> {
      //java.lang.IllegalArgumentException: wrong number of arguments
      VFS.invoke(VFS.class.getMethod("getInstance"), VFS.class, "unnecessaryArgument");
    });

    Assertions.assertThrows(IOException.class, () -> {
      //InvocationTargetException.getTargetException -> IOException
      VFS.invoke(Resources.class.getMethod("getResourceAsProperties", String.class), Resources.class, "invalidResource");
    });

    Assertions.assertThrows(RuntimeException.class, () -> {
      //Other InvocationTargetException
      VFS.invoke(Integer.class.getMethod("valueOf", String.class), Resources.class, "InvalidIntegerNumber");
    });

  }

  private class InstanceGetterProcedure implements Runnable {

    volatile VFS instanceGot;

    @Override
    public void run() {
      instanceGot = VFS.getInstance();
    }
  }
}
