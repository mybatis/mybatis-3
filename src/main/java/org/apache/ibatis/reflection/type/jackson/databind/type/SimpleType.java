package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.util.Collection;
import java.util.Map;

/**
 * Simple types are defined as anything other than one of recognized
 * container types (arrays, Collections, Maps). For our needs we
 * need not know anything further, since we have no way of dealing
 * with generic types other than Collections and Maps.
 */
public class SimpleType // note: until 2.6 was final
    extends TypeBase
{
    private static final long serialVersionUID = 1L;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * Constructor only used by core Jackson databind functionality;
     * should never be called by application code.
     *<p>
     * As with other direct construction that by-passes {@link TypeFactory},
     * no introspection occurs with respect to super-types; caller must be
     * aware of consequences if using this method.
     */
    protected SimpleType(Class<?> cls) {
        this(cls, TypeBindings.emptyBindings(), null, null);
    }

    protected SimpleType(Class<?> cls, TypeBindings bindings,
            JavaType superClass, JavaType[] superInts) {
        this(cls, bindings, superClass, superInts, null, null, false);
    }

    /**
     * Simple copy-constructor, usually used when upgrading/refining a simple type
     * into more specialized type.
     *
     * @since 2.7
     */
    protected SimpleType(TypeBase base) {
        super(base);
    }

    protected SimpleType(Class<?> cls, TypeBindings bindings,
            JavaType superClass, JavaType[] superInts,
            Object valueHandler, Object typeHandler, boolean asStatic)
    {
        super(cls, bindings, superClass, superInts,
                0, valueHandler, typeHandler, asStatic);
    }

    /**
     * Pass-through constructor used by {@link ReferenceType}.
     *
     * @since 2.6
     */
    protected SimpleType(Class<?> cls, TypeBindings bindings,
            JavaType superClass, JavaType[] superInts, int extraHash,
            Object valueHandler, Object typeHandler, boolean asStatic)
    {
        super(cls, bindings, superClass, superInts,
                extraHash, valueHandler, typeHandler, asStatic);
    }

    /**
     * Method used by core Jackson classes: NOT to be used by application code:
     * it does NOT properly handle inspection of super-types, so neither parent
     * Classes nor implemented Interfaces are accessible with resulting type
     * instance.
     *<p>
     * NOTE: public only because it is called by <code>ObjectMapper</code> which is
     * not in same package
     */
    public static SimpleType constructUnsafe(Class<?> raw) {
        return new SimpleType(raw, null,
                // 18-Oct-2015, tatu: Should be ok to omit possible super-types, right?
                null, null, null, null, false);
    }

    /**
     * Method that should NOT to be used by application code:
     * it does NOT properly handle inspection of super-types, so neither parent
     * Classes nor implemented Interfaces are accessible with resulting type
     * instance. Instead, please use {@link TypeFactory}'s <code>constructType</code>
     * methods which handle introspection appropriately.
     *<p>
     * Note that prior to 2.7, method usage was not limited and would typically
     * have worked acceptably: the problem comes from inability to resolve super-type
     * information, for which {@link TypeFactory} is needed.
     *
     * @deprecated Since 2.7
     */
    @Deprecated
    public static SimpleType construct(Class<?> cls)
    {
        /* Let's add sanity checks, just to ensure no
         * Map/Collection entries are constructed
         */
        if (Map.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Cannot construct SimpleType for a Map (class: "+cls.getName()+")");
        }
        if (Collection.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Cannot construct SimpleType for a Collection (class: "+cls.getName()+")");
        }
        // ... and while we are at it, not array types either
        if (cls.isArray()) {
            throw new IllegalArgumentException("Cannot construct SimpleType for an array (class: "+cls.getName()+")");
        }
        TypeBindings b = TypeBindings.emptyBindings();
        return new SimpleType(cls, b,
                _buildSuperClass(cls.getSuperclass(), b), null, null, null, false);
    }

    @Override
    @Deprecated
    protected JavaType _narrow(Class<?> subclass)
    {
        if (_class == subclass) {
            return this;
        }
        // Should we check that there is a sub-class relationship?
        // 15-Jan-2016, tatu: Almost yes, but there are some complications with
        //    placeholder values (`Void`, `NoClass`), so cannot quite do yet.
        // TODO: fix in 2.9
        if (!_class.isAssignableFrom(subclass)) {
            /*
            throw new IllegalArgumentException("Class "+subclass.getName()+" not sub-type of "
                    +_class.getName());
                    */
            return new SimpleType(subclass, _bindings, this, _superInterfaces,
                    _valueHandler, _typeHandler, _asStatic);
        }
        // Otherwise, stitch together the hierarchy. First, super-class
        Class<?> next = subclass.getSuperclass();
        if (next == _class) { // straight up parent class? Great.
            return new SimpleType(subclass, _bindings, this,
                    _superInterfaces, _valueHandler, _typeHandler, _asStatic);
        }
        if ((next != null) && _class.isAssignableFrom(next)) {
            JavaType superb = _narrow(next);
            return new SimpleType(subclass, _bindings, superb,
                    null, _valueHandler, _typeHandler, _asStatic);
        }
        // if not found, try a super-interface
        Class<?>[] nextI = subclass.getInterfaces();
        for (Class<?> iface : nextI) {
            if (iface == _class) { // directly implemented
                return new SimpleType(subclass, _bindings, null,
                        new JavaType[] { this }, _valueHandler, _typeHandler, _asStatic);
            }
            if (_class.isAssignableFrom(iface)) { // indirect, so recurse
                JavaType superb = _narrow(iface);
                return new SimpleType(subclass, _bindings, null,
                        new JavaType[] { superb }, _valueHandler, _typeHandler, _asStatic);
            }
        }
        // should not get here but...
        throw new IllegalArgumentException("Internal error: Cannot resolve sub-type for Class "+subclass.getName()+" to "
                +_class.getName());
    }

    @Override
    public JavaType withContentType(JavaType contentType) {
        throw new IllegalArgumentException("Simple types have no content types; cannot call withContentType()");
    }

    @Override
    public SimpleType withTypeHandler(Object h) {
        if (_typeHandler == h) {
            return this;
        }
        return new SimpleType(_class, _bindings, _superClass, _superInterfaces, _valueHandler, h, _asStatic);
    }

    @Override
    public JavaType withContentTypeHandler(Object h) {
        // no content type, so:
        throw new IllegalArgumentException("Simple types have no content types; cannot call withContenTypeHandler()");
    }

    @Override
    public SimpleType withValueHandler(Object h) {
        if (h == _valueHandler) {
            return this;
        }
        return new SimpleType(_class, _bindings, _superClass, _superInterfaces, h, _typeHandler, _asStatic);
    }

    @Override
    public  SimpleType withContentValueHandler(Object h) {
        // no content type, so:
        throw new IllegalArgumentException("Simple types have no content types; cannot call withContenValueHandler()");
    }

    @Override
    public SimpleType withStaticTyping() {
        return _asStatic ? this : new SimpleType(_class, _bindings,
                _superClass, _superInterfaces, _valueHandler, _typeHandler, true);
    }

    @Override
    public JavaType refine(Class<?> rawType, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces) {
        // SimpleType means something not-specialized, so:
        return null;
    }

    @Override
    protected String buildCanonicalName()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(_class.getName());

        final int count = _bindings.size();

        // 10-Apr-2021, tatu: [databind#3108] Ensure we have at least nominally
        //   compatible type declaration (weak guarantee but better than nothing)
        if ((count > 0) && _hasNTypeParameters(count)) {
            sb.append('<');
            for (int i = 0; i < count; ++i) {
                JavaType t = containedType(i);
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(t.toCanonical());
            }
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
    public boolean isContainerType() { return false; }

    @Override
    public boolean hasContentType() { return false; }

    @Override
    public StringBuilder getErasedSignature(StringBuilder sb) {
        return _classSignature(_class, sb, true);
    }

    @Override
    public StringBuilder getGenericSignature(StringBuilder sb)
    {
        _classSignature(_class, sb, false);

        final int count = _bindings.size();
        if (count > 0) {
            sb.append('<');
            for (int i = 0; i < count; ++i) {
                sb = containedType(i).getGenericSignature(sb);
            }
            sb.append('>');
        }
        sb.append(';');
        return sb;
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    /**
     * Helper method we need to recursively build skeletal representations
     * of superclasses.
     *
     * @since 2.7 -- remove when not needed (2.8?)
     */
    private static JavaType _buildSuperClass(Class<?> superClass, TypeBindings b)
    {
        if (superClass == null) {
            return null;
        }
        if (superClass == Object.class) {
            return TypeFactory.unknownType();
        }
        JavaType superSuper = _buildSuperClass(superClass.getSuperclass(), b);
        return new SimpleType(superClass, b,
                superSuper, null, null, null, false);
    }

    /*
    /**********************************************************
    /* Standard methods
    /**********************************************************
     */

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(40);
        sb.append("[simple type, class ").append(buildCanonicalName()).append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this) return true;
        if (o == null) return false;
        if (o.getClass() != getClass()) return false;

        SimpleType other = (SimpleType) o;

        // Classes must be identical...
        if (other._class != this._class) return false;

        // And finally, generic bindings, if any
        TypeBindings b1 = _bindings;
        TypeBindings b2 = other._bindings;
        return b1.equals(b2);
    }
}
