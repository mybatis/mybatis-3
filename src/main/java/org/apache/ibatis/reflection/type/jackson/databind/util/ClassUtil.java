package org.apache.ibatis.reflection.type.jackson.databind.util;

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

public final class ClassUtil
{
    private final static Class<?> CLS_OBJECT = Object.class;

    private final static Annotation[] NO_ANNOTATIONS = new Annotation[0];
    private final static Ctor[] NO_CTORS = new Ctor[0];

    private final static Iterator<?> EMPTY_ITERATOR = Collections.emptyIterator();

    /*
    /**********************************************************
    /* Simple factory methods
    /**********************************************************
     */

    /**
     * @since 2.7
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EMPTY_ITERATOR;
    }

    /*
    /**********************************************************
    /* Methods that deal with inheritance
    /**********************************************************
     */

    /**
     * Method that will find all sub-classes and implemented interfaces
     * of a given class or interface. Classes are listed in order of
     * precedence, starting with the immediate super-class, followed by
     * interfaces class directly declares to implemented, and then recursively
     * followed by parent of super-class and so forth.
     * Note that <code>Object.class</code> is not included in the list
     * regardless of whether <code>endBefore</code> argument is defined or not.
     *
     * @param endBefore Super-type to NOT include in results, if any; when
     *    encountered, will be ignored (and no super types are checked).
     *
     * @since 2.7
     */
    public static List<JavaType> findSuperTypes(JavaType type, Class<?> endBefore,
            boolean addClassItself) {
        if ((type == null) || type.hasRawClass(endBefore) || type.hasRawClass(Object.class)) {
            return Collections.emptyList();
        }
        List<JavaType> result = new ArrayList<JavaType>(8);
        _addSuperTypes(type, endBefore, result, addClassItself);
        return result;
    }

    /**
     * @since 2.7
     */
    public static List<Class<?>> findRawSuperTypes(Class<?> cls, Class<?> endBefore, boolean addClassItself) {
        if ((cls == null) || (cls == endBefore) || (cls == Object.class)) {
            return Collections.emptyList();
        }
        List<Class<?>> result = new ArrayList<Class<?>>(8);
        _addRawSuperTypes(cls, endBefore, result, addClassItself);
        return result;
    }

    /**
     * Method for finding all super classes (but not super interfaces) of given class,
     * starting with the immediate super class and ending in the most distant one.
     * Class itself is included if <code>addClassItself</code> is true.
     *<p>
     * NOTE: mostly/only called to resolve mix-ins as that's where we do not care
     * about fully-resolved types, just associated annotations.
     *
     * @since 2.7
     */
    public static List<Class<?>> findSuperClasses(Class<?> cls, Class<?> endBefore,
            boolean addClassItself) {
        List<Class<?>> result = new ArrayList<Class<?>>(8);
        if ((cls != null) && (cls != endBefore))  {
            if (addClassItself) {
                result.add(cls);
            }
            while ((cls = cls.getSuperclass()) != null) {
                if (cls == endBefore) {
                    break;
                }
                result.add(cls);
            }
        }
        return result;
    }

    @Deprecated // since 2.7
    public static List<Class<?>> findSuperTypes(Class<?> cls, Class<?> endBefore) {
        return findSuperTypes(cls, endBefore, new ArrayList<Class<?>>(8));
    }

    @Deprecated // since 2.7
    public static List<Class<?>> findSuperTypes(Class<?> cls, Class<?> endBefore, List<Class<?>> result) {
        _addRawSuperTypes(cls, endBefore, result, false);
        return result;
    }

    private static void _addSuperTypes(JavaType type, Class<?> endBefore, Collection<JavaType> result,
            boolean addClassItself)
    {
        if (type == null) {
            return;
        }
        final Class<?> cls = type.getRawClass();
        if (cls == endBefore || cls == Object.class) { return; }
        if (addClassItself) {
            if (result.contains(type)) { // already added, no need to check supers
                return;
            }
            result.add(type);
        }
        for (JavaType intCls : type.getInterfaces()) {
            _addSuperTypes(intCls, endBefore, result, true);
        }
        _addSuperTypes(type.getSuperClass(), endBefore, result, true);
    }

    private static void _addRawSuperTypes(Class<?> cls, Class<?> endBefore, Collection<Class<?>> result, boolean addClassItself) {
        if (cls == endBefore || cls == null || cls == Object.class) { return; }
        if (addClassItself) {
            if (result.contains(cls)) { // already added, no need to check supers
                return;
            }
            result.add(cls);
        }
        for (Class<?> intCls : _interfaces(cls)) {
            _addRawSuperTypes(intCls, endBefore, result, true);
        }
        _addRawSuperTypes(cls.getSuperclass(), endBefore, result, true);
    }

    /*
    /**********************************************************
    /* Class type detection methods
    /**********************************************************
     */

    /**
     * @return Null if class might be a bean; type String (that identifies
     *   why it's not a bean) if not
     */
    public static String canBeABeanType(Class<?> type)
    {
        // First: language constructs that ain't beans:
        if (type.isAnnotation()) {
            return "annotation";
        }
        if (type.isArray()) {
            return "array";
        }
        if (Enum.class.isAssignableFrom(type)) {
            return "enum";
        }
        if (type.isPrimitive()) {
            return "primitive";
        }

        // Anything else? Seems valid, then
        return null;
    }

