package org.apache.ibatis.io;

import org.junit.Assert;
import org.junit.Test;

/**
 * Author: jasonleaster
 * Date  : 2017/7/30
 * Email : jasonleaster@gmail.com
 * Description:
 *    Unit test for VFS getInstance method in multi-thread environment
 */
public class VFSTest {

  @Test
  public void getInstanceShouldNotBeNull() throws Exception {
    VFS vsf = VFS.getInstance();
    Assert.assertNotNull(vsf);
  }


  @Test
  public void getInstanceShouldNotBeNullInMultiThreadEnv() throws InterruptedException{
    final int threadCount = 3;

    Thread[] threads = new Thread[threadCount];
    InstanceGetterProcedure[] procedures = new InstanceGetterProcedure[threadCount];

    for (int i = 0; i < threads.length; i++) {
      String threadName = "Thread##"+ i;

      procedures[i] = new InstanceGetterProcedure();
      threads[i] = new Thread(procedures[i], threadName);
    }

    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread :threads)
    {
      thread.join();
    }

    // All caller got must be the same instance
    for (int i = 0; i < threadCount- 1; i++){
      Assert.assertEquals(procedures[i].instanceGot, procedures[i+1].instanceGot);
    }

  }

  private class InstanceGetterProcedure implements Runnable{

    volatile VFS instanceGot;

    @Override
    public void run() {
      instanceGot = VFS.getInstance();
    }
  }
}