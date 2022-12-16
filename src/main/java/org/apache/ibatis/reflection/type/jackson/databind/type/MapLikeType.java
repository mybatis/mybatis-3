package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;

/**
 * Type that represents Map-like types; things that consist of key/value pairs
 * but that do not necessarily implement {@link Map}, but that do not
 * have enough introspection functionality to allow for some level of generic
 * handling. This specifically allows framework to check for configuration and
 * annotation settings used for Map types, and pass these to custom handlers
 * that may be more familiar with actual type.
 */
public class MapLikeType extends TypeBase {
    private static final long serialVersionUID = 1L;

    /**
     * Type of keys of Map.
     */
    protected final JavaType _keyType;

    /**
     * Type of values of Map.
     */
    protected final JavaType _valueType;

    /*
    /**********************************************************
    * Life-cycle
    /**********************************************************
     */

    protected MapLikeType(Class<?> mapType, TypeBindings bindings,
            JavaType superClass, JavaType[] superInts, JavaType keyT,
            JavaType valueT, Object valueHandler, Object typeHandler,
            boolean asStatic) {
        super(mapType, bindings, superClass, superInts, keyT.hashCode()
                ^ valueT.hashCode(), valueHandler, typeHandler, asStatic);
        _keyType = keyT;
        _valueType = valueT;
    }

    /**
     * @since 2.7
     */
    protected MapLikeType(TypeBase base, JavaType keyT, JavaType valueT) {
        super(base);
        _keyType = keyT;
        _valueType = valueT;
    }

    /**
     * Factory method that can be used to "upgrade" a basic type into
     * collection-like one; usually done via {@link TypeModifier}
     *
     * @since 2.7
     */
    public static MapLikeType upgradeFrom(JavaType baseType, JavaType keyT,
            JavaType valueT) {
        // 19-Oct-2015, tatu: Not sure if and how other types could be used as
        // base;
        // will cross that bridge if and when need be
        if (baseType instanceof TypeBase) {
            return new MapLikeType((TypeBase) baseType, keyT, valueT);
        }
        throw new IllegalArgumentException(
                "Cannot upgrade from an instance of " + baseType.getClass());
    }

    @Deprecated
    // since 2.7; remove from 2.8
    public static MapLikeType construct(Class<?> rawType, JavaType keyT,
            JavaType valueT) {
        // First: may need to fabricate TypeBindings (needed for refining into
        // concrete collection types, as per [databind#1102])
        TypeVariable<?>[] vars = rawType.getTypeParameters();
        TypeBindings bindings;
        if ((vars == null) || (vars.length != 2)) {
            bindings = TypeBindings.emptyBindings();
        } else {
            bindings = TypeBindings.create(rawType, keyT, valueT);
        }
        return new MapLikeType(rawType, bindings, _bogusSuperClass(rawType),
                null, keyT, valueT, null, null, false);
    }

    @Deprecated
    // since 2.7
    @Override
    protected JavaType _narrow(Class<?> subclass) {
        return new MapLikeType(subclass, _bindings, _superClass,
                _superInterfaces, _keyType, _valueType, _valueHandler,
                _typeHandler, _asStatic);
    }

