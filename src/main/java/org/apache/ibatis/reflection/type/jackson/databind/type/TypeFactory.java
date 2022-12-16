package org.apache.ibatis.reflection.type.jackson.databind.type;

import org.apache.ibatis.reflection.type.jackson.databind.util.ArrayBuilders;
import org.apache.ibatis.reflection.type.jackson.databind.util.ClassUtil;
import org.apache.ibatis.reflection.type.jackson.databind.util.LRUMap;
import org.apache.ibatis.reflection.type.jackson.databind.util.LookupCache;
import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class used for creating concrete {@link JavaType} instances,
 * given various inputs.
 *<p>
 * Instances of this class are accessible using {@link org.apache.ibatis.type.resolved.jackson.databind.ObjectMapper}
 * as well as many objects it constructs (like
* {@link org.apache.ibatis.type.resolved.jackson.databind.DeserializationConfig} and
 * {@link org.apache.ibatis.type.resolved.jackson.databind.SerializationConfig})),
 * but usually those objects also
 * expose convenience methods (<code>constructType</code>).
 * So, you can do for example:
 *<pre>
 *   JavaType stringType = mapper.constructType(String.class);
 *</pre>
 * However, more advanced methods are only exposed by factory so that you
 * may need to use:
 *<pre>
 *   JavaType stringCollection = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
 *</pre>
 *<p>
 * Note on optimizations: generic type parameters are resolved for all types, with following
 * exceptions:
 *<ul>
 *  <li>For optimization purposes, type resolution is skipped for following commonly seen
 * types that do have type parameters, but ones that are rarely needed:
 *     <ul>
 *       <li>{@link Enum}: Self-referential type reference is simply dropped and
 *       Class is exposed as a simple, non-parameterized {@link SimpleType}
 *        </li>
 *       <li>{@link Comparable}: Type parameter is simply dropped and and
 *       interface is exposed as a simple, non-parameterized {@link SimpleType}
 *        </li>
 *       <li>Up until Jackson 2.13, {@link Class} type parameter was dropped; resolution
 *         was added back in Jackson 2.14.
 *        </li>
 *     </ul>
 *   </li>
 *  <li>For {@link Collection} subtypes, resolved type is ALWAYS the parameter for
 *     {link java.util.Collection} and not that of actually resolved subtype.
 *     This is usually (but not always) same parameter.
 *   </li>
 *  <li>For {@link Map} subtypes, resolved type is ALWAYS the parameter for
 *     {link java.util.Map} and not that of actually resolved subtype.
 *     These are usually (but not always) same parameters.
 *   </li>
 *</ul>
 */
