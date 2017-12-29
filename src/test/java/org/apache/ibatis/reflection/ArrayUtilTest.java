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

package org.apache.ibatis.reflection;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class ArrayUtilTest extends ArrayUtil {

  @Test
  public void testHashCode() throws Exception {
    Object arr;
    arr = new long[] { 1 };
    assertEquals(Arrays.hashCode((long[]) arr), ArrayUtil.hashCode(arr));
    arr = new int[] { 1 };
    assertEquals(Arrays.hashCode((int[]) arr), ArrayUtil.hashCode(arr));
    arr = new short[] { 1 };
    assertEquals(Arrays.hashCode((short[]) arr), ArrayUtil.hashCode(arr));
    arr = new char[] { 1 };
    assertEquals(Arrays.hashCode((char[]) arr), ArrayUtil.hashCode(arr));
    arr = new byte[] { 1 };
    assertEquals(Arrays.hashCode((byte[]) arr), ArrayUtil.hashCode(arr));
    arr = new boolean[] { true };
    assertEquals(Arrays.hashCode((boolean[]) arr), ArrayUtil.hashCode(arr));
    arr = new float[] { 1f };
    assertEquals(Arrays.hashCode((float[]) arr), ArrayUtil.hashCode(arr));
    arr = new double[] { 1d };
    assertEquals(Arrays.hashCode((double[]) arr), ArrayUtil.hashCode(arr));
    arr = new Object[] { "str" };
    assertEquals(Arrays.hashCode((Object[]) arr), ArrayUtil.hashCode(arr));

    assertEquals(0, ArrayUtil.hashCode(null));
    assertEquals("str".hashCode(), ArrayUtil.hashCode("str"));
    assertEquals(Integer.valueOf(1).hashCode(), ArrayUtil.hashCode(1));
  }

  @Test
  public void testequals() throws Exception {
    assertTrue(ArrayUtil.equals(new long[] { 1 }, new long[] { 1 }));
    assertTrue(ArrayUtil.equals(new int[] { 1 }, new int[] { 1 }));
    assertTrue(ArrayUtil.equals(new short[] { 1 }, new short[] { 1 }));
    assertTrue(ArrayUtil.equals(new char[] { 1 }, new char[] { 1 }));
    assertTrue(ArrayUtil.equals(new byte[] { 1 }, new byte[] { 1 }));
    assertTrue(ArrayUtil.equals(new boolean[] { true }, new boolean[] { true }));
    assertTrue(ArrayUtil.equals(new float[] { 1f }, new float[] { 1f }));
    assertTrue(ArrayUtil.equals(new double[] { 1d }, new double[] { 1d }));
    assertTrue(ArrayUtil.equals(new Object[] { "str" }, new Object[] { "str" }));

    assertFalse(ArrayUtil.equals(new long[] { 1 }, new long[] { 2 }));
    assertFalse(ArrayUtil.equals(new int[] { 1 }, new int[] { 2 }));
    assertFalse(ArrayUtil.equals(new short[] { 1 }, new short[] { 2 }));
    assertFalse(ArrayUtil.equals(new char[] { 1 }, new char[] { 2 }));
    assertFalse(ArrayUtil.equals(new byte[] { 1 }, new byte[] { 2 }));
    assertFalse(ArrayUtil.equals(new boolean[] { true }, new boolean[] { false }));
    assertFalse(ArrayUtil.equals(new float[] { 1f }, new float[] { 2f }));
    assertFalse(ArrayUtil.equals(new double[] { 1d }, new double[] { 2d }));
    assertFalse(ArrayUtil.equals(new Object[] { "str" }, new Object[] { "rts" }));

    assertTrue(ArrayUtil.equals(null, null));
    assertFalse(ArrayUtil.equals(new long[] { 1 }, null));
    assertFalse(ArrayUtil.equals(null, new long[] { 1 }));

    assertTrue(ArrayUtil.equals(1, 1));
    assertTrue(ArrayUtil.equals("str", "str"));
  }

  @Test
  public void testToString() throws Exception {
    Object arr;
    arr = new long[] { 1 };
    assertEquals(Arrays.toString((long[]) arr), ArrayUtil.toString(arr));
    arr = new int[] { 1 };
    assertEquals(Arrays.toString((int[]) arr), ArrayUtil.toString(arr));
    arr = new short[] { 1 };
    assertEquals(Arrays.toString((short[]) arr), ArrayUtil.toString(arr));
    arr = new char[] { 1 };
    assertEquals(Arrays.toString((char[]) arr), ArrayUtil.toString(arr));
    arr = new byte[] { 1 };
    assertEquals(Arrays.toString((byte[]) arr), ArrayUtil.toString(arr));
    arr = new boolean[] { true };
    assertEquals(Arrays.toString((boolean[]) arr), ArrayUtil.toString(arr));
    arr = new float[] { 1f };
    assertEquals(Arrays.toString((float[]) arr), ArrayUtil.toString(arr));
    arr = new double[] { 1d };
    assertEquals(Arrays.toString((double[]) arr), ArrayUtil.toString(arr));
    arr = new Object[] { "str" };
    assertEquals(Arrays.toString((Object[]) arr), ArrayUtil.toString(arr));

    assertEquals(Integer.valueOf(1).toString(), ArrayUtil.toString(1));
    assertEquals("null", ArrayUtil.toString(null));
  }

}
