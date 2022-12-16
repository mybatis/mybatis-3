package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class TypeBase
    extends JavaType
//    implements JsonSerializable
{
    private static final long serialVersionUID = 1;

    private final static TypeBindings NO_BINDINGS = TypeBindings.emptyBindings();
    private final static JavaType[] NO_TYPES = new JavaType[0];

    protected final JavaType _superClass;

    protected final JavaType[] _superInterfaces;

    /**
     * Bindings in effect for this type instance; possibly empty.
     * Needed when resolving types declared in members of this type
     * (if any).
     *
     * @since 2.7
     */
    protected final TypeBindings _bindings;

    /**
     * Lazily initialized external representation of the type
     */
    volatile transient String _canonicalName;

    /**
     * Main constructor to use by extending classes.
     */
    protected TypeBase(Class<?> raw, TypeBindings bindings, JavaType superClass, JavaType[] superInts,
            int hash,
            Object valueHandler, Object typeHandler, boolean asStatic)
    {
        super(raw, hash, valueHandler, typeHandler, asStatic);
        _bindings = (bindings == null) ? NO_BINDINGS : bindings;
        _superClass = superClass;
        _superInterfaces = superInts;
    }

    /**
     * Copy-constructor used when refining/upgrading type instances.
     *
     * @since 2.7
     */
    protected TypeBase(TypeBase base) {
        super(base);
        _superClass = base._superClass;
        _superInterfaces = base._superInterfaces;
        _bindings = base._bindings;
    }

    @Override
    public String toCanonical()
    {
        String str = _canonicalName;
        if (str == null) {
            str = buildCanonicalName();
        }
        return str;
    }

    protected String buildCanonicalName() {
        return _class.getName();
    }

    @Override
    public abstract StringBuilder getGenericSignature(StringBuilder sb);

    @Override
    public abstract StringBuilder getErasedSignature(StringBuilder sb);

    @Override
    public TypeBindings getBindings() {
        return _bindings;
    }

    @Override
    public int containedTypeCount() {
        return _bindings.size();
    }

    @Override
    public JavaType containedType(int index) {
        return _bindings.getBoundType(index);
    }

    @Override
    @Deprecated
    public String containedTypeName(int index) {
        return _bindings.getBoundName(index);
    }

    @Override
    public JavaType getSuperClass() {
        return _superClass;
    }

    @Override
    public List<JavaType> getInterfaces() {
        if (_superInterfaces == null) {
            return Collections.emptyList();
        }
        switch (_superInterfaces.length) {
        case 0:
            return Collections.emptyList();
        case 1:
            return Collections.singletonList(_superInterfaces[0]);
        }
        return Arrays.asList(_superInterfaces);
    }

    @Override
    public final JavaType findSuperType(Class<?> rawTarget)
    {
        if (rawTarget == _class) {
            return this;
        }
        // Check super interfaces first:
        if (rawTarget.isInterface() && (_superInterfaces != null)) {
            for (int i = 0, count = _superInterfaces.length; i < count; ++i) {
                JavaType type = _superInterfaces[i].findSuperType(rawTarget);
                if (type != null) {
                    return type;
                }
            }
        }
        // and if not found, super class and its supertypes
        if (_superClass != null) {
            JavaType type = _superClass.findSuperType(rawTarget);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    @Override
    public JavaType[] findTypeParameters(Class<?> expType)
    {
        JavaType match = findSuperType(expType);
        if (match == null) {
            return NO_TYPES;
        }
        return match.getBindings().typeParameterArray();
    }

    /*
    /**********************************************************
    /* JsonSerializable base implementation
    /**********************************************************
     */

    /*@Override
    public void serializeWithType(JsonGenerator g, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException
    {
        WritableTypeId typeIdDef = new WritableTypeId(this, JsonToken.VALUE_STRING);
        typeSer.writeTypePrefix(g, typeIdDef);
        this.serialize(g, provider);
        typeSer.writeTypeSuffix(g, typeIdDef);
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider provider)
        throws IOException
    {
        gen.writeString(toCanonical());
    }*/

    /*
    /**********************************************************
    /* Methods for sub-classes to use
    /**********************************************************
     */

    /**
     * @param trailingSemicolon Whether to add trailing semicolon for non-primitive
     *   (reference) types or not
     */
    protected static StringBuilder _classSignature(Class<?> cls, StringBuilder sb,
           boolean trailingSemicolon)
    {
        if (cls.isPrimitive()) {
            if (cls == Boolean.TYPE) {
                sb.append('Z');
            } else if (cls == Byte.TYPE) {
                sb.append('B');
            }
            else if (cls == Short.TYPE) {
                sb.append('S');
            }
            else if (cls == Character.TYPE) {
                sb.append('C');
            }
            else if (cls == Integer.TYPE) {
                sb.append('I');
            }
            else if (cls == Long.TYPE) {
                sb.append('J');
            }
            else if (cls == Float.TYPE) {
                sb.append('F');
            }
            else if (cls == Double.TYPE) {
                sb.append('D');
            }
            else if (cls == Void.TYPE) {
                sb.append('V');
            } else {
                throw new IllegalStateException("Unrecognized primitive type: "+cls.getName());
            }
        } else {
            sb.append('L');
            String name = cls.getName();
            for (int i = 0, len = name.length(); i < len; ++i) {
                char c = name.charAt(i);
                if (c == '.') c = '/';
                sb.append(c);
            }
            if (trailingSemicolon) {
                sb.append(';');
            }
        }
        return sb;
    }

    /**
     * Internal helper method used to figure out nominal super-class for
     * deprecated factory methods / constructors, where we are not given
     * properly resolved supertype hierarchy.
     * Will basically give `JavaType` for `java.lang.Object` for classes
     * other than `java.lafgn.Object`; null for others.
     *
     * @since 2.7
     */
    protected static JavaType _bogusSuperClass(Class<?> cls) {
        Class<?> parent = cls.getSuperclass();
        if (parent == null) {
            return null;
        }
        return TypeFactory.unknownType();
    }

    protected boolean _hasNTypeParameters(int count) {
        TypeVariable<?>[] params = _class.getTypeParameters();
        return (params.length == count);
    }
}
