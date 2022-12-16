package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.lang.reflect.TypeVariable;
import java.util.Collection;

/**
 * Type that represents things that act similar to {@link Collection};
 * but may or may not be instances of that interface.
 * This specifically allows framework to check for configuration and annotation
 * settings used for Map types, and pass these to custom handlers that may be more
 * familiar with actual type.
 */
public class CollectionLikeType extends TypeBase
{
    private static final long serialVersionUID = 1L;

    /**
     * Type of elements in collection
     */
    protected final JavaType _elementType;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected CollectionLikeType(Class<?> collT, TypeBindings bindings,
            JavaType superClass, JavaType[] superInts, JavaType elemT,
            Object valueHandler, Object typeHandler, boolean asStatic)
    {
        super(collT, bindings, superClass, superInts,
                elemT.hashCode(), valueHandler, typeHandler, asStatic);
        _elementType = elemT;
    }

    /**
     * @since 2.7
     */
    protected CollectionLikeType(TypeBase base, JavaType elemT)
    {
        super(base);
        _elementType = elemT;
    }

    /**
     * @since 2.7
     */
    public static CollectionLikeType construct(Class<?> rawType, TypeBindings bindings,
            JavaType superClass, JavaType[] superInts, JavaType elemT) {
        return new CollectionLikeType(rawType, bindings, superClass, superInts, elemT,
                null, null, false);
    }

    /**
     * @deprecated Since 2.7, use {@link #upgradeFrom} for constructing instances, given
     *    pre-resolved {@link SimpleType}.
     */
    @Deprecated // since 2.7
    public static CollectionLikeType construct(Class<?> rawType, JavaType elemT) {
        // First: may need to fabricate TypeBindings (needed for refining into
        // concrete collection types, as per [databind#1102])
        TypeVariable<?>[] vars = rawType.getTypeParameters();
        TypeBindings bindings;
        if ((vars == null) || (vars.length != 1)) {
            bindings = TypeBindings.emptyBindings();
        } else {
            bindings = TypeBindings.create(rawType, elemT);
        }
        return new CollectionLikeType(rawType, bindings,
                _bogusSuperClass(rawType), null,
                elemT, null, null, false);
    }

    /**
     * Factory method that can be used to "upgrade" a basic type into collection-like
     * one; usually done via {@link TypeModifier}
     *
     * @since 2.7
     */
    public static CollectionLikeType upgradeFrom(JavaType baseType, JavaType elementType) {
        // 19-Oct-2015, tatu: Not sure if and how other types could be used as base;
        //    will cross that bridge if and when need be
        if (baseType instanceof TypeBase) {
            return new CollectionLikeType((TypeBase) baseType, elementType);
        }
        throw new IllegalArgumentException("Cannot upgrade from an instance of "+baseType.getClass());
    }

    @Override
    @Deprecated // since 2.7
    protected JavaType _narrow(Class<?> subclass) {
        return new CollectionLikeType(subclass, _bindings,
                _superClass, _superInterfaces, _elementType,
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        if (_elementType == contentType) {
            return this;
        }
        return new CollectionLikeType(_class, _bindings, _superClass, _superInterfaces,
                contentType, _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public CollectionLikeType withTypeHandler(Object h) {
        return new CollectionLikeType(_class, _bindings,
                _superClass, _superInterfaces, _elementType, _valueHandler, h, _asStatic);
    }

    @Override
    public CollectionLikeType withContentTypeHandler(Object h)
    {
        return new CollectionLikeType(_class, _bindings,
                _superClass, _superInterfaces, _elementType.withTypeHandler(h),
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public CollectionLikeType withValueHandler(Object h) {
        return new CollectionLikeType(_class, _bindings,
                _superClass, _superInterfaces, _elementType, h, _typeHandler, _asStatic);
    }

    @Override
    public CollectionLikeType withContentValueHandler(Object h) {
        return new CollectionLikeType(_class, _bindings,
                _superClass, _superInterfaces, _elementType.withValueHandler(h),
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public JavaType withHandlersFrom(JavaType src) {
        JavaType type = super.withHandlersFrom(src);
        JavaType srcCt = src.getContentType();
        if (srcCt != null) {
            JavaType ct = _elementType.withHandlersFrom(srcCt);
            if (ct != _elementType) {
                type = type.withContentType(ct);
            }
        }
        return type;
    }

    @Override
    public CollectionLikeType withStaticTyping() {
        if (_asStatic) {
            return this;
        }
        return new CollectionLikeType(_class, _bindings,
                _superClass, _superInterfaces, _elementType.withStaticTyping(),
                _valueHandler, _typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces) {
        return new CollectionLikeType(rawType, bindings,
                superClass, superInterfaces, _elementType,
                _valueHandler, _typeHandler, _asStatic);
    }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    @Override
    public boolean isContainerType() { return true; }

    @Override
    public boolean isCollectionLikeType() { return true; }

    @Override
    public JavaType getContentType() { return _elementType; }

    @Override
    public Object getContentValueHandler() {
        return _elementType.getValueHandler();
    }

    @Override
    public Object getContentTypeHandler() {
        return _elementType.getTypeHandler();
    }

    @Override
    public boolean hasHandlers() {
        return super.hasHandlers() || _elementType.hasHandlers();
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        return _classSignature(_class, sb, true);
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        _classSignature(_class, sb, false);
        sb.append('<');
        _elementType.getGenericSignature(sb);
        sb.append(">;");
        return sb;
    }

    @Override
    protected String buildCanonicalName() {
        StringBuilder sb = new StringBuilder();
        sb.append(_class.getName());
        // 10-Apr-2021, tatu: [databind#3108] Ensure we have at least nominally
        //   compatible type declaration (weak guarantee but better than nothing)
        if ((_elementType != null) && _hasNTypeParameters(1)) {
            sb.append('<');
            sb.append(_elementType.toCanonical());
            sb.append('>');
        }
        return sb.toString();
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    /**
     * Method that can be used for checking whether this type is a
     * "real" Collection type; meaning whether it represents a parameterized
     * subtype of {@link Collection} or just something that acts
     * like one.
     *
     * @deprecated Since 2.12 just use instanceof
     */
    @Deprecated // since 2.12 use assignment checks
    public boolean isTrueCollectionType() {
        return Collection.class.isAssignableFrom(_class);
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;

        CollectionLikeType other = (CollectionLikeType) o;
        return  (_class == other._class) && _elementType.equals(other._elementType);
    }

    @Override
    public String toString()
    {
        return "[collection-like type; class "+_class.getName()+", contains "+_elementType+"]";
    }

}
