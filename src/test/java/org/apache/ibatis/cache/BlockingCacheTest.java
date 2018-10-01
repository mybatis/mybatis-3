package org.apache.ibatis.cache;

import org.apache.ibatis.cache.decorators.BlockingCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kezhenxu at 2018/9/17 21:21
 *
 * @author kezhenxu (kezhenxu94 at 163 dot com)
 */
public class BlockingCacheTest {

    @Test(timeout = 3000L)
    public void getObject() throws InterruptedException {
        final BlockingCache cache = new BlockingCache(new PerpetualCache("default"));

        // lock twice
        cache.getObject(1);
        cache.getObject(1);

        Thread thread = new Thread(() -> Assert.assertEquals(cache.getObject(1), 1));
        thread.setDaemon(false);
        thread.start();

        // but release once
        cache.putObject(1, 1);

        thread.join();
    }
}
