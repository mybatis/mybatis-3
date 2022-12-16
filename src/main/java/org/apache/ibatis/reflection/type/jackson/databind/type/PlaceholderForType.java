package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

/**
 * Helper type used when introspecting bindings for already resolved types,
 * needed for specialization.
 *
 * @since 2.8.11
 */
public class PlaceholderForType extends TypeBase
{
    private static final long serialVersionUID = 1L;

    protected final int _ordinal;

    /**
     * Type assigned during wildcard resolution (which follows type
     * structure resolution)
     */
    protected JavaType _actualType;

    public PlaceholderForType(int ordinal)
    {
        super(Object.class, TypeBindings.emptyBindings(),
                TypeFactory.unknownType(), null, 1, // super-class, super-interfaces, hashCode
                null, null, false); // value/type handler, as-static
        _ordinal = ordinal;
    }

    public JavaType actualType() { return _actualType; }
    public void actualType(JavaType t) { _actualType = t; }

    // Override to get better diagnostics
    @Override
    protected String buildCanonicalName() {
        return toString();
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        return getErasedSignature(sb);
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        sb.append('$').append(_ordinal+1);
        return sb;
    }

    @Override
    public JavaType withTypeHandler(Object h) {
        return _unsupported();
    }

    @Override
    public JavaType withContentTypeHandler(Object h) {
        return _unsupported();
    }

    @Override
    public JavaType withValueHandler(Object h) {
        return _unsupported();
    }

    @Override
    public JavaType withContentValueHandler(Object h) {
        return _unsupported();
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        return _unsupported();
    }

    @Override
    public JavaType withStaticTyping() {
        return _unsupported();
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
        return _unsupported();
    }

    @Override
    @Deprecated // since 2.7
    protected JavaType _narrow(Class<?> subclass) {
        return _unsupported();
    }

    @Override
    public boolean isContainerType() {
        return false;
    }

    @Override
    public String toString() {
        return getErasedSignature(new StringBuilder()).toString();
    }

    @Override
    public boolean equals(Object o) {
        return (o == this);
    }

    private <T> T _unsupported() {
        throw new UnsupportedOperationException("Operation should not be attempted on "+getClass().getName());
    }
}
