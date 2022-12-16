package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.lang.reflect.Array;

/**
 * Array types represent Java arrays, both primitive and object valued.
 * Further, Object-valued arrays can have element type of any other
 * legal {@link JavaType}.
 */
public final class ArrayType
    extends TypeBase
{
    private static final long serialVersionUID = 1L;

    /**
     * Type of elements in the array.
     */
    protected final JavaType _componentType;

    /**
     * We will also keep track of shareable instance of empty array,
     * since it usually needs to be constructed any way; and because
     * it is essentially immutable and thus can be shared.
     */
    protected final Object _emptyArray;

    protected ArrayType(JavaType componentType, TypeBindings bindings, Object emptyInstance,
            Object valueHandler, Object typeHandler, boolean asStatic)
    {
        // No super-class, interfaces, for now
        super(emptyInstance.getClass(), bindings, null, null,
                componentType.hashCode(),
                valueHandler, typeHandler, asStatic);
        _componentType = componentType;
        _emptyArray = emptyInstance;
    }

    public static ArrayType construct(JavaType componentType, TypeBindings bindings) {
        return construct(componentType, bindings, null, null);
    }

    public static ArrayType construct(JavaType componentType, TypeBindings bindings,
            Object valueHandler, Object typeHandler) {
        // Figuring out raw class for generic array is actually bit tricky...
        Object emptyInstance = Array.newInstance(componentType.getRawClass(), 0);
        return new ArrayType(componentType, bindings, emptyInstance, valueHandler, typeHandler, false);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        Object emptyInstance = Array.newInstance(contentType.getRawClass(), 0);
        return new ArrayType(contentType, _bindings, emptyInstance,
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public ArrayType withTypeHandler(Object h)
    {
        if (h == _typeHandler) {
            return this;
        }
        return new ArrayType(_componentType, _bindings, _emptyArray, _valueHandler, h, _asStatic);
    }

    @Override
    public ArrayType withContentTypeHandler(Object h)
    {
        if (h == _componentType.<Object>getTypeHandler()) {
            return this;
        }
        return new ArrayType(_componentType.withTypeHandler(h), _bindings, _emptyArray,
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public ArrayType withValueHandler(Object h) {
        if (h == _valueHandler) {
            return this;
        }
        return new ArrayType(_componentType, _bindings, _emptyArray, h, _typeHandler,_asStatic);
    }

    @Override
    public ArrayType withContentValueHandler(Object h) {
        if (h == _componentType.<Object>getValueHandler()) {
            return this;
        }
        return new ArrayType(_componentType.withValueHandler(h), _bindings, _emptyArray,
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public ArrayType withStaticTyping() {
        if (_asStatic) {
            return this;
        }
        return new ArrayType(_componentType.withStaticTyping(), _bindings,
                _emptyArray, _valueHandler, _typeHandler, true);
    }

    /*
    /**********************************************************
    /* Methods for narrowing conversions
    /**********************************************************
     */

    /**
     * Handling of narrowing conversions for arrays is trickier: for now,
     * it is not even allowed.
     */
    @Override
    @Deprecated // since 2.7
    protected JavaType _narrow(Class<?> subclass) {
        return _reportUnsupported();
    }

    // Should not be called, as array types in Java are not extensible; but
    // let's not freak out even if it is called?
    @Override
    public JavaType refine(Class<?> contentClass, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces) {
        return null;
    }

    private JavaType _reportUnsupported() {
        throw new UnsupportedOperationException("Cannot narrow or widen array types");
    }

    /*
    /**********************************************************
    /* Overridden methods
    /**********************************************************
     */

    @Override
    public boolean isArrayType() { return true; }

    /**
     * For some odd reason, modifiers for array classes would
     * claim they are abstract types. Not so, at least for our
     * purposes.
     */
    @Override
    public boolean isAbstract() { return false; }

    /**
     * For some odd reason, modifiers for array classes would
     * claim they are abstract types. Not so, at least for our
     * purposes.
     */
    @Override
    public boolean isConcrete() { return true; }

    @Override
    public boolean hasGenericTypes() {
        // arrays are not parameterized, but element type may be:
        return _componentType.hasGenericTypes();
    }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    @Override
    public boolean isContainerType() { return true; }

    @Override
    public JavaType getContentType() { return  _componentType; }

    @Override
    public Object getContentValueHandler() {
        return _componentType.getValueHandler();
    }

    @Override
    public Object getContentTypeHandler() {
        return _componentType.getTypeHandler();
    }

    @Override
    public boolean hasHandlers() {
        return super.hasHandlers() || _componentType.hasHandlers();
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        sb.append('[');
        return _componentType.getGenericSignature(sb);
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        sb.append('[');
        return _componentType.getErasedSignature(sb);
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * @since 2.12
     */
    public Object[] getEmptyArray() {
        return  (Object[]) _emptyArray;
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public String toString()
    {
        return "[array type, component type: "+_componentType+"]";
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;

        ArrayType other = (ArrayType) o;
        return _componentType.equals(other._componentType);
    }
}