    public static String isLocalType(Class<?> type, boolean allowNonStatic)
    {
        /* As per [JACKSON-187], GAE seems to throw SecurityExceptions
         * here and there... and GAE itself has a bug, too
         * Bah. So we need to catch some wayward exceptions on GAE
         */
        try {
            final boolean isStatic = Modifier.isStatic(type.getModifiers());

            // one more: method locals, anonymous, are not good:
            // 23-Jun-2020, tatu: [databind#2758] With JDK14+ should allow
            //    local Record types, however
            if (!isStatic && hasEnclosingMethod(type)) {
                return "local/anonymous";
            }
            /* But how about non-static inner classes? Can't construct
             * easily (theoretically, we could try to check if parent
             * happens to be enclosing... but that gets convoluted)
             */
            if (!allowNonStatic) {
                if (!isStatic && getEnclosingClass(type) != null) {
                    return "non-static member class";
                }
            }
        }
        catch (SecurityException e) { }
        catch (NullPointerException e) { }
        return null;
    }

    /**
     * Method for finding enclosing class for non-static inner classes
     */
    public static Class<?> getOuterClass(Class<?> type)
    {
        // as above, GAE has some issues...
        if (!Modifier.isStatic(type.getModifiers())) {
            try {
                // one more: method locals, anonymous, are not good:
                if (hasEnclosingMethod(type)) {
                    return null;
                }
                return getEnclosingClass(type);
            } catch (SecurityException e) { }
        }
        return null;
    }

    /**
     * Helper method used to weed out dynamic Proxy types; types that do
     * not expose concrete method API that we could use to figure out
     * automatic Bean (property) based serialization.
     */
    public static boolean isProxyType(Class<?> type)
    {
        // As per [databind#57], should NOT disqualify JDK proxy:
        /*
        // Then: well-known proxy (etc) classes
        if (Proxy.isProxyClass(type)) {
            return true;
        }
        */
        String name = type.getName();
        // Hibernate uses proxies heavily as well:
        if (name.startsWith("net.sf.cglib.proxy.")
            || name.startsWith("org.hibernate.proxy.")) {
            return true;
        }
        // Not one of known proxies, nope:
        return false;
    }

    /**
     * Helper method that checks if given class is a concrete one;
     * that is, not an interface or abstract class.
     */
    public static boolean isConcrete(Class<?> type)
    {
        int mod = type.getModifiers();
        return (mod & (Modifier.INTERFACE | Modifier.ABSTRACT)) == 0;
    }

    public static boolean isConcrete(Member member)
    {
        int mod = member.getModifiers();
        return (mod & (Modifier.INTERFACE | Modifier.ABSTRACT)) == 0;
    }

    public static boolean isCollectionMapOrArray(Class<?> type)
    {
        if (type.isArray()) return true;
        if (Collection.class.isAssignableFrom(type)) return true;
        if (Map.class.isAssignableFrom(type)) return true;
        return false;
    }

    /*public static boolean isBogusClass(Class<?> cls) {
        return (cls == Void.class || cls == Void.TYPE
                || cls == org.apache.ibatis.type.resolved.jackson.databind.annotation.NoClass.class);
    }*/

    /**
     * Helper method for detecting Java14-added new {@code Record} types
     *
     * @since 2.12
     */
    public static boolean isRecordType(Class<?> cls) {
        Class<?> parent = cls.getSuperclass();
        return (parent != null) && "java.lang.Record".equals(parent.getName());
    }

    /**
     * @since 2.7
     */
    public static boolean isObjectOrPrimitive(Class<?> cls) {
        return (cls == CLS_OBJECT) || cls.isPrimitive();
    }

    /**
     * @since 2.9
     */
    public static boolean hasClass(Object inst, Class<?> raw) {
        // 10-Nov-2016, tatu: Could use `Class.isInstance()` if we didn't care
        //    about being exactly that type
        return (inst != null) && (inst.getClass() == raw);
    }

    /**
     * @since 2.9
     */
    public static void verifyMustOverride(Class<?> expType, Object instance,
            String method)
    {
        if (instance.getClass() != expType) {
            throw new IllegalStateException(String.format(
                    "Sub-class %s (of class %s) must override method '%s'",
                instance.getClass().getName(), expType.getName(), method));
        }
    }

    /*
    /**********************************************************
    /* Method type detection methods
    /**********************************************************
     */

    /**
     * @deprecated Since 2.6 not used; may be removed before 3.x
     */
    @Deprecated // since 2.6
    public static boolean hasGetterSignature(Method m)
    {
        // First: static methods can't be getters
        if (Modifier.isStatic(m.getModifiers())) {
            return false;
        }
        // Must take no args
        Class<?>[] pts = m.getParameterTypes();
        if (pts != null && pts.length != 0) {
            return false;
        }
        // Can't be a void method
        if (Void.TYPE == m.getReturnType()) {
            return false;
        }
        // Otherwise looks ok:
        return true;
    }

    /*
    /**********************************************************
    /* Exception handling; simple re-throw
    /**********************************************************
     */

