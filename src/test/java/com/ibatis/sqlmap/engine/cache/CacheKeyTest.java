/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.ibatis.sqlmap.engine.cache;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.ibatis.cache.CacheKey;

/*
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
