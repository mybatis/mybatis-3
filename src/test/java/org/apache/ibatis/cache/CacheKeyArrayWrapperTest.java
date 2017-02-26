/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.cache;

import static org.junit.Assert.*;
import org.junit.Test;

import static org.apache.ibatis.cache.CacheKeyArrayWrapper.wrap;

public class CacheKeyArrayWrapperTest {

  @Test
  public void shouldBeEqualWithSameContent() {
    checkSame(wrap(new boolean[] {true}), wrap(new boolean[] {true}));
    checkSame(wrap(new byte[] {1}),       wrap(new byte[] {1}));
    checkSame(wrap(new char[] {'a'}),     wrap(new char[] {'a'}));
    checkSame(wrap(new short[] {1}),      wrap(new short[] {1}));
    checkSame(wrap(new int[] {1}),        wrap(new int[] {1}));
    checkSame(wrap(new long[] {1L}),      wrap(new long[] {1L}));
    checkSame(wrap(new float[] {1.0f}),   wrap(new float[] {1.0f}));
    checkSame(wrap(new double[] {1.0}),   wrap(new double[] {1.0}));
    Object obj = new Object();
    checkSame(wrap(new Object[] {obj}),   wrap(new Object[] {obj}));
  }

  @Test
  public void shouldNotBeEqualWithDifferentContent() {
    assertNotEquals(wrap(new boolean[] {true}), wrap(new boolean[] {false}));
    assertNotEquals(wrap(new byte[] {1}),       wrap(new byte[] {2}));
    assertNotEquals(wrap(new char[] {'a'}),     wrap(new char[] {'b'}));
    assertNotEquals(wrap(new short[] {1}),      wrap(new short[] {2}));
    assertNotEquals(wrap(new int[] {1}),        wrap(new int[] {2}));
    assertNotEquals(wrap(new long[] {1L}),      wrap(new long[] {2L}));
    assertNotEquals(wrap(new float[] {1.0f}),   wrap(new float[] {2.0f}));
    assertNotEquals(wrap(new double[] {1.0}),   wrap(new double[] {2.0}));
    Object obj1 = new Object();
    Object obj2 = new Object();
    assertNotEquals(wrap(new Object[] {obj1}),  wrap(new Object[] {obj2}));
  }


  @Test
  public void shouldNotBeEqualWithBoxedContent() {
    assertNotEquals(wrap(new boolean[] {true}), wrap(new Boolean[] {true}));
    assertNotEquals(wrap(new byte[] {1}),       wrap(new Byte[] {1}));
    assertNotEquals(wrap(new char[] {'a'}),     wrap(new Character[] {'a'}));
    assertNotEquals(wrap(new short[] {1}),      wrap(new Short[] {1}));
    assertNotEquals(wrap(new int[] {1}),        wrap(new Integer[] {1}));
    assertNotEquals(wrap(new long[] {1L}),      wrap(new Long[] {1L}));
    assertNotEquals(wrap(new float[] {1.0f}),   wrap(new Float[] {1.0f}));
    assertNotEquals(wrap(new double[] {1.0}),   wrap(new Double[] {1.0}));
  }

  private void checkSame(Object a, Object b) {
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertEquals(a.toString(), b.toString());
  }
}
