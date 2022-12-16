package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.util.Objects;

/**
 * Specialized {@link SimpleType} for types that are referential types,
 * that is, values that can be dereferenced to another value (or null),
 * of different type.
 * Referenced type is accessible using {@link #getContentType()}.
 *
 * @since 2.6
 */
public class ReferenceType extends SimpleType
{
    private static final long serialVersionUID = 1L;

    protected final JavaType _referencedType;

    /**
     * Essential type used for type ids, for example if type id is needed for
     * referencing type with polymorphic handling. Typically initialized when
     * a {@link SimpleType} is upgraded into reference type, but NOT changed
     * if being sub-classed.
     *
     * @since 2.8
     */
    protected final JavaType _anchorType;

    protected ReferenceType(Class<?> cls, TypeBindings bindings,
            JavaType superClass, JavaType[] superInts, JavaType refType,
            JavaType anchorType,
            Object valueHandler, Object typeHandler, boolean asStatic)
    {
        super(cls, bindings, superClass, superInts, Objects.hashCode(refType),
                valueHandler, typeHandler, asStatic);
        _referencedType = refType;
        _anchorType = (anchorType == null) ? this : anchorType;
    }

    /**
     * Constructor used when upgrading into this type (via {@link #upgradeFrom},
     * the usual way for {@link ReferenceType}s to come into existence.
     * Sets up what is considered the "base" reference type
     *
     * @since 2.7
     */
    protected ReferenceType(TypeBase base, JavaType refType)
    {
        super(base);
        _referencedType = refType;
        // we'll establish this as the anchor type
        _anchorType = this;
    }

    /**
     * Factory method that can be used to "upgrade" a basic type into collection-like
     * one; usually done via {@link TypeModifier}
     *
     * @param baseType Resolved non-reference type (usually {@link SimpleType}) that is being upgraded
     * @param refdType Referenced type; usually the first and only type parameter, but not necessarily
     *
     * @since 2.7
     */
    public static ReferenceType upgradeFrom(JavaType baseType, JavaType refdType) {
        if (refdType == null) {
            throw new IllegalArgumentException("Missing referencedType");
        }
        // 19-Oct-2015, tatu: Not sure if and how other types could be used as base;
        //    will cross that bridge if and when need be
        if (baseType instanceof TypeBase) {
            return new ReferenceType((TypeBase) baseType, refdType);
        }
        throw new IllegalArgumentException("Cannot upgrade from an instance of "+baseType.getClass());
    }

    /**
     * @since 2.7
     */
    public static ReferenceType construct(Class<?> cls, TypeBindings bindings,
            JavaType superClass, JavaType[] superInts, JavaType refType)
    {
        return new ReferenceType(cls, bindings, superClass, superInts,
                refType, null, null, null, false);
    }

    @Deprecated // since 2.7
    public static ReferenceType construct(Class<?> cls, JavaType refType) {
        return new ReferenceType(cls, TypeBindings.emptyBindings(),
                // !!! TODO: missing supertypes
                null, null, null, refType, null, null, false);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        if (_referencedType == contentType) {
            return this;
        }
        return new ReferenceType(_class, _bindings, _superClass, _superInterfaces,
                contentType, _anchorType, _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public ReferenceType withTypeHandler(Object h)
    {
        if (h == _typeHandler) {
            return this;
        }
        return new ReferenceType(_class, _bindings, _superClass, _superInterfaces,
                _referencedType, _anchorType, _valueHandler, h, _asStatic);
    }

    @Override
    public ReferenceType withContentTypeHandler(Object h)
    {
        if (h == _referencedType.<Object>getTypeHandler()) {
            return this;
        }
        return new ReferenceType(_class, _bindings, _superClass, _superInterfaces,
                _referencedType.withTypeHandler(h), _anchorType,
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public ReferenceType withValueHandler(Object h) {
        if (h == _valueHandler) {
            return this;
        }
        return new ReferenceType(_class, _bindings,
                _superClass, _superInterfaces, _referencedType, _anchorType,
                h, _typeHandler,_asStatic);
    }

    @Override
    public ReferenceType withContentValueHandler(Object h) {
        if (h == _referencedType.<Object>getValueHandler()) {
            return this;
        }
        JavaType refdType = _referencedType.withValueHandler(h);
        return new ReferenceType(_class, _bindings,
                _superClass, _superInterfaces, refdType, _anchorType,
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public ReferenceType withStaticTyping() {
        if (_asStatic) {
            return this;
        }
        return new ReferenceType(_class, _bindings, _superClass, _superInterfaces,
                _referencedType.withStaticTyping(), _anchorType,
                 _valueHandler, _typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces) {
        return new ReferenceType(rawType, _bindings,
                superClass, superInterfaces, _referencedType, _anchorType,
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    protected String buildCanonicalName()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(_class.getName());
        if ((_referencedType != null) && _hasNTypeParameters(1)) {
            sb.append('<');
            sb.append(_referencedType.toCanonical());
            sb.append('>');
        }
        return sb.toString();
    }

    /*
    /**********************************************************
    /* Narrow/widen
    /**********************************************************
     */

    @Override
    @Deprecated // since 2.7
    protected JavaType _narrow(Class<?> subclass)
    {
        // Should we check that there is a sub-class relationship?
        return new ReferenceType(subclass, _bindings,
                _superClass, _superInterfaces, _referencedType, _anchorType,
                _valueHandler, _typeHandler, _asStatic);
    }

    /*
    /**********************************************************
    /* Public API overrides
    /**********************************************************
     */

    @Override
    public JavaType getContentType() {
        return _referencedType;
    }

    @Override
    public JavaType getReferencedType() {
        return _referencedType;
    }

    @Override
    public boolean hasContentType() {
        return true;
    }

    @Override
    public boolean isReferenceType() {
        return true;
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        return _classSignature(_class, sb, true);
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb)
    {
        _classSignature(_class, sb, false);
        sb.append('<');
        sb = _referencedType.getGenericSignature(sb);
        sb.append(">;");
        return sb;
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    public JavaType getAnchorType() {
        return _anchorType;
    }

    /**
     * Convenience accessor that allows checking whether this is the anchor type
     * itself; if not, it must be one of supertypes that is also a {@link ReferenceType}
     */
    public boolean isAnchorType() {
        return (_anchorType == this);
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public String toString()
    {
        return new StringBuilder(40)
            .append("[reference type, class ")
            .append(buildCanonicalName())
            .append('<')
            .append(_referencedType)
            .append('>')
            .append(']')
            .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;

        ReferenceType other = (ReferenceType) o;

        if (other._class != _class) return false;

        // Otherwise actually mostly worry about referenced type
        return _referencedType.equals(other._referencedType);
    }
}
