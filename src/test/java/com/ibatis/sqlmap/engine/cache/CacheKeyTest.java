package com.ibatis.sqlmap.engine.cache;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.ibatis.cache.CacheKey;

/**
 * CacheKey Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>08/29/2006</pre>
 */
public class CacheKeyTest extends TestCase {
  public CacheKeyTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    super.setUp();
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testUpdate() {
    CacheKey key3 = new CacheKey();

    CacheKey key4 = new CacheKey();

    key3.update("AV");

    key4.update("B7");

    assertTrue(!key3.equals(key4));
    assertTrue(!key3.toString().equals(key4.toString()));

  }

  public static Test suite() {
    return new TestSuite(CacheKeyTest.class);
  }
}
