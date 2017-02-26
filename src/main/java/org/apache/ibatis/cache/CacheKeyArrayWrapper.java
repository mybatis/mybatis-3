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

import java.util.Arrays;

class CacheKeyArrayWrapper {

  static CacheKeyArrayWrapper wrap(Object unwrapped) {
    if (unwrapped == null || !unwrapped.getClass().isArray()) {
      throw new IllegalArgumentException("Expected object of array type, but got " + unwrapped);
    }

    Class<?> componentType = unwrapped.getClass().getComponentType();

    if (componentType == Boolean.TYPE) {
      return new WrapBooleanArray((boolean[]) unwrapped);
    } else if (componentType == Byte.TYPE) {
      return new WrapByteArray((byte[]) unwrapped);
    } else if (componentType == Character.TYPE) {
      return new WrapCharacterArray((char[]) unwrapped);
    } else if (componentType == Short.TYPE) {
      return new WrapShortArray((short[]) unwrapped);
    } else if (componentType == Integer.TYPE) {
      return new WrapIntegerArray((int[]) unwrapped);
    } else if (componentType == Long.TYPE) {
      return new WrapLongArray((long[]) unwrapped);
    } else if (componentType == Float.TYPE) {
      return new WrapFloatArray((float[]) unwrapped);
    } else if (componentType == Double.TYPE) {
      return new WrapDoubleArray((double[]) unwrapped);
    } else {
      return new WrapObjectArray((Object[]) unwrapped);
    }
  }

  private static final class WrapObjectArray extends CacheKeyArrayWrapper {
    private final Object[] source;

    WrapObjectArray(Object[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapObjectArray) &&
          Arrays.equals(source, ((WrapObjectArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }

  private static final class WrapBooleanArray extends CacheKeyArrayWrapper {
    private final boolean[] source;

    WrapBooleanArray(boolean[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapBooleanArray) &&
          Arrays.equals(source, ((WrapBooleanArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }

  private static final class WrapByteArray extends CacheKeyArrayWrapper {
    private final byte[] source;

    WrapByteArray(byte[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapByteArray) &&
          Arrays.equals(source, ((WrapByteArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }

  private static final class WrapCharacterArray extends CacheKeyArrayWrapper {
    private final char[] source;

    WrapCharacterArray(char[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapCharacterArray) &&
          Arrays.equals(source, ((WrapCharacterArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }

  private static final class WrapShortArray extends CacheKeyArrayWrapper {
    private final short[] source;

    WrapShortArray(short[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapShortArray) &&
          Arrays.equals(source, ((WrapShortArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }

  private static final class WrapIntegerArray extends CacheKeyArrayWrapper {
    private final int[] source;

    WrapIntegerArray(int[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapIntegerArray) &&
          Arrays.equals(source, ((WrapIntegerArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }

  private static final class WrapLongArray extends CacheKeyArrayWrapper {
    private final long[] source;

    WrapLongArray(long[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapLongArray) &&
          Arrays.equals(source, ((WrapLongArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }

  private static final class WrapFloatArray extends CacheKeyArrayWrapper {
    private final float[] source;

    WrapFloatArray(float[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapFloatArray) &&
          Arrays.equals(source, ((WrapFloatArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }

  private static final class WrapDoubleArray extends CacheKeyArrayWrapper {
    private final double[] source;

    WrapDoubleArray(double[] source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      return (other instanceof WrapDoubleArray) &&
          Arrays.equals(source, ((WrapDoubleArray) other).source);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(source);
    }

    @Override
    public String toString() {
      return Arrays.toString(source);
    }
  }
}
