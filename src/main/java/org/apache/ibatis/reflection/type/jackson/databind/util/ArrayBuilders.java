package org.apache.ibatis.reflection.type.jackson.databind.util;

import java.lang.reflect.Array;
import java.util.HashSet;

/**
 * Helper class that contains set of distinct builders for different
 * arrays of primitive values. It also provides trivially simple
 * reuse scheme, which assumes that caller knows not to use instances
 * concurrently (which works ok with primitive arrays since they can
 * not contain other non-primitive types).
 * Also note that instances are not thread safe; the intent is that
 * a builder is constructed on per-call (deserialization) basis.
 */
public final class ArrayBuilders
{
    private BooleanBuilder _booleanBuilder = null;

    // note: no need for char[] builder, assume they are Strings

    private ByteBuilder _byteBuilder = null;
    private ShortBuilder _shortBuilder = null;
    private IntBuilder _intBuilder = null;
    private LongBuilder _longBuilder = null;

    private FloatBuilder _floatBuilder = null;
    private DoubleBuilder _doubleBuilder = null;

    public ArrayBuilders() { }

    public BooleanBuilder getBooleanBuilder()
    {
        if (_booleanBuilder == null) {
            _booleanBuilder = new BooleanBuilder();
        }
        return _booleanBuilder;
    }

    public ByteBuilder getByteBuilder()
    {
        if (_byteBuilder == null) {
            _byteBuilder = new ByteBuilder();
        }
        return _byteBuilder;
    }
    public ShortBuilder getShortBuilder()
    {
        if (_shortBuilder == null) {
            _shortBuilder = new ShortBuilder();
        }
        return _shortBuilder;
    }
    public IntBuilder getIntBuilder()
    {
        if (_intBuilder == null) {
            _intBuilder = new IntBuilder();
        }
        return _intBuilder;
    }
    public LongBuilder getLongBuilder()
    {
        if (_longBuilder == null) {
            _longBuilder = new LongBuilder();
        }
        return _longBuilder;
    }

    public FloatBuilder getFloatBuilder()
    {
        if (_floatBuilder == null) {
            _floatBuilder = new FloatBuilder();
        }
        return _floatBuilder;
    }
    public DoubleBuilder getDoubleBuilder()
    {
        if (_doubleBuilder == null) {
            _doubleBuilder = new DoubleBuilder();
        }
        return _doubleBuilder;
    }

    /*
    /**********************************************************
    /* Impl classes
    /**********************************************************
     */

    public final static class BooleanBuilder
        extends PrimitiveArrayBuilder<boolean[]>
    {
        public BooleanBuilder() { }
        @Override
        public final boolean[] _constructArray(int len) { return new boolean[len]; }
    }

    public final static class ByteBuilder
        extends PrimitiveArrayBuilder<byte[]>
    {
        public ByteBuilder() { }
        @Override
        public final byte[] _constructArray(int len) { return new byte[len]; }
    }
    public final static class ShortBuilder
        extends PrimitiveArrayBuilder<short[]>
    {
        public ShortBuilder() { }
        @Override
        public final short[] _constructArray(int len) { return new short[len]; }
    }
    public final static class IntBuilder
        extends PrimitiveArrayBuilder<int[]>
    {
        public IntBuilder() { }
        @Override
        public final int[] _constructArray(int len) { return new int[len]; }
    }
    public final static class LongBuilder
        extends PrimitiveArrayBuilder<long[]>
    {
        public LongBuilder() { }
        @Override
        public final long[] _constructArray(int len) { return new long[len]; }
    }

    public final static class FloatBuilder
        extends PrimitiveArrayBuilder<float[]>
    {
        public FloatBuilder() { }
        @Override
        public final float[] _constructArray(int len) { return new float[len]; }
    }
    public final static class DoubleBuilder
        extends PrimitiveArrayBuilder<double[]>
    {
        public DoubleBuilder() { }
        @Override
        public final double[] _constructArray(int len) { return new double[len]; }
    }

    /*
    /**********************************************************
    /* Static helper methods
    /**********************************************************
     */

    /**
     * Helper method used for constructing simple value comparator used for
     * comparing arrays for content equality.
     *<p>
     * Note: current implementation is not optimized for speed; if performance
     * ever becomes an issue, it is possible to construct much more efficient
     * typed instances (one for Object[] and sub-types; one per primitive type).
     *
     * @since 2.2 Moved from earlier <code>Comparators</code> class
     */
    public static Object getArrayComparator(final Object defaultValue)
    {
        final int length = Array.getLength(defaultValue);
        final Class<?> defaultValueType = defaultValue.getClass();
        return new Object() {
            @Override
            public boolean equals(Object other) {
                if (other == this) return true;
                if (!ClassUtil.hasClass(other, defaultValueType)) {
                    return false;
                }
                if (Array.getLength(other) != length) return false;
                // so far so good: compare actual equality; but only shallow one
                for (int i = 0; i < length; ++i) {
                    Object value1 = Array.get(defaultValue, i);
                    Object value2 = Array.get(other, i);
                    if (value1 == value2) continue;
                    if (value1 != null) {
                        if (!value1.equals(value2)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        };
    }

    public static <T> HashSet<T> arrayToSet(T[] elements)
    {
        if (elements != null) {
            int len = elements.length;
            HashSet<T> result = new HashSet<T>(len);
            for (int i = 0; i < len; ++i) {
                result.add(elements[i]);
            }
            return result;
        }
        return new HashSet<T>();
    }

    /**
     * Helper method for constructing a new array that contains specified
     * element followed by contents of the given array but never contains
     * duplicates.
     * If element already existed, one of two things happens: if the element
     * was already the first one in array, array is returned as is; but
     * if not, a new copy is created in which element has moved as the head.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] insertInListNoDup(T[] array, T element)
    {
        final int len = array.length;

        // First: see if the element already exists
        for (int ix = 0; ix < len; ++ix) {
            if (array[ix] == element) {
                // if at head already, return as is
                if (ix == 0) {
                    return array;
                }
                // otherwise move things around
                T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), len);
                System.arraycopy(array, 0, result, 1, ix);
                result[0] = element;
                ++ix;
                int left = len - ix;
                if (left > 0) {
                	System.arraycopy(array, ix, result, ix, left);
                }
                return result;
            }
        }

        // but if not, allocate new array, move
        T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), len+1);
        if (len > 0) {
            System.arraycopy(array, 0, result, 1, len);
        }
        result[0] = element;
        return result;
    }
}