@SuppressWarnings({"rawtypes" })
public class TypeFactory // note: was final in 2.9, removed from 2.10
    implements java.io.Serializable
{
    private static final long serialVersionUID = 1L;

    private final static JavaType[] NO_TYPES = new JavaType[0];

    /**
     * Globally shared singleton. Not accessed directly; non-core
     * code should use per-ObjectMapper instance (via configuration objects).
     * Core Jackson code uses {@link #defaultInstance} for accessing it.
     */
    protected final static TypeFactory instance = new TypeFactory();

    protected final static TypeBindings EMPTY_BINDINGS = TypeBindings.emptyBindings();

    /*
    /**********************************************************
    /* Constants for "well-known" classes
    /**********************************************************
     */

    // // // Let's assume that a small set of core primitive/basic types
    // // // will not be modified, and can be freely shared to streamline
    // // // parts of processing

    private final static Class<?> CLS_STRING = String.class;
    private final static Class<?> CLS_OBJECT = Object.class;

    private final static Class<?> CLS_COMPARABLE = Comparable.class;
    private final static Class<?> CLS_ENUM = Enum.class;
//    private final static Class<?> CLS_JSON_NODE = JsonNode.class; // since 2.10

    private final static Class<?> CLS_BOOL = Boolean.TYPE;
    private final static Class<?> CLS_INT = Integer.TYPE;
    private final static Class<?> CLS_LONG = Long.TYPE;

    /*
    /**********************************************************
    /* Cached pre-constructed JavaType instances
    /**********************************************************
     */

    // note: these are primitive, hence no super types
    protected final static SimpleType CORE_TYPE_BOOL = new SimpleType(CLS_BOOL);
    protected final static SimpleType CORE_TYPE_INT = new SimpleType(CLS_INT);
    protected final static SimpleType CORE_TYPE_LONG = new SimpleType(CLS_LONG);

    // and as to String... well, for now, ignore its super types
    protected final static SimpleType CORE_TYPE_STRING = new SimpleType(CLS_STRING);

    // @since 2.7
    protected final static SimpleType CORE_TYPE_OBJECT = new SimpleType(CLS_OBJECT);

    /**
     * Cache {@link Comparable} because it is both parameteric (relatively costly to
     * resolve) and mostly useless (no special handling), better handle directly
     *
     * @since 2.7
     */
    protected final static SimpleType CORE_TYPE_COMPARABLE = new SimpleType(CLS_COMPARABLE);

    /**
     * Cache {@link Enum} because it is parametric AND self-referential (costly to
     * resolve) and useless in itself (no special handling).
     *
     * @since 2.7
     */
    protected final static SimpleType CORE_TYPE_ENUM = new SimpleType(CLS_ENUM);

    /**
     * Cache {@link JsonNode} because it is no critical path of simple tree model
     * reading and does not have things to override
     *
     * @since 2.10
     */
//    protected final static SimpleType CORE_TYPE_JSON_NODE = new SimpleType(CLS_JSON_NODE);

    /**
     * Since type resolution can be expensive (specifically when resolving
     * actual generic types), we will use small cache to avoid repetitive
     * resolution of core types
     */
    protected final LookupCache<Object,JavaType> _typeCache;

    /*
    /**********************************************************
    /* Configuration
    /**********************************************************
     */

    /**
     * Registered {@link TypeModifier}s: objects that can change details
     * of {@link JavaType} instances factory constructs.
     */
    protected final TypeModifier[] _modifiers;

    protected final TypeParser _parser;

    /**
     * ClassLoader used by this factory [databind#624].
     */
    protected final ClassLoader _classLoader;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    private TypeFactory() {
        this((LookupCache<Object,JavaType>) null);
    }

    /**
     * @since 2.8
     * @deprecated Since 2.12
     */
    @Deprecated // since 2.12
    protected TypeFactory(LRUMap<Object,JavaType> typeCache) {
        this((LookupCache<Object,JavaType>) typeCache);
    }

    /**
     * @since 2.12
     */
    protected TypeFactory(LookupCache<Object,JavaType> typeCache) {
        if (typeCache == null) {
            typeCache = new LRUMap<>(16, 200);
        }
        _typeCache = typeCache;
        _parser = new TypeParser(this);
        _modifiers = null;
        _classLoader = null;
    }

    /**
     * @deprecated Since 2.12
     */
    @Deprecated // since 2.12
    protected TypeFactory(LRUMap<Object,JavaType> typeCache, TypeParser p,
            TypeModifier[] mods, ClassLoader classLoader)
    {
        this((LookupCache<Object,JavaType>) typeCache, p, mods, classLoader);
    }

    /**
     * @since 2.12
     */
    protected TypeFactory(LookupCache<Object,JavaType> typeCache, TypeParser p,
                          TypeModifier[] mods, ClassLoader classLoader)
    {
        if (typeCache == null) {
            typeCache = new LRUMap<>(16, 200);
        }
        _typeCache = typeCache;
        // As per [databind#894] must ensure we have back-linkage from TypeFactory:
        _parser = p.withFactory(this);
        _modifiers = mods;
        _classLoader = classLoader;
    }

    /**
     * "Mutant factory" method which will construct a new instance with specified
     * {@link TypeModifier} added as the first modifier to call (in case there
     * are multiple registered).
     */
    public TypeFactory withModifier(TypeModifier mod)
    {
        LookupCache<Object,JavaType> typeCache = _typeCache;
        TypeModifier[] mods;
        if (mod == null) { // mostly for unit tests
            mods = null;
            // 30-Jun-2016, tatu: for some reason expected semantics are to clear cache
            //    in this case; can't recall why, but keeping the same
            typeCache = null;
        } else if (_modifiers == null) {
            mods = new TypeModifier[] { mod };
            // 29-Jul-2019, tatu: Actually I think we better clear cache in this case
            //    as well to ensure no leakage occurs (see [databind#2395])
            typeCache = null;
        } else {
            // but may keep existing cache otherwise
            mods = ArrayBuilders.insertInListNoDup(_modifiers, mod);
        }
        return new TypeFactory(typeCache, _parser, mods, _classLoader);
    }

    /**
     * "Mutant factory" method which will construct a new instance with specified
     * {@link ClassLoader} to use by {@link #findClass}.
     */
    public TypeFactory withClassLoader(ClassLoader classLoader) {
        return new TypeFactory(_typeCache, _parser, _modifiers, classLoader);
    }

    /**
     * Mutant factory method that will construct new {@link TypeFactory} with
     * identical settings except for different cache; most likely one with
     * bigger maximum size.
     *
     * @since 2.8
     * @deprecated Since 2.12
     */
    @Deprecated // since 2.12
    public TypeFactory withCache(LRUMap<Object,JavaType> cache)  {
        return new TypeFactory(cache, _parser, _modifiers, _classLoader);
    }

    /**
     * Mutant factory method that will construct new {@link TypeFactory} with
     * identical settings except for different cache; most likely one with
     * bigger maximum size.
     *
     * @since 2.12
     */
    public TypeFactory withCache(LookupCache<Object,JavaType> cache)  {
        return new TypeFactory(cache, _parser, _modifiers, _classLoader);
    }

    /**
     * Method used to access the globally shared instance, which has
     * no custom configuration. Used by <code>ObjectMapper</code> to
     * get the default factory when constructed.
     */
    public static TypeFactory defaultInstance() { return instance; }

    /**
     * Method that will clear up any cached type definitions that may
     * be cached by this {@link TypeFactory} instance.
     * This method should not be commonly used, that is, only use it
     * if you know there is a problem with retention of type definitions;
     * the most likely (and currently only known) problem is retention
     * of {@link Class} instances via {@link JavaType} reference.
     *
     * @since 2.4.1
     */
    public void clearCache() {
        _typeCache.clear();
    }

    public ClassLoader getClassLoader() {
        return _classLoader;
    }

    /*
    /**********************************************************
    /* Static methods for non-instance-specific functionality
    /**********************************************************
     */

    /**
     * Method for constructing a marker type that indicates missing generic
     * type information, which is handled same as simple type for
     * <code>java.lang.Object</code>.
     */
    public static JavaType unknownType() {
        return defaultInstance()._unknownType();
    }

    /**
     * Static helper method that can be called to figure out type-erased
     * call for given JDK type. It can be called statically since type resolution
     * process can never change actual type-erased class; thereby static
     * default instance is used for determination.
     */
    public static Class<?> rawClass(Type t) {
        if (t instanceof Class<?>) {
            return (Class<?>) t;
        }
        // Should be able to optimize bit more in future...
        return defaultInstance().constructType(t).getRawClass();
    }

    /*
    /**********************************************************
    /* Low-level helper methods
    /**********************************************************
     */

    /**
     * Low-level lookup method moved from {@link ClassUtil},
     * to allow for overriding of lookup functionality in environments like OSGi.
     *
     * @since 2.6
     */
    public Class<?> findClass(String className) throws ClassNotFoundException
    {
        if (className.indexOf('.') < 0) {
            Class<?> prim = _findPrimitive(className);
            if (prim != null) {
                return prim;
            }
        }
        // Two-phase lookup: first using context ClassLoader; then default
        Throwable prob = null;
        ClassLoader loader = this.getClassLoader();
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        if (loader != null) {
            try {
                return classForName(className, true, loader);
            } catch (Exception e) {
                prob = ClassUtil.getRootCause(e);
            }
        }
        try {
            return classForName(className);
        } catch (Exception e) {
            if (prob == null) {
                prob = ClassUtil.getRootCause(e);
            }
        }
        ClassUtil.throwIfRTE(prob);
        throw new ClassNotFoundException(prob.getMessage(), prob);
    }

    protected Class<?> classForName(String name, boolean initialize,
            ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(name, true, loader);
    }

    protected Class<?> classForName(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    protected Class<?> _findPrimitive(String className)
    {
        if ("int".equals(className)) return Integer.TYPE;
        if ("long".equals(className)) return Long.TYPE;
        if ("float".equals(className)) return Float.TYPE;
        if ("double".equals(className)) return Double.TYPE;
        if ("boolean".equals(className)) return Boolean.TYPE;
        if ("byte".equals(className)) return Byte.TYPE;
        if ("char".equals(className)) return Character.TYPE;
        if ("short".equals(className)) return Short.TYPE;
        if ("void".equals(className)) return Void.TYPE;
        return null;
    }

    /*
    /**********************************************************
    /* Type conversion, parameterization resolution methods
    /**********************************************************
     */

    /**
     * Factory method for creating a subtype of given base type, as defined
     * by specified subclass; but retaining generic type information if any.
     * Can be used, for example, to get equivalent of "HashMap&lt;String,Integer&gt;"
     * from "Map&lt;String,Integer&gt;" by giving <code>HashMap.class</code>
     * as subclass.
     * Short-cut for:
     *<pre>
     * constructSpecializedType(baseType, subclass, class);
     *</pre>
     * that is, will use "strict" compatibility checking, usually used for
     * deserialization purposes (but often not for serialization).
     */
    public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass)
        throws IllegalArgumentException
    {
        return constructSpecializedType(baseType, subclass, false);
    }

    /**
     * Factory method for creating a subtype of given base type, as defined
     * by specified subclass; but retaining generic type information if any.
     * Can be used, for example, to get equivalent of "HashMap&lt;String,Integer&gt;"
     * from "Map&lt;String,Integer&gt;" by giving <code>HashMap.class</code>
     * as subclass.
     *
     * @param baseType Declared base type with resolved type parameters
     * @param subclass Runtime subtype to use for resolving
     * @param relaxedCompatibilityCheck Whether checking for type-assignment compatibility
     *    should be "relaxed" ({@code true}) or "strict" ({@code false}): typically
     *    serialization uses relaxed, deserialization strict checking.
     *
     * @return Resolved sub-type
     *
     * @since 2.11
     */
    public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass,
            boolean relaxedCompatibilityCheck)
        throws IllegalArgumentException
    {
        // simple optimization to avoid costly introspection if type-erased type does NOT differ
        final Class<?> rawBase = baseType.getRawClass();
        if (rawBase == subclass) {
            return baseType;
        }
        JavaType newType;

        // also: if we start from untyped, not much to save
        do { // bogus loop to be able to break
            if (rawBase == Object.class) {
                newType = _fromClass(null, subclass, EMPTY_BINDINGS);
                break;
            }
            if (!rawBase.isAssignableFrom(subclass)) {
                throw new IllegalArgumentException(String.format("Class %s not subtype of %s",
                        ClassUtil.nameOf(subclass), ClassUtil.getTypeDescription(baseType)
                ));
            }
            // A few special cases where we can simplify handling:

            // (1) A small set of "well-known" List/Map subtypes where can take a short-cut
            if (baseType.isContainerType()) {
                if (baseType.isMapLikeType()) {
                    if ((subclass == HashMap.class)
                            || (subclass == LinkedHashMap.class)
                            || (subclass == EnumMap.class)
                            || (subclass == TreeMap.class)) {
                        newType = _fromClass(null, subclass,
                                TypeBindings.create(subclass, baseType.getKeyType(), baseType.getContentType()));
                        break;
                    }
                } else if (baseType.isCollectionLikeType()) {
                    if ((subclass == ArrayList.class)
                            || (subclass == LinkedList.class)
                            || (subclass == HashSet.class)
                            || (subclass == TreeSet.class)) {
                        newType = _fromClass(null, subclass,
                                TypeBindings.create(subclass, baseType.getContentType()));
                        break;
                    }
                    // 29-Oct-2015, tatu: One further shortcut: there are variants of `EnumSet`,
                    //    but they are impl details and we basically do not care...
                    if (rawBase == EnumSet.class) {
                        return baseType;
                    }
                }
            }
            // (2) Original target type has no generics -- just resolve subtype
            if (baseType.getBindings().isEmpty()) {
                newType = _fromClass(null, subclass, EMPTY_BINDINGS);
                break;
            }

            // (3) Sub-class does not take type parameters -- just resolve subtype
            int typeParamCount = subclass.getTypeParameters().length;
            if (typeParamCount == 0) {
                newType = _fromClass(null, subclass, EMPTY_BINDINGS);
                break;
            }
            // (4) If all else fails, do the full traversal using placeholders
            TypeBindings tb = _bindingsForSubtype(baseType, typeParamCount,
                    subclass, relaxedCompatibilityCheck);
            newType = _fromClass(null, subclass, tb);

        } while (false);

        // 25-Sep-2016, tatu: As per [databind#1384] also need to ensure handlers get
        //   copied as well
        newType = newType.withHandlersFrom(baseType);
        return newType;
    }

    private TypeBindings _bindingsForSubtype(JavaType baseType, int typeParamCount,
            Class<?> subclass, boolean relaxedCompatibilityCheck)
    {
        PlaceholderForType[] placeholders = new PlaceholderForType[typeParamCount];
        for (int i = 0; i < typeParamCount; ++i) {
            placeholders[i] = new PlaceholderForType(i);
        }
        TypeBindings b = TypeBindings.create(subclass, placeholders);
        // First: pseudo-resolve to get placeholders in place:
        JavaType tmpSub = _fromClass(null, subclass, b);
        // Then find super-type
        JavaType baseWithPlaceholders = tmpSub.findSuperType(baseType.getRawClass());
        if (baseWithPlaceholders == null) { // should be found but...
            throw new IllegalArgumentException(String.format(
                    "Internal error: unable to locate supertype (%s) from resolved subtype %s", baseType.getRawClass().getName(),
                    subclass.getName()));
        }
        // and traverse type hierarchies to both verify and to resolve placeholders
        String error = _resolveTypePlaceholders(baseType, baseWithPlaceholders);
        if (error != null) {
            // 28-Mar-2020, tatu: As per [databind#2632], need to ignore the issue in
            //   some cases. For now, just fully ignore; may need to refine in future
            if (!relaxedCompatibilityCheck) {
                throw new IllegalArgumentException("Failed to specialize base type "+baseType.toCanonical()+" as "
                        +subclass.getName()+", problem: "+error);
            }
        }

        final JavaType[] typeParams = new JavaType[typeParamCount];
        for (int i = 0; i < typeParamCount; ++i) {
            JavaType t = placeholders[i].actualType();
            // 18-Oct-2017, tatu: Looks like sometimes we have incomplete bindings (even if not
            //     common, it is possible if subtype is type-erased class with added type
            //     variable -- see test(s) with "bogus" type(s)).
            if (t == null) {
                t = unknownType();
            }
            typeParams[i] = t;
        }
        return TypeBindings.create(subclass, typeParams);
    }

    private String _resolveTypePlaceholders(JavaType sourceType, JavaType actualType)
        throws IllegalArgumentException
    {
        List<JavaType> expectedTypes = sourceType.getBindings().getTypeParameters();
        List<JavaType> actualTypes = actualType.getBindings().getTypeParameters();

        final int actCount = actualTypes.size();

        for (int i = 0, expCount = expectedTypes.size(); i < expCount; ++i) {
            JavaType exp = expectedTypes.get(i);
            JavaType act = (i < actCount) ? actualTypes.get(i) : unknownType();

            if (!_verifyAndResolvePlaceholders(exp, act)) {
                // 14-May-2018, tatu: As per [databind#2034] it seems we better relax assignment
                //   rules further -- at least likely "raw" (untyped, non-generic) base should probably
                //   allow specialization.
                if (exp.hasRawClass(Object.class)) {
                    continue;
                }
                // 19-Apr-2018, tatu: Hack for [databind#1964] -- allow type demotion
                //    for `java.util.Map` key type if (and only if) target type is
                //    `java.lang.Object`
                // 19-Aug-2019, tatu: Further, allow for all Map-like types, with assumption
                //    first argument would be key; initially just because Scala Maps have
                //    some issues (see [databind#2422])
                if (i == 0) {
                    if (sourceType.isMapLikeType()
                            && act.hasRawClass(Object.class)) {
                        continue;
                    }
                }
                // 19-Nov-2018, tatu: To solve [databind#2155], let's allow type-compatible
                //   assignment for interfaces at least...
                if (exp.isInterface()) {
                    if (exp.isTypeOrSuperTypeOf(act.getRawClass())) {
                        continue;
                    }
                }
                return String.format("Type parameter #%d/%d differs; can not specialize %s with %s",
                        (i+1), expCount, exp.toCanonical(), act.toCanonical());
            }
        }
        return null;
    }

    private boolean _verifyAndResolvePlaceholders(JavaType exp, JavaType act)
    {
        // See if we have an actual type placeholder to resolve; if yes, replace
        if (act instanceof PlaceholderForType) {
            ((PlaceholderForType) act).actualType(exp);
            return true;
        }
        // if not, try to verify compatibility. But note that we can not
        // use simple equality as we need to resolve recursively
        if (exp.getRawClass() != act.getRawClass()) {
            return false;
        }
        // But we can check type parameters "blindly"
        List<JavaType> expectedTypes = exp.getBindings().getTypeParameters();
        List<JavaType> actualTypes = act.getBindings().getTypeParameters();
        for (int i = 0, len = expectedTypes.size(); i < len; ++i) {
            JavaType exp2 = expectedTypes.get(i);
            JavaType act2 = actualTypes.get(i);
            if (!_verifyAndResolvePlaceholders(exp2, act2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method similar to {@link #constructSpecializedType}, but that creates a
     * less-specific type of given type. Usually this is as simple as simply
     * finding super-type with type erasure of <code>superClass</code>, but
     * there may be need for some additional work-arounds.
     *
     * @param superClass
     *
     * @since 2.7
     */
    public JavaType constructGeneralizedType(JavaType baseType, Class<?> superClass)
    {
        // simple optimization to avoid costly introspection if type-erased type does NOT differ
        final Class<?> rawBase = baseType.getRawClass();
        if (rawBase == superClass) {
            return baseType;
        }
        JavaType superType = baseType.findSuperType(superClass);
        if (superType == null) {
            // Most likely, caller did not verify sub/super-type relationship
            if (!superClass.isAssignableFrom(rawBase)) {
                throw new IllegalArgumentException(String.format(
                        "Class %s not a super-type of %s", superClass.getName(), baseType));
            }
            // 01-Nov-2015, tatu: Should never happen, but ch
            throw new IllegalArgumentException(String.format(
                    "Internal error: class %s not included as super-type for %s",
                    superClass.getName(), baseType));
        }
        return superType;
    }

    /**
     * Factory method for constructing a {@link JavaType} out of its canonical
     * representation (see {@link JavaType#toCanonical()}).
     *
     * @param canonical Canonical string representation of a type
     *
     * @throws IllegalArgumentException If canonical representation is malformed,
     *   or class that type represents (including its generic parameters) is
     *   not found
     */
    public JavaType constructFromCanonical(String canonical) throws IllegalArgumentException
    {
        return _parser.parse(canonical);
    }

    /**
     * Method that is to figure out actual type parameters that given
     * class binds to generic types defined by given (generic)
     * interface or class.
     * This could mean, for example, trying to figure out
     * key and value types for Map implementations.
     *
     * @param type Sub-type (leaf type) that implements <code>expType</code>
     */
    public JavaType[] findTypeParameters(JavaType type, Class<?> expType)
    {
        JavaType match = type.findSuperType(expType);
        if (match == null) {
            return NO_TYPES;
        }
        return match.getBindings().typeParameterArray();
    }

    /**
     * @deprecated Since 2.7 resolve raw type first, then find type parameters
     */
    @Deprecated // since 2.7
    public JavaType[] findTypeParameters(Class<?> clz, Class<?> expType, TypeBindings bindings) {
        return findTypeParameters(constructType(clz, bindings), expType);
    }

    /**
     * @deprecated Since 2.7 resolve raw type first, then find type parameters
     */
    @Deprecated // since 2.7
    public JavaType[] findTypeParameters(Class<?> clz, Class<?> expType) {
        return findTypeParameters(constructType(clz), expType);
    }

    /**
     * Method that can be called to figure out more specific of two
     * types (if they are related; that is, one implements or extends the
     * other); or if not related, return the primary type.
     *
     * @param type1 Primary type to consider
     * @param type2 Secondary type to consider
     *
     * @since 2.2
     */
    public JavaType moreSpecificType(JavaType type1, JavaType type2)
    {
        if (type1 == null) {
            return type2;
        }
        if (type2 == null) {
            return type1;
        }
        Class<?> raw1 = type1.getRawClass();
        Class<?> raw2 = type2.getRawClass();
        if (raw1 == raw2) {
            return type1;
        }
        // TODO: maybe try sub-classing, to retain generic types?
        if (raw1.isAssignableFrom(raw2)) {
            return type2;
        }
        return type1;
    }

    /*
    /**********************************************************
    /* Public general-purpose factory methods
    /**********************************************************
     */

    public JavaType constructType(Type type) {
        return _fromAny(null, type, EMPTY_BINDINGS);
    }

    /*public JavaType constructType(TypeReference<?> typeRef)
    {
        // 19-Oct-2015, tatu: Simpler variant like so should work
        return _fromAny(null, typeRef.getType(), EMPTY_BINDINGS);

        // but if not, due to funky sub-classing, type variables, what follows
        // is a more complete processing a la Java ClassMate.

        final Class<?> refdRawType = typeRef.getClass();
        JavaType type = _fromClass(null, refdRawType, EMPTY_BINDINGS);
        JavaType genType = type.findSuperType(TypeReference.class);
        if (genType == null) { // sanity check; shouldn't occur
            throw new IllegalArgumentException("Unparameterized GenericType instance ("+refdRawType.getName()+")");
        }
        TypeBindings b = genType.getBindings();
        JavaType[] params = b.typeParameterArray();
        if (params.length == 0) {
            throw new IllegalArgumentException("Unparameterized GenericType instance ("+refdRawType.getName()+")");
        }
        return params[0];
    }*/

    /**
     * Method to call when resolving types of {@link Member}s
     * like Fields, Methods and Constructor parameters and there is a
     * {@link TypeBindings} (that describes binding of type parameters within
     * context) to pass.
     * This is typically used only by code in databind itself.
     *
     * @param type Type of a {@link Member} to resolve
     * @param contextBindings Type bindings from the context, often class in which
     *     member declared but may be subtype of that type (to bind actual bound
     *     type parametrers). Not used if {@code type} is of type {@code Class<?>}.
     *
     * @return Fully resolve type
     *
     * @since 2.12 as replacement for deprecated {@link #constructType(Type, TypeBindings)}
     */
    public JavaType resolveMemberType(Type type, TypeBindings contextBindings) {
        return _fromAny(null, type, contextBindings);
    }

    /*
    /**********************************************************
    /* Deprecated public factory methods
    /**********************************************************
     */

    /**
     * Method that you very likely should NOT be using -- you need to know a lot
     * about internal details of {@link TypeBindings} and even then it will probably
     * not do what you want.
     * Usually you would instead want to call one of {@code constructXxxType()}
     * methods (where {@code Xxx} would be "Array", "Collection[Like]", "Map[Like]"
     * or "Parametric").
     *
     * @deprecated Since 2.12
     */
    @Deprecated // since 2.12
    public JavaType constructType(Type type, TypeBindings bindings) {
        // 15-Jun-2020, tatu: To resolve (parts of) [databind#2796], need to
        //    call _fromClass() directly if we get `Class` argument
        if (type instanceof Class<?>) {
            JavaType resultType = _fromClass(null, (Class<?>) type, bindings);
            return _applyModifiers(type, resultType);
        }
        return _fromAny(null, type, bindings);
    }

    /**
     * @deprecated Since 2.7 (accidentally removed in 2.7.0; added back in 2.7.1)
     */
    @Deprecated
    public JavaType constructType(Type type, Class<?> contextClass) {
        JavaType contextType = (contextClass == null) ? null : constructType(contextClass);
        return constructType(type, contextType);
    }

    /**
     * @deprecated Since 2.7 (accidentally removed in 2.7.0; added back in 2.7.1)
     */
    @Deprecated
    public JavaType constructType(Type type, JavaType contextType) {
        TypeBindings bindings;
        if (contextType == null) {
            bindings = EMPTY_BINDINGS;
        } else {
            bindings = contextType.getBindings();
            // 16-Nov-2016, tatu: Unfortunately as per [databind#1456] this can't
            //   be made to work for some cases used to work (even if accidentally);
            //   however, we can try a simple heuristic to increase chances of
            //   compatibility from 2.6 code
            if (type.getClass() != Class.class) {
                // Ok: so, ideally we would test super-interfaces if necessary;
                // but let's assume most if not all cases are for classes.
                while (bindings.isEmpty()) {
                    contextType = contextType.getSuperClass();
                    if (contextType == null) {
                        break;
                    }
                    bindings = contextType.getBindings();
                }
            }
        }
        return _fromAny(null, type, bindings);
    }

    /*
    /**********************************************************
    /* Direct factory methods
    /**********************************************************
     */

    /**
     * Method for constructing an {@link ArrayType}.
     *<p>
     * NOTE: type modifiers are NOT called on array type itself; but are called
     * for element type (and other contained types)
     */
    public ArrayType constructArrayType(Class<?> elementType) {
        return ArrayType.construct(_fromAny(null, elementType, null), null);
    }

    /**
     * Method for constructing an {@link ArrayType}.
     *<p>
     * NOTE: type modifiers are NOT called on array type itself; but are called
     * for contained types.
     */
    public ArrayType constructArrayType(JavaType elementType) {
        return ArrayType.construct(elementType, null);
    }

    /**
     * Method for constructing a {@link CollectionType}.
     *<p>
     * NOTE: type modifiers are NOT called on Collection type itself; but are called
     * for contained types.
     */
    public CollectionType constructCollectionType(Class<? extends Collection> collectionClass,
            Class<?> elementClass) {
        return constructCollectionType(collectionClass,
                _fromClass(null, elementClass, EMPTY_BINDINGS));
    }

    /**
     * Method for constructing a {@link CollectionType}.
     *<p>
     * NOTE: type modifiers are NOT called on Collection type itself; but are called
     * for contained types.
     */
    public CollectionType constructCollectionType(Class<? extends Collection> collectionClass,
            JavaType elementType)
    {
        TypeBindings bindings = TypeBindings.createIfNeeded(collectionClass, elementType);
        CollectionType result = (CollectionType) _fromClass(null, collectionClass, bindings);
        // 17-May-2017, tatu: As per [databind#1415], we better verify bound values if (but only if)
        //    type being resolved was non-generic (i.e.element type was ignored)
        if (bindings.isEmpty() && (elementType != null)) {
            JavaType t = result.findSuperType(Collection.class);
            JavaType realET = t.getContentType();
            if (!realET.equals(elementType)) {
                throw new IllegalArgumentException(String.format(
                        "Non-generic Collection class %s did not resolve to something with element type %s but %s ",
                        ClassUtil.nameOf(collectionClass), elementType, realET));
            }
        }
        return result;
    }

    /**
     * Method for constructing a {@link CollectionLikeType}.
     *<p>
     * NOTE: type modifiers are NOT called on constructed type itself; but are called
     * for contained types.
     */
    public CollectionLikeType constructCollectionLikeType(Class<?> collectionClass, Class<?> elementClass) {
        return constructCollectionLikeType(collectionClass,
                _fromClass(null, elementClass, EMPTY_BINDINGS));
    }

    /**
     * Method for constructing a {@link CollectionLikeType}.
     *<p>
     * NOTE: type modifiers are NOT called on constructed type itself; but are called
     * for contained types.
     */
    public CollectionLikeType constructCollectionLikeType(Class<?> collectionClass, JavaType elementType) {
        JavaType type = _fromClass(null, collectionClass,
                TypeBindings.createIfNeeded(collectionClass, elementType));
        if (type instanceof CollectionLikeType) {
            return (CollectionLikeType) type;
        }
        return CollectionLikeType.upgradeFrom(type, elementType);
    }

    /**
     * Method for constructing a {@link MapType} instance
     *<p>
     * NOTE: type modifiers are NOT called on constructed type itself; but are called
     * for contained types.
     */
    public MapType constructMapType(Class<? extends Map> mapClass,
            Class<?> keyClass, Class<?> valueClass) {
        JavaType kt, vt;
        if (mapClass == Properties.class) {
            kt = vt = CORE_TYPE_STRING;
        } else {
            kt = _fromClass(null, keyClass, EMPTY_BINDINGS);
            vt = _fromClass(null, valueClass, EMPTY_BINDINGS);
        }
        return constructMapType(mapClass, kt, vt);
    }

    /**
     * Method for constructing a {@link MapType} instance
     *<p>
     * NOTE: type modifiers are NOT called on constructed type itself.
     */
    public MapType constructMapType(Class<? extends Map> mapClass, JavaType keyType, JavaType valueType) {
        TypeBindings bindings = TypeBindings.createIfNeeded(mapClass, new JavaType[] { keyType, valueType });
        MapType result = (MapType) _fromClass(null, mapClass, bindings);
        // 17-May-2017, tatu: As per [databind#1415], we better verify bound values if (but only if)
        //    type being resolved was non-generic (i.e.element type was ignored)
        if (bindings.isEmpty()) {
            JavaType t = result.findSuperType(Map.class);
            JavaType realKT = t.getKeyType();
            if (!realKT.equals(keyType)) {
                throw new IllegalArgumentException(String.format(
                        "Non-generic Map class %s did not resolve to something with key type %s but %s ",
                        ClassUtil.nameOf(mapClass), keyType, realKT));
            }
            JavaType realVT = t.getContentType();
            if (!realVT.equals(valueType)) {
                throw new IllegalArgumentException(String.format(
                        "Non-generic Map class %s did not resolve to something with value type %s but %s ",
                        ClassUtil.nameOf(mapClass), valueType, realVT));
            }
        }
        return result;
    }

    /**
     * Method for constructing a {@link MapLikeType} instance
     *<p>
     * NOTE: type modifiers are NOT called on constructed type itself; but are called
     * for contained types.
     */
    public MapLikeType constructMapLikeType(Class<?> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return constructMapLikeType(mapClass,
                _fromClass(null, keyClass, EMPTY_BINDINGS),
                _fromClass(null, valueClass, EMPTY_BINDINGS));
    }

    /**
     * Method for constructing a {@link MapLikeType} instance
     *<p>
     * NOTE: type modifiers are NOT called on constructed type itself.
     */
    public MapLikeType constructMapLikeType(Class<?> mapClass, JavaType keyType, JavaType valueType) {
        // 19-Oct-2015, tatu: Allow case of no-type-variables, since it seems likely to be
        //    a valid use case here
        JavaType type = _fromClass(null, mapClass,
                TypeBindings.createIfNeeded(mapClass, new JavaType[] { keyType, valueType }));
        if (type instanceof MapLikeType) {
            return (MapLikeType) type;
        }
        return MapLikeType.upgradeFrom(type, keyType, valueType);
    }

    /**
     * Method for constructing a type instance with specified parameterization.
     *<p>
     * NOTE: type modifiers are NOT called on constructed type itself.
     */
    public JavaType constructSimpleType(Class<?> rawType, JavaType[] parameterTypes) {
        return _fromClass(null, rawType, TypeBindings.create(rawType, parameterTypes));
    }

    /**
     * Method for constructing a type instance with specified parameterization.
     *
     * @since 2.6
     *
     * @deprecated Since 2.7
     */
    @Deprecated
    public JavaType constructSimpleType(Class<?> rawType, Class<?> parameterTarget,
            JavaType[] parameterTypes)
    {
        return constructSimpleType(rawType, parameterTypes);
    }

    /**
     * Method for constructing a {@link ReferenceType} instance with given type parameter
     * (type MUST take one and only one type parameter)
     *<p>
     * NOTE: type modifiers are NOT called on constructed type itself.
     *
     * @since 2.6
     */
    public JavaType constructReferenceType(Class<?> rawType, JavaType referredType)
    {
        return ReferenceType.construct(rawType,
                TypeBindings.create(rawType, referredType), // [databind#2091]
                null, null, // or super-class, interfaces?
                referredType);
    }

    /**
     * Method that use by core Databind functionality, and that should NOT be called
     * by application code outside databind package.
     *<p>
     * Unchecked here not only means that no checks are made as to whether given class
     * might be non-simple type (like {@link CollectionType}) but also that most of supertype
     * information is not gathered. This means that unless called on primitive types or
     * {@link String}, results are probably not what you want to use.
     *
     * @deprecated Since 2.8, to indicate users should never call this method.
     */
    @Deprecated // since 2.8
    public JavaType uncheckedSimpleType(Class<?> cls) {
        // 18-Oct-2015, tatu: Not sure how much problem missing super-type info is here
        return _constructSimple(cls, EMPTY_BINDINGS, null, null);
    }

    /**
     * Factory method for constructing {@link JavaType} that
     * represents a parameterized type. For example, to represent
     * type {@code List<Set<Integer>>}, you could
     * call
     *<pre>
     *  JavaType inner = TypeFactory.constructParametricType(Set.class, Integer.class);
     *  return TypeFactory.constructParametricType(List.class, inner);
     *</pre>
     *<p>
     * NOTE: since 2.11.2 {@link TypeModifier}s ARE called on result (fix for [databind#2796])
     *
     * @param parametrized Type-erased type to parameterize
     * @param parameterClasses Type parameters to apply
     *
     * @since 2.5 NOTE: was briefly deprecated for 2.6
     */
    public JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        int len = parameterClasses.length;
        JavaType[] pt = new JavaType[len];
        for (int i = 0; i < len; ++i) {
            pt[i] = _fromClass(null, parameterClasses[i], EMPTY_BINDINGS);
        }
        return constructParametricType(parametrized, pt);
    }

    /**
     * Factory method for constructing {@link JavaType} that
     * represents a parameterized type. For example, to represent
     * type {@code List<Set<Integer>>}, you could
     *<pre>
     *  JavaType inner = TypeFactory.constructParametricType(Set.class, Integer.class);
     *  return TypeFactory.constructParametricType(List.class, inner);
     *</pre>
     *<p>
     * NOTE: since 2.11.2 {@link TypeModifier}s ARE called on result (fix for [databind#2796])
     *
     * @param rawType Actual type-erased type
     * @param parameterTypes Type parameters to apply
     *
     * @return Fully resolved type for given base type and type parameters
     */
    public JavaType constructParametricType(Class<?> rawType, JavaType... parameterTypes)
    {
        return constructParametricType(rawType, TypeBindings.create(rawType, parameterTypes));
    }

    /**
     * Factory method for constructing {@link JavaType} that
     * represents a parameterized type. The type's parameters are
     * specified as an instance of {@link TypeBindings}. This
     * is useful if you already have the type's parameters such
     * as those found on {@link JavaType}. For example, you could call
     * <pre>
     *   return TypeFactory.constructParametricType(ArrayList.class, javaType.getBindings());
     * </pre>
     * This effectively applies the parameterized types from one
     * {@link JavaType} to another class.
     *
     * @param rawType Actual type-erased type
     * @param parameterTypes Type bindings for the raw type
     *
     * @since 2.12
     */
    public JavaType constructParametricType(Class<?> rawType, TypeBindings parameterTypes)
    {
        // 16-Jul-2020, tatu: Since we do not call `_fromAny()`, need to make
        //   sure `TypeModifier`s are applied:
        JavaType resultType =  _fromClass(null, rawType, parameterTypes);
        return _applyModifiers(rawType, resultType);
    }

    /**
     * @since 2.5
     * @deprecated since 2.9 Use {@link #constructParametricType(Class,JavaType...)} instead
     */
    @Deprecated
    public JavaType constructParametrizedType(Class<?> parametrized, Class<?> parametersFor,
            JavaType... parameterTypes)
    {
        return constructParametricType(parametrized, parameterTypes);
    }

    /**
     * @since 2.5
     * @deprecated since 2.9 Use {@link #constructParametricType(Class,Class...)} instead
     */
    @Deprecated
    public JavaType constructParametrizedType(Class<?> parametrized, Class<?> parametersFor,
            Class<?>... parameterClasses)
    {
        return constructParametricType(parametrized, parameterClasses);
    }

    /*
    /**********************************************************
    /* Direct factory methods for "raw" variants, used when
    /* parameterization is unknown
    /**********************************************************
     */

    /**
     * Method that can be used to construct "raw" Collection type; meaning that its
     * parameterization is unknown.
     * This is similar to using <code>Object.class</code> parameterization,
     * and is equivalent to calling:
     *<pre>
     *  typeFactory.constructCollectionType(collectionClass, typeFactory.unknownType());
     *</pre>
     *<p>
     * This method should only be used if parameterization is completely unavailable.
     */
    public CollectionType constructRawCollectionType(Class<? extends Collection> collectionClass) {
        return constructCollectionType(collectionClass, unknownType());
    }

    /**
     * Method that can be used to construct "raw" Collection-like type; meaning that its
     * parameterization is unknown.
     * This is similar to using <code>Object.class</code> parameterization,
     * and is equivalent to calling:
     *<pre>
     *  typeFactory.constructCollectionLikeType(collectionClass, typeFactory.unknownType());
     *</pre>
     *<p>
     * This method should only be used if parameterization is completely unavailable.
     */
    public CollectionLikeType constructRawCollectionLikeType(Class<?> collectionClass) {
        return constructCollectionLikeType(collectionClass, unknownType());
    }

    /**
     * Method that can be used to construct "raw" Map type; meaning that its
     * parameterization is unknown.
     * This is similar to using <code>Object.class</code> parameterization,
     * and is equivalent to calling:
     *<pre>
     *  typeFactory.constructMapType(collectionClass, typeFactory.unknownType(), typeFactory.unknownType());
     *</pre>
     *<p>
     * This method should only be used if parameterization is completely unavailable.
     */
    public MapType constructRawMapType(Class<? extends Map> mapClass) {
        return constructMapType(mapClass, unknownType(), unknownType());
    }

    /**
     * Method that can be used to construct "raw" Map-like type; meaning that its
     * parameterization is unknown.
     * This is similar to using <code>Object.class</code> parameterization,
     * and is equivalent to calling:
     *<pre>
     *  typeFactory.constructMapLikeType(collectionClass, typeFactory.unknownType(), typeFactory.unknownType());
     *</pre>
     *<p>
     * This method should only be used if parameterization is completely unavailable.
     */
    public MapLikeType constructRawMapLikeType(Class<?> mapClass) {
        return constructMapLikeType(mapClass, unknownType(), unknownType());
    }

    /*
    /**********************************************************
    /* Low-level factory methods
    /**********************************************************
     */

    private JavaType _mapType(Class<?> rawClass, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces)
    {
        JavaType kt, vt;

        // 28-May-2015, tatu: Properties are special, as per [databind#810]; fake "correct" parameter sig
        if (rawClass == Properties.class) {
            kt = vt = CORE_TYPE_STRING;
        } else {
            List<JavaType> typeParams = bindings.getTypeParameters();
            // ok to have no types ("raw")
            final int pc = typeParams.size();
            switch (pc) {
            case 0: // acceptable?
                kt = vt = _unknownType();
                break;
            case 2:
                kt = typeParams.get(0);
                vt = typeParams.get(1);
                break;
            default:
                throw new IllegalArgumentException(String.format(
"Strange Map type %s with %d type parameter%s (%s), can not resolve",
ClassUtil.nameOf(rawClass), pc, (pc == 1) ? "" : "s", bindings));
            }
        }
        return MapType.construct(rawClass, bindings, superClass, superInterfaces, kt, vt);
    }

    private JavaType _collectionType(Class<?> rawClass, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces)
    {
        List<JavaType> typeParams = bindings.getTypeParameters();
        // ok to have no types ("raw")
        JavaType ct;
        if (typeParams.isEmpty()) {
            ct = _unknownType();
        } else if (typeParams.size() == 1) {
            ct = typeParams.get(0);
        } else {
            throw new IllegalArgumentException("Strange Collection type "+rawClass.getName()+": cannot determine type parameters");
        }
        return CollectionType.construct(rawClass, bindings, superClass, superInterfaces, ct);
    }

    private JavaType _referenceType(Class<?> rawClass, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces)
    {
        List<JavaType> typeParams = bindings.getTypeParameters();
        // ok to have no types ("raw")
        JavaType ct;
        if (typeParams.isEmpty()) {
            ct = _unknownType();
        } else if (typeParams.size() == 1) {
            ct = typeParams.get(0);
        } else {
            throw new IllegalArgumentException("Strange Reference type "+rawClass.getName()+": cannot determine type parameters");
        }
        return ReferenceType.construct(rawClass, bindings, superClass, superInterfaces, ct);
    }

    /**
     * Factory method to call when no special {@link JavaType} is needed,
     * no generic parameters are passed. Default implementation may check
     * pre-constructed values for "well-known" types, but if none found
     * will simply call {@link #_newSimpleType}
     *
     * @since 2.7
     */
    protected JavaType _constructSimple(Class<?> raw, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces)
    {
        if (bindings.isEmpty()) {
            JavaType result = _findWellKnownSimple(raw);
            if (result != null) {
                return result;
            }
        }
        return _newSimpleType(raw, bindings, superClass, superInterfaces);
    }

    /**
     * Factory method that is to create a new {@link SimpleType} with no
     * checks whatsoever. Default implementation calls the single argument
     * constructor of {@link SimpleType}.
     *
     * @since 2.7
     */
    protected JavaType _newSimpleType(Class<?> raw, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces)
    {
        return new SimpleType(raw, bindings, superClass, superInterfaces);
    }

    protected JavaType _unknownType() {
        /* 15-Sep-2015, tatu: Prior to 2.7, we constructed new instance for each call.
         *    This may have been due to potential mutability of the instance; but that
         *    should not be issue any more, and creation is somewhat wasteful. So let's
         *    try reusing singleton/flyweight instance.
         */
        return CORE_TYPE_OBJECT;
    }

    /**
     * Helper method called to see if requested, non-generic-parameterized
     * type is one of common, "well-known" types, instances of which are
     * pre-constructed and do not need dynamic caching.
     *
     * @since 2.7
     */
    protected JavaType _findWellKnownSimple(Class<?> clz) {
        if (clz.isPrimitive()) {
            if (clz == CLS_BOOL) return CORE_TYPE_BOOL;
            if (clz == CLS_INT) return CORE_TYPE_INT;
            if (clz == CLS_LONG) return CORE_TYPE_LONG;
        } else {
            if (clz == CLS_STRING) return CORE_TYPE_STRING;
            if (clz == CLS_OBJECT) return CORE_TYPE_OBJECT; // since 2.7
//            if (clz == CLS_JSON_NODE) return CORE_TYPE_JSON_NODE; // since 2.10
        }
        return null;
    }

    /*
    /**********************************************************
    /* Actual type resolution, traversal
    /**********************************************************
     */

    /**
     * Factory method that can be used if type information is passed
     * as Java typing returned from <code>getGenericXxx</code> methods
     * (usually for a return or argument type).
     */
    protected JavaType _fromAny(ClassStack context, Type srcType, TypeBindings bindings)
    {
        JavaType resultType;

        // simple class?
        if (srcType instanceof Class<?>) {
            // Important: remove possible bindings since this is type-erased thingy
            resultType = _fromClass(context, (Class<?>) srcType, EMPTY_BINDINGS);
        }
        // But if not, need to start resolving.
        else if (srcType instanceof ParameterizedType) {
            resultType = _fromParamType(context, (ParameterizedType) srcType, bindings);
        }
        else if (srcType instanceof JavaType) { // [databind#116]
            // no need to modify further if we already had JavaType
            return (JavaType) srcType;
        }
        else if (srcType instanceof GenericArrayType) {
            resultType = _fromArrayType(context, (GenericArrayType) srcType, bindings);
        }
        else if (srcType instanceof TypeVariable<?>) {
            resultType = _fromVariable(context, (TypeVariable<?>) srcType, bindings);
        }
        else if (srcType instanceof WildcardType) {
            resultType = _fromWildcard(context, (WildcardType) srcType, bindings);
        } else {
            // sanity check
            throw new IllegalArgumentException("Unrecognized Type: "+((srcType == null) ? "[null]" : srcType.toString()));
        }
        // 21-Feb-2016, nateB/tatu: as per [databind#1129] (applied for 2.7.2),
        //   we do need to let all kinds of types to be refined, esp. for Scala module.
        return _applyModifiers(srcType, resultType);
    }

    protected JavaType _applyModifiers(Type srcType, JavaType resolvedType)
    {
        if (_modifiers == null) {
            return resolvedType;
        }
        JavaType resultType = resolvedType;
        TypeBindings b = resultType.getBindings();
        if (b == null) {
            b = EMPTY_BINDINGS;
        }
        for (TypeModifier mod : _modifiers) {
            JavaType t = mod.modifyType(resultType, srcType, b, this);
            if (t == null) {
                throw new IllegalStateException(String.format(
                        "TypeModifier %s (of type %s) return null for type %s",
                        mod, mod.getClass().getName(), resultType));
            }
            resultType = t;
        }
        return resultType;
    }

    /**
     * @param bindings Mapping of formal parameter declarations (for generic
     *   types) into actual types
     */
    protected JavaType _fromClass(ClassStack context, Class<?> rawType, TypeBindings bindings)
    {
        // Very first thing: small set of core types we know well:
        JavaType result = _findWellKnownSimple(rawType);
        if (result != null) {
            return result;
        }
        // Barring that, we may have recently constructed an instance
        final Object key;
        if ((bindings == null) || bindings.isEmpty()) {
            key = rawType;
        } else {
            key = bindings.asKey(rawType);
        }
        result = _typeCache.get(key); // ok, cache object is synced
        if (result != null) {
            return result;
        }

        // 15-Oct-2015, tatu: recursive reference?
        if (context == null) {
            context = new ClassStack(rawType);
        } else {
            ClassStack prev = context.find(rawType);
            if (prev != null) {
                // Self-reference: needs special handling, then...
                ResolvedRecursiveType selfRef = new ResolvedRecursiveType(rawType, EMPTY_BINDINGS);
                prev.addSelfReference(selfRef);
                return selfRef;
            }
            // no, but need to update context to allow for proper cycle resolution
            context = context.child(rawType);
        }

        // First: do we have an array type?
        if (rawType.isArray()) {
            result = ArrayType.construct(_fromAny(context, rawType.getComponentType(), bindings),
                    bindings);
        } else {
            // If not, need to proceed by first resolving parent type hierarchy

            JavaType superClass;
            JavaType[] superInterfaces;

            if (rawType.isInterface()) {
                superClass = null;
                superInterfaces = _resolveSuperInterfaces(context, rawType, bindings);
            } else {
                // Note: even Enums can implement interfaces, so cannot drop those
                superClass = _resolveSuperClass(context, rawType, bindings);
                superInterfaces = _resolveSuperInterfaces(context, rawType, bindings);
            }

            // 19-Oct-2015, tatu: Bit messy, but we need to 'fix' java.util.Properties here...
            if (rawType == Properties.class) {
                result = MapType.construct(rawType, bindings, superClass, superInterfaces,
                        CORE_TYPE_STRING, CORE_TYPE_STRING);
            }
            // And then check what flavor of type we got. Start by asking resolved
            // super-type if refinement is all that is needed?
            else if (superClass != null) {
                result = superClass.refine(rawType, bindings, superClass, superInterfaces);
            }
            // if not, perhaps we are now resolving a well-known class or interface?
            if (result == null) {
                result = _fromWellKnownClass(context, rawType, bindings, superClass, superInterfaces);
                if (result == null) {
                    result = _fromWellKnownInterface(context, rawType, bindings, superClass, superInterfaces);
                    if (result == null) {
                        // but if nothing else, "simple" class for now:
                        result = _newSimpleType(rawType, bindings, superClass, superInterfaces);
                    }
                }
            }
        }
        context.resolveSelfReferences(result);
        // 16-Jul-2016, tatu: [databind#1302] is solved different way, but ideally we shouldn't
        //     cache anything with partially resolved `ResolvedRecursiveType`... so maybe improve
        if (!result.hasHandlers()) {
            _typeCache.putIfAbsent(key, result); // cache object syncs
        }
        return result;
    }

    protected JavaType _resolveSuperClass(ClassStack context, Class<?> rawType, TypeBindings parentBindings)
    {
        Type parent = ClassUtil.getGenericSuperclass(rawType);
        if (parent == null) {
            return null;
        }
        return _fromAny(context, parent, parentBindings);
    }

    protected JavaType[] _resolveSuperInterfaces(ClassStack context, Class<?> rawType, TypeBindings parentBindings)
    {
        Type[] types = ClassUtil.getGenericInterfaces(rawType);
        if (types == null || types.length == 0) {
            return NO_TYPES;
        }
        int len = types.length;
        JavaType[] resolved = new JavaType[len];
        for (int i = 0; i < len; ++i) {
            Type type = types[i];
            resolved[i] = _fromAny(context, type, parentBindings);
        }
        return resolved;
    }

    /**
     * Helper class used to check whether exact class for which type is being constructed
     * is one of well-known base interfaces or classes that indicates alternate
     * {@link JavaType} implementation.
     */
    protected JavaType _fromWellKnownClass(ClassStack context, Class<?> rawType, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces)
    {
        if (bindings == null) {
            bindings = EMPTY_BINDINGS;
        }
        // Quite simple when we resolving exact class/interface; start with that
        if (rawType == Map.class) {
            return _mapType(rawType, bindings, superClass, superInterfaces);
        }
        if (rawType == Collection.class) {
            return _collectionType(rawType, bindings, superClass, superInterfaces);
        }
        // and since 2.6 one referential type
        if (rawType == AtomicReference.class) {
            return _referenceType(rawType, bindings, superClass, superInterfaces);
        }
        // 01-Nov-2015, tatu: As of 2.7, couple of potential `CollectionLikeType`s (like
        //    `Iterable`, `Iterator`), and `MapLikeType`s (`Map.Entry`) are not automatically
        //    detected, related to difficulties in propagating type upwards (Iterable, for
        //    example, is a weak, tag-on type). They may be detectable in future.
        return null;
    }

    protected JavaType _fromWellKnownInterface(ClassStack context, Class<?> rawType, TypeBindings bindings,
            JavaType superClass, JavaType[] superInterfaces)
    {
        // But that's not all: may be possible current type actually implements an
        // interface type. So...
        final int intCount = superInterfaces.length;

        for (int i = 0; i < intCount; ++i) {
            JavaType result = superInterfaces[i].refine(rawType, bindings, superClass, superInterfaces);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * This method deals with parameterized types, that is,
     * first class generic classes.
     */
    protected JavaType _fromParamType(ClassStack context, ParameterizedType ptype,
            TypeBindings parentBindings)
    {
        // Assumption here is we'll always get Class, not one of other Types
        Class<?> rawType = (Class<?>) ptype.getRawType();

        // 29-Oct-2015, tatu: For performance reasons, let's streamline handling of
        //   couple of not-so-useful parametric types
        if (rawType == CLS_ENUM) {
            return CORE_TYPE_ENUM;
        }
        if (rawType == CLS_COMPARABLE) {
            return CORE_TYPE_COMPARABLE;
        }

        // First: what is the actual base type? One odd thing is that 'getRawType'
        // returns Type, not Class<?> as one might expect. But let's assume it is
        // always of type Class: if not, need to add more code to resolve it to Class.
        Type[] args = ptype.getActualTypeArguments();
        int paramCount = (args == null) ? 0 : args.length;
        TypeBindings newBindings;

        if (paramCount == 0) {
            newBindings = EMPTY_BINDINGS;
        } else {
            JavaType[] pt = new JavaType[paramCount];
            for (int i = 0; i < paramCount; ++i) {
                pt[i] = _fromAny(context, args[i], parentBindings);
            }
            newBindings = TypeBindings.create(rawType, pt);
        }
        return _fromClass(context, rawType, newBindings);
    }

    protected JavaType _fromArrayType(ClassStack context, GenericArrayType type, TypeBindings bindings)
    {
        JavaType elementType = _fromAny(context, type.getGenericComponentType(), bindings);
        return ArrayType.construct(elementType, bindings);
    }

    protected JavaType _fromVariable(ClassStack context, TypeVariable<?> var, TypeBindings bindings)
    {
        // ideally should find it via bindings:
        final String name = var.getName();
        if (bindings == null) {
            throw new IllegalArgumentException("Null `bindings` passed (type variable \""+name+"\")");
        }
        JavaType type = bindings.findBoundType(name);
        if (type != null) {
            return type;
        }
        // but if not, use bounds... note that approach here is simplistic; not taking
        // into account possible multiple bounds, nor consider upper bounds.
        if (bindings.hasUnbound(name)) {
            return CORE_TYPE_OBJECT;
        }
        bindings = bindings.withUnboundVariable(name);

        final Type[] bounds;

        // 15-Jan-2019, tatu: As weird as this looks, apparently on some platforms (Arm CPU, mobile
        //    devices), unsynchronized internal access can lead to issues, see:
        //
        //  https://vmlens.com/articles/java-lang-reflect-typevariable-getbounds-is-not-thread-safe/
        //
        //    No good way to reproduce but since this should not be on critical path, let's add
        //    syncing as it seems potentially necessary.
        synchronized (var) {
            bounds = var.getBounds();
        }
        return _fromAny(context, bounds[0], bindings);
    }

    protected JavaType _fromWildcard(ClassStack context, WildcardType type, TypeBindings bindings)
    {
        /* Similar to challenges with TypeVariable, we may have multiple upper bounds.
         * But it is also possible that if upper bound defaults to Object, we might
         * want to consider lower bounds instead.
         * For now, we won't try anything more advanced; above is just for future reference.
         */
        return _fromAny(context, type.getUpperBounds()[0], bindings);
    }
}