    /**
     * Helper method that will check if argument is an {@link Error},
     * and if so, (re)throw it; otherwise just return
     *
     * @since 2.9
     */
    public static Throwable throwIfError(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        }
        return t;
    }

    /**
     * Helper method that will check if argument is an {@link RuntimeException},
     * and if so, (re)throw it; otherwise just return
     *
     * @since 2.9
     */
    public static Throwable throwIfRTE(Throwable t) {
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        return t;
    }

    /**
     * Helper method that will check if argument is an {@link IOException},
     * and if so, (re)throw it; otherwise just return
     *
     * @since 2.9
     */
    public static Throwable throwIfIOE(Throwable t) throws IOException {
        if (t instanceof IOException) {
            throw (IOException) t;
        }
        return t;
    }

    /*
    /**********************************************************
    /* Exception handling; other
    /**********************************************************
     */

    /**
     * Method that can be used to find the "root cause", innermost
     * of chained (wrapped) exceptions.
     */
    public static Throwable getRootCause(Throwable t)
    {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    /**
     * Method that works like by calling {@link #getRootCause} and then
     * either throwing it (if instanceof {@link IOException}), or
     * return.
     *
     * @since 2.8
     */
    public static Throwable throwRootCauseIfIOE(Throwable t) throws IOException {
        return throwIfIOE(getRootCause(t));
    }

    /**
     * Method that will wrap 't' as an {@link IllegalArgumentException} if it
     * is a checked exception; otherwise (runtime exception or error) throw as is
     */
    public static void throwAsIAE(Throwable t) {
        throwAsIAE(t, t.getMessage());
    }

    /**
     * Method that will wrap 't' as an {@link IllegalArgumentException} (and with
     * specified message) if it
     * is a checked exception; otherwise (runtime exception or error) throw as is
     */
    public static void throwAsIAE(Throwable t, String msg)
    {
        throwIfRTE(t);
        throwIfError(t);
        throw new IllegalArgumentException(msg, t);
    }

    /**
     * @since 2.9
     */
    /*public static <T> T throwAsMappingException(DeserializationContext ctxt,
            IOException e0) throws JsonMappingException {
        if (e0 instanceof JsonMappingException) {
            throw (JsonMappingException) e0;
        }
        throw JsonMappingException.from(ctxt, e0.getMessage())
            .withCause(e0);
    }*/

    /**
     * Method that will locate the innermost exception for given Throwable;
     * and then wrap it as an {@link IllegalArgumentException} if it
     * is a checked exception; otherwise (runtime exception or error) throw as is
     */
    public static void unwrapAndThrowAsIAE(Throwable t)
    {
        throwAsIAE(getRootCause(t));
    }

    /**
     * Method that will locate the innermost exception for given Throwable;
     * and then wrap it as an {@link IllegalArgumentException} if it
     * is a checked exception; otherwise (runtime exception or error) throw as is
     */
    public static void unwrapAndThrowAsIAE(Throwable t, String msg)
    {
        throwAsIAE(getRootCause(t), msg);
    }

    /**
     * Helper method that encapsulate logic in trying to close output generator
     * in case of failure; useful mostly in forcing flush()ing as otherwise
     * error conditions tend to be hard to diagnose. However, it is often the
     * case that output state may be corrupt so we need to be prepared for
     * secondary exception without masking original one.
     *
     * @since 2.8
     */
    /*public static void closeOnFailAndThrowAsIOE(JsonGenerator g, Exception fail)
        throws IOException
    {
        // 04-Mar-2014, tatu: Let's try to prevent auto-closing of
        //    structures, which typically causes more damage.
        g.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        try {
            g.close();
        } catch (Exception e) {
            fail.addSuppressed(e);
        }
        throwIfIOE(fail);
        throwIfRTE(fail);
        throw new RuntimeException(fail);
    }*/

    /**
     * Helper method that encapsulate logic in trying to close given {@link Closeable}
     * in case of failure; useful mostly in forcing flush()ing as otherwise
     * error conditions tend to be hard to diagnose. However, it is often the
     * case that output state may be corrupt so we need to be prepared for
     * secondary exception without masking original one.
     *
     * @since 2.8
     */
    /*public static void closeOnFailAndThrowAsIOE(JsonGenerator g,
            Closeable toClose, Exception fail)
        throws IOException
    {
        if (g != null) {
            g.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
            try {
                g.close();
            } catch (Exception e) {
                fail.addSuppressed(e);
            }
        }
        if (toClose != null) {
            try {
                toClose.close();
            } catch (Exception e) {
                fail.addSuppressed(e);
            }
        }
        throwIfIOE(fail);
        throwIfRTE(fail);
        throw new RuntimeException(fail);
    }*/

    /*
    /**********************************************************
    /* Instantiation
    /**********************************************************
     */

    /**
     * Method that can be called to try to create an instantiate of
     * specified type. Instantiation is done using default no-argument
     * constructor.
     *
     * @param canFixAccess Whether it is possible to try to change access
     *   rights of the default constructor (in case it is not publicly
     *   accessible) or not.
     *
     * @throws IllegalArgumentException If instantiation fails for any reason;
     *    except for cases where constructor throws an unchecked exception
     *    (which will be passed as is)
     */
    public static <T> T createInstance(Class<T> cls, boolean canFixAccess)
        throws IllegalArgumentException
    {
        Constructor<T> ctor = findConstructor(cls, canFixAccess);
        if (ctor == null) {
            throw new IllegalArgumentException("Class "+cls.getName()+" has no default (no arg) constructor");
        }
        try {
            return ctor.newInstance();
        } catch (Exception e) {
            ClassUtil.unwrapAndThrowAsIAE(e, "Failed to instantiate class "+cls.getName()+", problem: "+e.getMessage());
            return null;
        }
    }

    public static <T> Constructor<T> findConstructor(Class<T> cls, boolean forceAccess)
        throws IllegalArgumentException
    {
        try {
            Constructor<T> ctor = cls.getDeclaredConstructor();
            if (forceAccess) {
                checkAndFixAccess(ctor, forceAccess);
            } else {
                // Has to be public...
                if (!Modifier.isPublic(ctor.getModifiers())) {
                    throw new IllegalArgumentException("Default constructor for "+cls.getName()+" is not accessible (non-public?): not allowed to try modify access via Reflection: cannot instantiate type");
                }
            }
            return ctor;
        } catch (NoSuchMethodException e) {
            ;
        } catch (Exception e) {
            ClassUtil.unwrapAndThrowAsIAE(e, "Failed to find default constructor of class "+cls.getName()+", problem: "+e.getMessage());
        }
        return null;
    }

    /*
    /**********************************************************
    /* Class name, description access
    /**********************************************************
     */

    /**
     * @since 2.9
     */
    public static Class<?> classOf(Object inst) {
        if (inst == null) {
            return null;
        }
        return inst.getClass();
    }

    /**
     * @since 2.9
     */
    public static Class<?> rawClass(JavaType t) {
        if (t == null) {
            return null;
        }
        return t.getRawClass();
    }

    /**
     * @since 2.9
     */
    public static <T> T nonNull(T valueOrNull, T defaultValue) {
        return (valueOrNull == null) ? defaultValue : valueOrNull;
    }

    /**
     * @since 2.9
     */
    public static String nullOrToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * @since 2.9
     */
    public static String nonNullString(String str) {
        if (str == null) {
            return "";
        }
        return str;
    }

    /**
     * Returns either quoted value (with double-quotes) -- if argument non-null
     * String -- or String NULL (no quotes) (if null).
     *
     * @since 2.9
     */
    public static String quotedOr(Object str, String forNull) {
        if (str == null) {
            return forNull;
        }
        return String.format("\"%s\"", str);
    }

    /*
    /**********************************************************
    /* Type name, name, desc handling methods
    /**********************************************************
     */

    /**
     * Helper method used to construct appropriate description
     * when passed either type (Class) or an instance; in latter
     * case, class of instance is to be used.
     */
    public static String getClassDescription(Object classOrInstance)
    {
        if (classOrInstance == null) {
            return "unknown";
        }
        Class<?> cls = (classOrInstance instanceof Class<?>) ?
            (Class<?>) classOrInstance : classOrInstance.getClass();
        return nameOf(cls);
    }

    /**
     * Helper method to create and return "backticked" description of given
     * resolved type (or, {@code "null"} if {@code null} passed), similar
     * to return vaue of {@link #getClassDescription(Object)}.
     *
     * @param fullType Fully resolved type or null
     * @return String description of type including generic type parameters, surrounded
     *   by backticks, if type passed; or string "null" if {code null} passed
     *
     * @since 2.10
     */
    public static String getTypeDescription(JavaType fullType)
    {
        if (fullType == null) {
            return "[null]";
        }
        StringBuilder sb = new StringBuilder(80).append('`');
        sb.append(fullType.toCanonical());
        return sb.append('`').toString();
    }

    /**
     * Helper method used to construct appropriate description
     * when passed either type (Class) or an instance; in latter
     * case, class of instance is to be used.
     *
     * @since 2.9
     */
    public static String classNameOf(Object inst) {
        if (inst == null) {
            return "[null]";
        }
        Class<?> raw = (inst instanceof Class<?>) ? (Class<?>) inst : inst.getClass();
        return nameOf(raw);
    }

    /**
     * Returns either `cls.getName()` (if `cls` not null),
     * or "[null]" if `cls` is null.
     *
     * @since 2.9
     */
    public static String nameOf(Class<?> cls) {
        if (cls == null) {
            return "[null]";
        }
        int index = 0;
        while (cls.isArray()) {
            ++index;
            cls = cls.getComponentType();
        }
        String base = cls.isPrimitive() ? cls.getSimpleName() : cls.getName();
        if (index > 0) {
            StringBuilder sb = new StringBuilder(base);
            do {
                sb.append("[]");
            } while (--index > 0);
            base = sb.toString();
        }
        return backticked(base);
    }

    /**
     * Returns either single-quoted (apostrophe) {@code 'named.getName()'} (if {@code named} not null),
     * or "[null]" if {@code named} is null.
     *<p>
     * NOTE: before 2.12 returned "backticked" version instead of single-quoted name; changed
     * to be compatible with most existing quoting usage within databind
     *
     * @since 2.9
     */
    /*public static String nameOf(Named named) {
        if (named == null) {
            return "[null]";
        }
        return apostrophed(named.getName());
    }*/

    /**
     * Returns either single-quoted (apostrophe) {@code 'name'} (if {@code name} not null),
     * or "[null]" if {@code name} is null.
     *
     * @since 2.12
     */
    public static String name(String name) {
        if (name == null) {
            return "[null]";
        }
        return apostrophed(name);
    }

    /**
     * Returns either single-quoted (apostrophe) {@code 'name'} (if {@code name} not null),
     * or "[null]" if {@code name} is null.
     *
     * @since 2.12
     */
    /*public static String name(PropertyName name) {
        if (name == null) {
            return "[null]";
        }
        // 26-Aug-2020, tatu: Should we consider namespace somehow?
        return apostrophed(name.getSimpleName());
    }*/

    /*
    /**********************************************************
    /* Other escaping, description access
    /**********************************************************
     */

    /**
     * Returns either {@code `text`} (backtick-quoted) or {@code [null]}.
     *
     * @since 2.9
     */
    public static String backticked(String text) {
        if (text == null) {
            return "[null]";
        }
        return new StringBuilder(text.length()+2).append('`').append(text).append('`').toString();
    }

    /**
     * Returns either {@code 'text'} (single-quoted) or {@code [null]}.
     *
     * @since 2.9
     */
    public static String apostrophed(String text) {
        if (text == null) {
            return "[null]";
        }
        return new StringBuilder(text.length()+2).append('\'').append(text).append('\'').toString();
    }

    /**
     * Helper method that returns {@link Throwable#getMessage()} for all other exceptions
     * except for (a) {@link JacksonException}, for which {@code getOriginalMessage()} is
     * returned, and (b) {@link InvocationTargetException}, for which the cause's message
     * is returned, if available.
     * Method is used to avoid accidentally including trailing location information twice
     * in message when wrapping exceptions.
     *
     * @since 2.9.7
     */
    /*public static String exceptionMessage(Throwable t) {
        if (t instanceof JacksonException) {
            return ((JacksonException) t).getOriginalMessage();
        }
        if (t instanceof InvocationTargetException && t.getCause() != null) {
            return t.getCause().getMessage();
        }
        return t.getMessage();
    }*/

    /*
    /**********************************************************
    /* Primitive type support
    /**********************************************************
     */

    /**
     * Helper method used to get default value for wrappers used for primitive types
     * (0 for Integer etc)
     */
    public static Object defaultValue(Class<?> cls)
    {
        if (cls == Integer.TYPE) {
            return Integer.valueOf(0);
        }
        if (cls == Long.TYPE) {
            return Long.valueOf(0L);
        }
        if (cls == Boolean.TYPE) {
            return Boolean.FALSE;
        }
        if (cls == Double.TYPE) {
            return Double.valueOf(0.0);
        }
        if (cls == Float.TYPE) {
            return Float.valueOf(0.0f);
        }
        if (cls == Byte.TYPE) {
            return Byte.valueOf((byte) 0);
        }
        if (cls == Short.TYPE) {
            return Short.valueOf((short) 0);
        }
        if (cls == Character.TYPE) {
            return '\0';
        }
        throw new IllegalArgumentException("Class "+cls.getName()+" is not a primitive type");
    }

    /**
     * Helper method for finding wrapper type for given primitive type (why isn't
     * there one in JDK?).
     * NOTE: throws {@link IllegalArgumentException} if given type is NOT primitive
     * type (caller has to check).
     */
    public static Class<?> wrapperType(Class<?> primitiveType)
    {
        if (primitiveType == Integer.TYPE) {
            return Integer.class;
        }
        if (primitiveType == Long.TYPE) {
            return Long.class;
        }
        if (primitiveType == Boolean.TYPE) {
            return Boolean.class;
        }
        if (primitiveType == Double.TYPE) {
            return Double.class;
        }
        if (primitiveType == Float.TYPE) {
            return Float.class;
        }
        if (primitiveType == Byte.TYPE) {
            return Byte.class;
        }
        if (primitiveType == Short.TYPE) {
            return Short.class;
        }
        if (primitiveType == Character.TYPE) {
            return Character.class;
        }
        throw new IllegalArgumentException("Class "+primitiveType.getName()+" is not a primitive type");
    }

    /**
     * Method that can be used to find primitive type for given class if (but only if)
     * it is either wrapper type or primitive type; returns {@code null} if type is neither.
     *
     * @since 2.7
     */
    public static Class<?> primitiveType(Class<?> type)
    {
        if (type.isPrimitive()) {
            return type;
        }

        if (type == Integer.class) {
            return Integer.TYPE;
        }
        if (type == Long.class) {
            return Long.TYPE;
        }
        if (type == Boolean.class) {
            return Boolean.TYPE;
        }
        if (type == Double.class) {
            return Double.TYPE;
        }
        if (type == Float.class) {
            return Float.TYPE;
        }
        if (type == Byte.class) {
            return Byte.TYPE;
        }
        if (type == Short.class) {
            return Short.TYPE;
        }
        if (type == Character.class) {
            return Character.TYPE;
        }
        return null;
    }

    /*
    /**********************************************************
    /* Access checking/handling methods
    /**********************************************************
     */

    /**
     * Equivalent to call:
     *<pre>
     *   checkAndFixAccess(member, false);
     *</pre>
     *
     * @deprecated Since 2.7 call variant that takes boolean flag.
     */
    @Deprecated
    public static void checkAndFixAccess(Member member) {
        checkAndFixAccess(member, false);
    }

    /**
     * Method that is called if a {@link Member} may need forced access,
     * to force a field, method or constructor to be accessible: this
     * is done by calling {@link AccessibleObject#setAccessible(boolean)}.
     *
     * @param member Accessor to call <code>setAccessible()</code> on.
     * @param evenIfAlreadyPublic Whether to always try to make accessor
     *   accessible, even if {@code public} (true),
     *   or only if needed to force by-pass of non-{@code public} access (false)
     *
     * @since 2.7
     */
    public static void checkAndFixAccess(Member member, boolean evenIfAlreadyPublic)
    {
        // We know all members are also accessible objects...
        AccessibleObject ao = (AccessibleObject) member;

        // 14-Jan-2009, tatu: It seems safe and potentially beneficial to
        //   always to make it accessible (latter because it will force
        //   skipping checks we have no use for...), so let's always call it.
        try {
            // 15-Apr-2021, tatu: With JDK 14+ we will be hitting access limitations
            //    esp. wrt JDK types so let's change a bit
            final Class<?> declaringClass = member.getDeclaringClass();
            boolean isPublic = Modifier.isPublic(member.getModifiers())
                    && Modifier.isPublic(declaringClass.getModifiers());
            if (!isPublic || (evenIfAlreadyPublic && !isJDKClass(declaringClass))) {
                ao.setAccessible(true);
            }
        } catch (SecurityException se) {
            // 17-Apr-2009, tatu: This can fail on platforms like
            // Google App Engine); so let's only fail if we really needed it...
            if (!ao.isAccessible()) {
                Class<?> declClass = member.getDeclaringClass();
                throw new IllegalArgumentException("Cannot access "+member+" (from class "+declClass.getName()+"; failed to set access: "+se.getMessage());
            }
            // 14-Apr-2021, tatu: [databind#3118] Java 9/JPMS causes new fails...
            //    But while our baseline is Java 8, must check name
        } catch (RuntimeException se) {
            if ("InaccessibleObjectException".equals(se.getClass().getSimpleName())) {
                throw new IllegalArgumentException(String.format(
"Failed to call `setAccess()` on %s '%s' (of class %s) due to `%s`, problem: %s",
member.getClass().getSimpleName(), member.getName(),
nameOf(member.getDeclaringClass()),
se.getClass().getName(), se.getMessage()),
                        se);
            }
            throw se;
        }
    }

    /*
    /**********************************************************
    /* Enum type detection
    /**********************************************************
     */

    /**
     * Helper method that encapsulates reliable check on whether
     * given raw type "is an Enum", that is, is or extends {@link Enum}.
     *
     * @since 2.10.1
     */
    public static boolean isEnumType(Class<?> rawType) {
        return Enum.class.isAssignableFrom(rawType);
    }

    /**
     * Helper method that can be used to dynamically figure out
     * enumeration type of given {@link EnumSet}, without having
     * access to its declaration.
     * Code is needed to work around design flaw in JDK.
     */
    public static Class<? extends Enum<?>> findEnumType(EnumSet<?> s)
    {
        // First things first: if not empty, easy to determine
        if (!s.isEmpty()) {
            return findEnumType(s.iterator().next());
        }
        // Otherwise need to locate using an internal field
        return EnumTypeLocator.instance.enumTypeFor(s);
    }

    /**
     * Helper method that can be used to dynamically figure out
     * enumeration type of given {@link EnumSet}, without having
     * access to its declaration.
     * Code is needed to work around design flaw in JDK.
     */
    public static Class<? extends Enum<?>> findEnumType(EnumMap<?,?> m)
    {
        if (!m.isEmpty()) {
            return findEnumType(m.keySet().iterator().next());
        }
        // Otherwise need to locate using an internal field
        return EnumTypeLocator.instance.enumTypeFor(m);
    }

    /**
     * Helper method that can be used to dynamically figure out formal
     * enumeration type (class) for given enumeration. This is either
     * class of enum instance (for "simple" enumerations), or its
     * superclass (for enums with instance fields or methods)
     */
    public static Class<? extends Enum<?>> findEnumType(Enum<?> en)
    {
        // enums with "body" are sub-classes of the formal type
        return en.getDeclaringClass();
    }

    /**
     * Helper method that can be used to dynamically figure out formal
     * enumeration type (class) for given class of an enumeration value.
     * This is either class of enum instance (for "simple" enumerations),
     * or its superclass (for enums with instance fields or methods)
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Enum<?>> findEnumType(Class<?> cls)
    {
        // enums with "body" are sub-classes of the formal type
        if (cls.getSuperclass() != Enum.class) {
            cls = cls.getSuperclass();
        }
        return (Class<? extends Enum<?>>) cls;
    }

    /**
     * A method that will look for the first Enum value annotated with the given Annotation.
     * <p>
     * If there's more than one value annotated, the first one found will be returned. Which one exactly is used is undetermined.
     *
     * @param enumClass The Enum class to scan for a value with the given annotation
     * @param annotationClass The annotation to look for.
     * @return the Enum value annotated with the given Annotation or {@code null} if none is found.
     * @throws IllegalArgumentException if there's a reflection issue accessing the Enum
     * @since 2.8
     */
    public static <T extends Annotation> Enum<?> findFirstAnnotatedEnumValue(Class<Enum<?>> enumClass, Class<T> annotationClass)
    {
        Field[] fields = enumClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isEnumConstant()) {
                Annotation defaultValueAnnotation = field.getAnnotation(annotationClass);
                if (defaultValueAnnotation != null) {
                    final String name = field.getName();
                    for (Enum<?> enumValue : enumClass.getEnumConstants()) {
                        if (name.equals(enumValue.name())) {
                            return enumValue;
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
    /**********************************************************************
    /* Methods for detecting special class categories
    /**********************************************************************
     */

    /**
     * Method that can be called to determine if given Object is the default
     * implementation Jackson uses; as opposed to a custom serializer installed by
     * a module or calling application. Determination is done using
     * {@link JacksonStdImpl} annotation on handler (serializer, deserializer etc)
     * class.
     *<p>
     * NOTE: passing `null` is legal, and will result in <code>true</code>
     * being returned.
     */
    /*public static boolean isJacksonStdImpl(Object impl) {
        return (impl == null) || isJacksonStdImpl(impl.getClass());
    }

    public static boolean isJacksonStdImpl(Class<?> implClass) {
        return (implClass.getAnnotation(JacksonStdImpl.class) != null);
    }*/

    /**
     * Accessor for checking whether given {@code Class} is under Java package
     * of {@code java.*} or {@code javax.*} (including all sub-packages).
     *<p>
     * Added since some aspects of handling need to be changed for JDK types (and
     * possibly some extensions under {@code javax.}?): for example, forcing of access
     * will not work well for future JDKs (12 and later).
     *<p>
     * Note: in Jackson 2.11 only returned true for {@code java.*} (and not {@code javax.*});
     * was changed in 2.12.
     *
     * @since 2.11
     */
    public static boolean isJDKClass(Class<?> rawType) {
        final String clsName = rawType.getName();
        return clsName.startsWith("java.") || clsName.startsWith("javax.");
    }

    /**
     * Convenience method for:
     *<pre>
     *   return getJDKMajorVersion() >= 17
     *</pre>
     * that also catches any possible exceptions so it is safe to call
     * from static contexts.
     *
     * @return {@code True} if we can determine that the code is running on
     *    JDK 17 or above; {@code false} otherwise.
     *
     * @since 2.15
     */
    public static boolean isJDK17OrAbove() {
        try {
            return getJDKMajorVersion() >= 17;
        } catch (Throwable t) {
            System.err.println("Failed to determine JDK major version, assuming pre-JDK-17; problem: "+t);
            return false;
        }
    }

    /**
     * @return Major version of JDK we are running on
     *
     * @throws IllegalStateException If JDK version information cannot be determined
     *
     * @since 2.15
     */
    public static int getJDKMajorVersion() {
        String version;

        try {
            version = System.getProperty("java.version");
        } catch (SecurityException e) {
            throw new IllegalStateException("Could not access 'java.version': cannot determine JDK major version");
        }
        if (version.startsWith("1.")) {
            // 25-Nov-2022, tatu: We'll consider JDK 8 to be the baseline since
            //    Jackson 2.15+ only runs on 8 and above
            return 8;
        }
        int dotIndex = version.indexOf(".");
        String cleaned = (dotIndex < 0) ? version : version.substring(0, dotIndex);
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid JDK version String '"+version+"' cannot determine JDK major version");
        }
    }

    /*
    /**********************************************************
    /* Access to various Class definition aspects; possibly
    /* cacheable; and attempts was made in 2.7.0 - 2.7.7; however
    /* unintented retention (~= memory leak) wrt [databind#1363]
    /* resulted in removal of caching
    /**********************************************************
     */

    public static boolean isNonStaticInnerClass(Class<?> cls) {
        return !Modifier.isStatic(cls.getModifiers())
                && (getEnclosingClass(cls) != null);
    }

    /**
     * @since 2.7
     *
     * @deprecated Since 2.12 (just call methods directly or check class name)
     */
    @Deprecated // since 2.12
    public static String getPackageName(Class<?> cls) {
        Package pkg = cls.getPackage();
        return (pkg == null) ? null : pkg.getName();
    }

    /**
     * @since 2.7
     */
    public static boolean hasEnclosingMethod(Class<?> cls) {
        return !isObjectOrPrimitive(cls) && (cls.getEnclosingMethod() != null);
    }

    /**
     * @deprecated since 2.11 (just call Class method directly)
     */
    @Deprecated
    public static Field[] getDeclaredFields(Class<?> cls) {
        return cls.getDeclaredFields();
    }

    /**
     * @deprecated since 2.11 (just call Class method directly)
     */
    @Deprecated
    public static Method[] getDeclaredMethods(Class<?> cls) {
        return cls.getDeclaredMethods();
    }

    /**
     * @since 2.7
     */
    public static Annotation[] findClassAnnotations(Class<?> cls) {
        if (isObjectOrPrimitive(cls)) {
            return NO_ANNOTATIONS;
        }
        return cls.getDeclaredAnnotations();
    }

    /**
     * Helper method that gets methods declared in given class; usually a simple thing,
     * but sometimes (as per [databind#785]) more complicated, depending on classloader
     * setup.
     *
     * @since 2.9
     */
    public static Method[] getClassMethods(Class<?> cls)
    {
        try {
            return cls.getDeclaredMethods();
        } catch (final NoClassDefFoundError ex) {
            // One of the methods had a class that was not found in the cls.getClassLoader.
            // Maybe the developer was nice and has a different class loader for this context.
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null){
                // Nope... this is going to end poorly
                return _failGetClassMethods(cls, ex);
            }
            final Class<?> contextClass;
            try {
                contextClass = loader.loadClass(cls.getName());
            } catch (ClassNotFoundException e) {
                ex.addSuppressed(e);
                return _failGetClassMethods(cls, ex);
            }
            try {
                return contextClass.getDeclaredMethods(); // Cross fingers
            } catch (Throwable t) {
                return _failGetClassMethods(cls, t);
            }
        } catch (Throwable t) {
            return _failGetClassMethods(cls, t);
        }
    }

    // @since 2.11.4 (see [databind#2807])
    private static Method[] _failGetClassMethods(Class<?> cls, Throwable rootCause)
            throws IllegalArgumentException
    {
        throw new IllegalArgumentException(String.format(
"Failed on call to `getDeclaredMethods()` on class `%s`, problem: (%s) %s",
cls.getName(), rootCause.getClass().getName(), rootCause.getMessage()),
                rootCause);
    }

    /**
     * @since 2.7
     */
    public static Ctor[] getConstructors(Class<?> cls) {
        // Note: can NOT skip abstract classes as they may be used with mix-ins
        // and for regular use shouldn't really matter.
        if (cls.isInterface() || isObjectOrPrimitive(cls)) {
            return NO_CTORS;
        }
        Constructor<?>[] rawCtors = cls.getDeclaredConstructors();
        final int len = rawCtors.length;
        Ctor[] result = new Ctor[len];
        for (int i = 0; i < len; ++i) {
            result[i] = new Ctor(rawCtors[i]);
        }
        return result;
    }

    // // // Then methods that do NOT cache access but were considered
    // // // (and could be added to do caching if it was proven effective)

    /**
     * @since 2.7
     */
    public static Class<?> getDeclaringClass(Class<?> cls) {
        return isObjectOrPrimitive(cls) ? null : cls.getDeclaringClass();
    }

    /**
     * @since 2.7
     */
    public static Type getGenericSuperclass(Class<?> cls) {
        return cls.getGenericSuperclass();
    }

    /**
     * @since 2.7
     */
    public static Type[] getGenericInterfaces(Class<?> cls) {
        return cls.getGenericInterfaces();
    }

    /**
     * @since 2.7
     */
    public static Class<?> getEnclosingClass(Class<?> cls) {
        // Caching does not seem worthwhile, as per profiling
        return isObjectOrPrimitive(cls) ? null : cls.getEnclosingClass();
    }

    private static Class<?>[] _interfaces(Class<?> cls) {
        return cls.getInterfaces();
    }

    /*
    /**********************************************************
    /* Helper classes
    /**********************************************************
     */

    /**
     * Inner class used to contain gory details of how we can determine
     * details of instances of common JDK types like {@link EnumMap}s.
     */
    private static class EnumTypeLocator
    {
        final static EnumTypeLocator instance = new EnumTypeLocator();

        private final Field enumSetTypeField;
        private final Field enumMapTypeField;

        private final String failForEnumSet;
        private final String failForEnumMap;

        private EnumTypeLocator() {
            //JDK uses following fields to store information about actual Enumeration
            // type for EnumSets, EnumMaps...

            Field f = null;
            String msg = null;

            try {
                f = locateField(EnumSet.class, "elementType", Class.class);
            } catch (Exception e) {
                msg = e.toString();
            }
            enumSetTypeField = f;
            failForEnumSet = msg;

            f = null;
            msg = null;
            try {
                f = locateField(EnumMap.class, "keyType", Class.class);
            } catch (Exception e) {
                msg  = e.toString();
            }
            enumMapTypeField = f;
            failForEnumMap = msg;
        }

        @SuppressWarnings("unchecked")
        public Class<? extends Enum<?>> enumTypeFor(EnumSet<?> set)
        {
            if (enumSetTypeField != null) {
                return (Class<? extends Enum<?>>) get(set, enumSetTypeField);
            }
            throw new IllegalStateException(
"Cannot figure out type parameter for `EnumSet` (odd JDK platform?), problem: "+failForEnumSet);
        }

        @SuppressWarnings("unchecked")
        public Class<? extends Enum<?>> enumTypeFor(EnumMap<?,?> set)
        {
            if (enumMapTypeField != null) {
                return (Class<? extends Enum<?>>) get(set, enumMapTypeField);
            }
            throw new IllegalStateException(
"Cannot figure out type parameter for `EnumMap` (odd JDK platform?), problem: "+failForEnumMap);
        }

        private Object get(Object bean, Field field)
        {
            try {
                return field.get(bean);
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        private static Field locateField(Class<?> fromClass, String expectedName, Class<?> type)
            throws Exception
        {
    	        // First: let's see if we can find exact match:
            Field[] fields = fromClass.getDeclaredFields();
            for (Field f : fields) {
                if (!expectedName.equals(f.getName()) || f.getType() != type) {
                    continue;
                }
                f.setAccessible(true);
                return f;
            }
            // If not found, indicate with exception
            throw new IllegalStateException(String.format(
"No field named '%s' in class '%s'", expectedName, fromClass.getName()));
        }
    }

    /*
    /**********************************************************
    /* Helper classed used for caching
    /**********************************************************
     */

    /**
     * Value class used for caching Constructor declarations; used because
     * caching done by JDK appears to be somewhat inefficient for some use cases.
     *
     * @since 2.7
     */
    public final static class Ctor
    {
        public final Constructor<?> _ctor;

        private transient Annotation[] _annotations;

        private transient Annotation[][] _paramAnnotations;

        private int _paramCount = -1;

        public Ctor(Constructor<?> ctor) {
            _ctor = ctor;
        }

        public Constructor<?> getConstructor() {
            return _ctor;
        }

        public int getParamCount() {
            int c = _paramCount;
            if (c < 0) {
                c = _ctor.getParameterCount();
                _paramCount = c;
            }
            return c;
        }

        public Class<?> getDeclaringClass() {
            return _ctor.getDeclaringClass();
        }

        public Annotation[] getDeclaredAnnotations() {
            Annotation[] result = _annotations;
            if (result == null) {
                result = _ctor.getDeclaredAnnotations();
                _annotations = result;
            }
            return result;
        }

        public  Annotation[][] getParameterAnnotations() {
            Annotation[][] result = _paramAnnotations;
            if (result == null) {
                result = _ctor.getParameterAnnotations();
                _paramAnnotations = result;
            }
            return result;
        }
    }
}