    /**
     * @since 2.7
     */
    public MapLikeType withKeyType(JavaType keyType) {
        if (keyType == _keyType) {
            return this;
        }
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, keyType, _valueType, _valueHandler,
                _typeHandler, _asStatic);
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        if (_valueType == contentType) {
            return this;
        }
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, _keyType, contentType, _valueHandler,
                _typeHandler, _asStatic);
    }

    @Override
    public MapLikeType withTypeHandler(Object h) {
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, _keyType, _valueType, _valueHandler, h,
                _asStatic);
    }

    @Override
    public MapLikeType withContentTypeHandler(Object h) {
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, _keyType, _valueType.withTypeHandler(h),
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public MapLikeType withValueHandler(Object h) {
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, _keyType, _valueType, h, _typeHandler,
                _asStatic);
    }

    @Override
    public MapLikeType withContentValueHandler(Object h) {
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, _keyType, _valueType.withValueHandler(h),
                _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    public JavaType withHandlersFrom(JavaType src) {
        JavaType type = super.withHandlersFrom(src);
        JavaType srcKeyType = src.getKeyType();
        // "withKeyType()" not part of JavaType, hence must verify:
        if (type instanceof MapLikeType) {
            if (srcKeyType != null) {
                JavaType ct = _keyType.withHandlersFrom(srcKeyType);
                if (ct != _keyType) {
                    type = ((MapLikeType) type).withKeyType(ct);
                }
            }
        }
        JavaType srcCt = src.getContentType();
        if (srcCt != null) {
            JavaType ct = _valueType.withHandlersFrom(srcCt);
            if (ct != _valueType) {
                type = type.withContentType(ct);
            }
        }
        return type;
    }

    @Override
    public MapLikeType withStaticTyping() {
        if (_asStatic) {
            return this;
        }
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, _keyType, _valueType.withStaticTyping(),
                _valueHandler, _typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces) {
        return new MapLikeType(rawType, bindings, superClass, superInterfaces,
                _keyType, _valueType, _valueHandler, _typeHandler, _asStatic);
    }

    @Override
    protected String buildCanonicalName() {
        StringBuilder sb = new StringBuilder();
        sb.append(_class.getName());
        // 10-Apr-2021, tatu: [databind#3108] Ensure we have at least nominally
        //   compatible type declaration (weak guarantee but better than nothing)
        if ((_keyType != null) && _hasNTypeParameters(2)) {
            sb.append('<');
            sb.append(_keyType.toCanonical());
            sb.append(',');
            sb.append(_valueType.toCanonical());
            sb.append('>');
        }
        return sb.toString();
    }

    /*
    /**********************************************************
    /* Public API
    /**********************************************************
     */

    @Override
    public boolean isContainerType() {
        return true;
    }

    @Override
    public boolean isMapLikeType() {
        return true;
    }

    @Override
    public JavaType getKeyType() {
        return _keyType;
    }

    @Override
    public JavaType getContentType() {
        return _valueType;
    }

    @Override
    public Object getContentValueHandler() {
        return _valueType.getValueHandler();
    }

    @Override
    public Object getContentTypeHandler() {
        return _valueType.getTypeHandler();
    }

    @Override
    public boolean hasHandlers() {
        return super.hasHandlers() || _valueType.hasHandlers()
                || _keyType.hasHandlers();
    }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        return _classSignature(_class, sb, true);
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb) {
        _classSignature(_class, sb, false);
        sb.append('<');
        _keyType.getGenericSignature(sb);
        _valueType.getGenericSignature(sb);
        sb.append(">;");
        return sb;
    }

    /*
    /**********************************************************
    /* Extended API
    /**********************************************************
     */

    public MapLikeType withKeyTypeHandler(Object h) {
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, _keyType.withTypeHandler(h), _valueType,
                _valueHandler, _typeHandler, _asStatic);
    }

    public MapLikeType withKeyValueHandler(Object h) {
        return new MapLikeType(_class, _bindings, _superClass,
                _superInterfaces, _keyType.withValueHandler(h), _valueType,
                _valueHandler, _typeHandler, _asStatic);
    }

    /**
     * Method that can be used for checking whether this type is a "real"
     * Collection type; meaning whether it represents a parameterized subtype of
     * {@link Collection} or just something that acts like one.
     *
     * @deprecated Since 2.12 just use instanceof
     */
    @Deprecated // since 2.12 use assignment checks
    public boolean isTrueMapType() {
        return Map.class.isAssignableFrom(_class);
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public String toString() {
        return String.format("[map-like type; class %s, %s -> %s]",
                _class.getName(), _keyType, _valueType);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;

        MapLikeType other = (MapLikeType) o;
        return (_class == other._class) && _keyType.equals(other._keyType)
                && _valueType.equals(other._valueType);
    }
}
