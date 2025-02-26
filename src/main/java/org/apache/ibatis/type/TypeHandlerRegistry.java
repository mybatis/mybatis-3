/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.type;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.chrono.JapaneseDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public final class TypeHandlerRegistry {

  private final Map<JdbcType, TypeHandler<?>> jdbcTypeHandlerMap = new EnumMap<>(JdbcType.class);
  private final Map<Type, Map<JdbcType, TypeHandler<?>>> typeHandlerMap = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Type, Constructor<?>> smartHandlers = new ConcurrentHashMap<>();
  private final Map<Class<?>, TypeHandler<?>> allTypeHandlersMap = new HashMap<>();

  private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();

  @SuppressWarnings("rawtypes")
  private Class<? extends TypeHandler> defaultEnumTypeHandler = EnumTypeHandler.class;

  /**
   * The default constructor.
   */
  public TypeHandlerRegistry() {
    this(new Configuration());
  }

  /**
   * The constructor that pass the MyBatis configuration.
   *
   * @param configuration
   *          a MyBatis configuration
   *
   * @since 3.5.4
   */
  public TypeHandlerRegistry(Configuration configuration) {
    // If a handler is registered against null JDBC type, it is the default handler for the Java type. Users can
    // override the default handler (e.g. `register(boolean.class, null, new YNBooleanTypeHandler())` or register a
    // custom handler for a specific Java-JDBC type combination (e.g. `register(boolean.class, JdbcType.CHAR, new
    // YNBooleanTypeHandler())`).
    register(new Type[] { Boolean.class, boolean.class }, new JdbcType[] { null }, BooleanTypeHandler.INSTANCE);
    register(new Type[] { Byte.class, byte.class }, new JdbcType[] { null }, ByteTypeHandler.INSTANCE);
    register(new Type[] { Short.class, short.class }, new JdbcType[] { null }, ShortTypeHandler.INSTANCE);
    register(new Type[] { Integer.class, int.class }, new JdbcType[] { null }, IntegerTypeHandler.INSTANCE);
    register(new Type[] { Long.class, long.class }, new JdbcType[] { null }, LongTypeHandler.INSTANCE);
    register(new Type[] { Float.class, float.class }, new JdbcType[] { null }, FloatTypeHandler.INSTANCE);
    register(new Type[] { Double.class, double.class }, new JdbcType[] { null }, DoubleTypeHandler.INSTANCE);
    register(new Type[] { Character.class, char.class }, new JdbcType[] { null }, new CharacterTypeHandler());
    register(String.class, null, StringTypeHandler.INSTANCE);
    register(Reader.class, null, new ClobReaderTypeHandler());
    register(BigInteger.class, null, new BigIntegerTypeHandler());
    register(BigDecimal.class, null, BigDecimalTypeHandler.INSTANCE);
    register(InputStream.class, null, new BlobInputStreamTypeHandler());
    register(Byte[].class, null, new ByteObjectArrayTypeHandler());
    register(byte[].class, null, ByteArrayTypeHandler.INSTANCE);
    register(Date.class, null, DateTypeHandler.INSTANCE);
    register(java.sql.Date.class, null, new SqlDateTypeHandler());
    register(Time.class, null, new SqlTimeTypeHandler());
    register(Timestamp.class, null, new SqlTimestampTypeHandler());
    register(Instant.class, null, new InstantTypeHandler());
    register(LocalDateTime.class, null, new LocalDateTimeTypeHandler());
    register(LocalDate.class, null, new LocalDateTypeHandler());
    register(LocalTime.class, null, new LocalTimeTypeHandler());
    register(OffsetDateTime.class, null, new OffsetDateTimeTypeHandler());
    register(OffsetTime.class, null, new OffsetTimeTypeHandler());
    register(ZonedDateTime.class, null, new ZonedDateTimeTypeHandler());
    register(Month.class, null, new MonthTypeHandler());
    register(Year.class, null, new YearTypeHandler());
    register(YearMonth.class, null, new YearMonthTypeHandler());
    register(JapaneseDate.class, null, new JapaneseDateTypeHandler());

    // These type handlers are used only for specific combinations of Java type and JDBC type.
    register(String.class, JdbcType.CLOB, ClobTypeHandler.INSTANCE);
    register(String.class, JdbcType.NCLOB, NClobTypeHandler.INSTANCE);
    register(new Type[] { String.class }, new JdbcType[] { JdbcType.NCHAR, JdbcType.NVARCHAR, JdbcType.LONGNVARCHAR },
        NStringTypeHandler.INSTANCE);
    register(new Type[] { Byte[].class }, new JdbcType[] { JdbcType.BLOB, JdbcType.LONGVARBINARY },
        new BlobByteObjectArrayTypeHandler());
    register(new Type[] { byte[].class }, new JdbcType[] { JdbcType.BLOB, JdbcType.LONGVARBINARY },
        BlobTypeHandler.INSTANCE);
    register(Date.class, JdbcType.DATE, DateOnlyTypeHandler.INSTANCE);
    register(Date.class, JdbcType.TIME, TimeOnlyTypeHandler.INSTANCE);
    register(String.class, JdbcType.SQLXML, new SqlxmlTypeHandler());

    // Type handlers in the `jdbcTypeHandlerMap` are used when Java type is unknown or
    // as a last resort when no matching handler is found for the target Java type.
    // It is also used in some internal purposes like creating cache keys.
    // Although it is possible for users to override these mappings via register(JdbcType, TypeHandler),
    // it might have unexpected side-effect.
    // To configure type handlers for mapping to Map, for example, it is recommended to call the 3-args
    // version of register method. e.g. register(Object.class, JdbcType.DATE, new DateTypeHandler())
    jdbcTypeHandlerMap.put(JdbcType.BOOLEAN, BooleanTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.BIT, BooleanTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.TINYINT, ByteTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.SMALLINT, ShortTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.INTEGER, IntegerTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.BIGINT, LongTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.REAL, FloatTypeHandler.INSTANCE); // As per JDBC spec
    jdbcTypeHandlerMap.put(JdbcType.FLOAT, DoubleTypeHandler.INSTANCE); // As per JDBC spec
    jdbcTypeHandlerMap.put(JdbcType.DOUBLE, DoubleTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.DECIMAL, BigDecimalTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.NUMERIC, BigDecimalTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.CHAR, StringTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.VARCHAR, StringTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.LONGVARCHAR, StringTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.CLOB, ClobTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.NVARCHAR, NStringTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.NCHAR, NStringTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.LONGNVARCHAR, NStringTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.NCLOB, NClobTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.ARRAY, new ArrayTypeHandler());
    jdbcTypeHandlerMap.put(JdbcType.BINARY, ByteArrayTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.VARBINARY, ByteArrayTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.LONGVARBINARY, ByteArrayTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.BLOB, BlobTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.TIMESTAMP, DateTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.DATE, DateOnlyTypeHandler.INSTANCE);
    jdbcTypeHandlerMap.put(JdbcType.TIME, TimeOnlyTypeHandler.INSTANCE);
  }

  /**
   * Set a default {@link TypeHandler} class for {@link Enum}. A default {@link TypeHandler} is
   * {@link org.apache.ibatis.type.EnumTypeHandler}.
   *
   * @param typeHandler
   *          a type handler class for {@link Enum}
   *
   * @since 3.4.5
   */
  public void setDefaultEnumTypeHandler(@SuppressWarnings("rawtypes") Class<? extends TypeHandler> typeHandler) {
    this.defaultEnumTypeHandler = typeHandler;
  }

  public boolean hasTypeHandler(Type javaType) {
    return hasTypeHandler(javaType, null);
  }

  @Deprecated(since = "3.6.0", forRemoval = true)
  public boolean hasTypeHandler(TypeReference<?> javaTypeReference) {
    return hasTypeHandler(javaTypeReference, null);
  }

  public boolean hasTypeHandler(Type javaType, JdbcType jdbcType) {
    return javaType != null && getTypeHandler(javaType, jdbcType) != null;
  }

  @Deprecated(since = "3.6.0", forRemoval = true)
  public boolean hasTypeHandler(TypeReference<?> javaTypeReference, JdbcType jdbcType) {
    return javaTypeReference != null && getTypeHandler(javaTypeReference, jdbcType) != null;
  }

  @Deprecated(since = "3.6.0", forRemoval = true)
  public TypeHandler<?> getMappingTypeHandler(Class<? extends TypeHandler<?>> handlerType) {
    return allTypeHandlersMap.get(handlerType);
  }

  public TypeHandler<?> getTypeHandler(Type type) {
    return getTypeHandler(type, null);
  }

  @Deprecated(since = "3.6.0", forRemoval = true)
  public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference) {
    return getTypeHandler(javaTypeReference, null);
  }

  public TypeHandler<?> getTypeHandler(JdbcType jdbcType) {
    return jdbcTypeHandlerMap.get(jdbcType);
  }

  @SuppressWarnings("unchecked")
  @Deprecated(since = "3.6.0", forRemoval = true)
  public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference, JdbcType jdbcType) {
    return (TypeHandler<T>) getTypeHandler(javaTypeReference.getRawType(), jdbcType);
  }

  public TypeHandler<?> getTypeHandler(Type type, JdbcType jdbcType, Class<? extends TypeHandler<?>> typeHandlerClass) {
    TypeHandler<?> typeHandler = getTypeHandler(type, jdbcType);
    if (typeHandler != null && (typeHandlerClass == null || typeHandler.getClass().equals(typeHandlerClass))) {
      return typeHandler;
    }
    if (typeHandlerClass == null) {
      typeHandler = getSmartHandler(type, jdbcType);
    } else {
      typeHandler = getMappingTypeHandler(typeHandlerClass);
      if (typeHandler == null) {
        typeHandler = getInstance(type, typeHandlerClass);
      }
    }
    return typeHandler;
  }

  public TypeHandler<?> getTypeHandler(Type type, JdbcType jdbcType) {
    if (ParamMap.class.equals(type)) {
      return null;
    } else if (type == null) {
      return getTypeHandler(jdbcType);
    }

    TypeHandler<?> handler = null;
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMap(type);

    if (Object.class.equals(type)) {
      if (jdbcHandlerMap != null) {
        handler = jdbcHandlerMap.get(jdbcType);
      }
      return handler;
    }

    if (jdbcHandlerMap != null) {
      handler = jdbcHandlerMap.get(jdbcType);
      if (handler == null) {
        handler = jdbcHandlerMap.get(null);
      }
      if (handler == null) {
        // #591
        handler = pickSoleHandler(jdbcHandlerMap);
      }
    }
    if (handler == null) {
      handler = getSmartHandler(type, jdbcType);
    }
    if (handler == null && type instanceof ParameterizedType) {
      handler = getTypeHandler((Class<?>) ((ParameterizedType) type).getRawType(), jdbcType);
    }
    return handler;
  }

  private TypeHandler<?> getSmartHandler(Type type, JdbcType jdbcType) {
    Constructor<?> candidate = null;

    for (Entry<Type, Constructor<?>> entry : smartHandlers.entrySet()) {
      Type registeredType = entry.getKey();
      if (registeredType.equals(type)) {
        candidate = entry.getValue();
        break;
      }
      if (registeredType instanceof Class) {
        if (type instanceof Class && ((Class<?>) registeredType).isAssignableFrom((Class<?>) type)) {
          candidate = entry.getValue();
        }
      } else if (registeredType instanceof ParameterizedType) {
        Class<?> registeredClass = (Class<?>) ((ParameterizedType) registeredType).getRawType();
        if (type instanceof ParameterizedType) {
          Class<?> clazz = (Class<?>) ((ParameterizedType) type).getRawType();
          if (registeredClass.isAssignableFrom(clazz)) {
            candidate = entry.getValue();
          }
        }
      }
    }

    if (candidate == null) {
      if (type instanceof Class) {
        Class<?> clazz = (Class<?>) type;
        if (Enum.class.isAssignableFrom(clazz)) {
          Class<?> enumClass = clazz.isAnonymousClass() ? clazz.getSuperclass() : clazz;
          TypeHandler<?> enumHandler = getInstance(enumClass, defaultEnumTypeHandler);
          register(new Type[] { enumClass }, new JdbcType[] { jdbcType }, enumHandler);
          return enumHandler;
        }
      }
      return null;
    }

    try {
      TypeHandler<?> typeHandler = (TypeHandler<?>) candidate.newInstance(type);
      register(type, jdbcType, typeHandler);
      return typeHandler;
    } catch (ReflectiveOperationException e) {
      throw new TypeException("Failed to invoke constructor " + candidate.toString(), e);
    }
  }

  private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMap(Type type) {
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(type);
    if (jdbcHandlerMap != null) {
      return NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap) ? null : jdbcHandlerMap;
    }
    if (type instanceof Class) {
      Class<?> clazz = (Class<?>) type;
      if (!Enum.class.isAssignableFrom(clazz)) {
        jdbcHandlerMap = getJdbcHandlerMapForSuperclass(clazz);
      }
    }
    typeHandlerMap.put(type, jdbcHandlerMap == null ? NULL_TYPE_HANDLER_MAP : jdbcHandlerMap);
    return jdbcHandlerMap;
  }

  private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForSuperclass(Class<?> clazz) {
    Class<?> superclass = clazz.getSuperclass();
    if (superclass == null || Object.class.equals(superclass)) {
      return null;
    }
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(superclass);
    if (jdbcHandlerMap != null) {
      return jdbcHandlerMap;
    }
    return getJdbcHandlerMapForSuperclass(superclass);
  }

  private TypeHandler<?> pickSoleHandler(Map<JdbcType, TypeHandler<?>> jdbcHandlerMap) {
    TypeHandler<?> soleHandler = null;
    for (TypeHandler<?> handler : jdbcHandlerMap.values()) {
      if (soleHandler == null) {
        soleHandler = handler;
      } else if (!handler.getClass().equals(soleHandler.getClass())) {
        // More than one type handlers registered.
        return null;
      }
    }
    return soleHandler;
  }

  public void register(JdbcType mappedJdbcType, TypeHandler<?> handler) {
    jdbcTypeHandlerMap.put(mappedJdbcType, handler);
  }

  //
  // REGISTER INSTANCE
  //

  // Only handler

  public <T> void register(TypeHandler<T> handler) {
    register(mappedJavaTypes(handler.getClass()), mappedJdbcTypes(handler.getClass()), handler);
  }

  // java type + handler

  public void register(Class<?> mappedJavaType, TypeHandler<?> handler) {
    register((Type) mappedJavaType, handler);
  }

  private void register(Type mappedJavaType, TypeHandler<?> handler) {
    register(new Type[] { mappedJavaType }, mappedJdbcTypes(handler.getClass()), handler);
  }

  @Deprecated(since = "3.6.0", forRemoval = true)
  public <T> void register(TypeReference<T> javaTypeReference, TypeHandler<? extends T> handler) {
    register(javaTypeReference.getRawType(), handler);
  }

  // java type + jdbc type + handler

  public void register(Type mappedJavaType, JdbcType mappedJdbcType, TypeHandler<?> handler) {
    register(new Type[] { mappedJavaType }, new JdbcType[] { mappedJdbcType }, handler);
  }

  private void register(Type[] mappedJavaTypes, JdbcType[] mappedJdbcTypes, TypeHandler<?> handler) {
    for (Type javaType : mappedJavaTypes) {
      if (javaType == null) {
        continue;
      }

      typeHandlerMap.compute(javaType, (k, v) -> {
        Map<JdbcType, TypeHandler<?>> map = (v == null || v == NULL_TYPE_HANDLER_MAP ? new HashMap<>() : v);
        for (JdbcType jdbcType : mappedJdbcTypes) {
          map.put(jdbcType, handler);
        }
        return map;
      });

      if (javaType instanceof ParameterizedType) {
        // MEMO: add annotation to skip this?
        Type rawType = ((ParameterizedType) javaType).getRawType();
        typeHandlerMap.compute(rawType, (k, v) -> {
          Map<JdbcType, TypeHandler<?>> map = (v == null || v == NULL_TYPE_HANDLER_MAP ? new HashMap<>() : v);
          for (JdbcType jdbcType : mappedJdbcTypes) {
            map.merge(jdbcType, handler, (handler1, handler2) -> handler1.equals(handler2) ? handler1
                : new ConflictedTypeHandler((Class<?>) rawType, jdbcType, handler1, handler2));
          }
          return map;
        });
      }
    }

    allTypeHandlersMap.put(handler.getClass(), handler);
  }

  //
  // REGISTER CLASS
  //

  // Only handler type

  public void register(Class<?> handlerClass) {
    register(mappedJavaTypes(handlerClass), mappedJdbcTypes(handlerClass), handlerClass);
  }

  // java type + handler type

  @Deprecated(since = "3.6.0", forRemoval = true)
  public void register(String javaTypeClassName, String typeHandlerClassName) throws ClassNotFoundException {
    register(Resources.classForName(javaTypeClassName), Resources.classForName(typeHandlerClassName));
  }

  public void register(Type mappedJavaType, Class<?> handlerClass) {
    register(new Type[] { mappedJavaType }, mappedJdbcTypes(handlerClass), handlerClass);
  }

  // java type + jdbc type + handler type

  public void register(Type mappedJavaType, JdbcType mappedJdbcType, Class<?> handlerClass) {
    register(new Type[] { mappedJavaType }, new JdbcType[] { mappedJdbcType }, handlerClass);
  }

  private void register(Type[] mappedJavaTypes, JdbcType[] mappedJdbcTypes, Class<?> handlerClass) {
    if (!TypeHandler.class.isAssignableFrom(handlerClass)) {
      throw new IllegalArgumentException(String.format("'%s' does not implement TypeHandler.", handlerClass.getName()));
    }
    for (Constructor<?> constructor : handlerClass.getConstructors()) {
      if (constructor.getParameterCount() != 1) {
        continue;
      }
      Class<?> argType = constructor.getParameterTypes()[0];
      if (Type.class.equals(argType) || Class.class.equals(argType)) {
        for (Type javaType : mappedJavaTypes) {
          smartHandlers.computeIfAbsent(javaType, k -> constructor);
        }
        return;
      }
    }
    // It is not a smart handler
    register(mappedJavaTypes, mappedJdbcTypes, getInstance(null, handlerClass));
  }

  private Type[] mappedJavaTypes(Class<?> clazz) {
    MappedTypes mappedTypesAnno = clazz.getAnnotation(MappedTypes.class);
    if (mappedTypesAnno != null) {
      return mappedTypesAnno.value();
    }
    return TypeParameterResolver.resolveClassTypeParams(TypeHandler.class, clazz);
  }

  private JdbcType[] mappedJdbcTypes(Class<?> clazz) {
    MappedJdbcTypes mappedJdbcTypesAnno = clazz.getAnnotation(MappedJdbcTypes.class);
    if (mappedJdbcTypesAnno != null) {
      JdbcType[] jdbcTypes = mappedJdbcTypesAnno.value();
      if (mappedJdbcTypesAnno.includeNullJdbcType()) {
        int newLength = jdbcTypes.length + 1;
        jdbcTypes = Arrays.copyOf(jdbcTypes, newLength);
        jdbcTypes[newLength - 1] = null;
      }
      return jdbcTypes;
    }
    return new JdbcType[] { null };
  }

  // Construct a handler (used also from Builders)

  @SuppressWarnings("unchecked")
  public <T> TypeHandler<T> getInstance(Type javaType, Class<?> handlerClass) {
    Constructor<?> c;
    try {
      if (javaType != null) {
        try {
          c = handlerClass.getConstructor(Type.class);
          return (TypeHandler<T>) c.newInstance(javaType);
        } catch (NoSuchMethodException ignored) {
        }
        if (javaType instanceof Class) {
          try {
            c = handlerClass.getConstructor(Class.class);
            return (TypeHandler<T>) c.newInstance(javaType);
          } catch (NoSuchMethodException ignored) {
          }
        }
      }
      try {
        c = handlerClass.getConstructor();
        return (TypeHandler<T>) c.newInstance();
      } catch (NoSuchMethodException e) {
        throw new TypeException("Unable to find a usable constructor for " + handlerClass, e);
      }
    } catch (ReflectiveOperationException e) {
      throw new TypeException("Failed to invoke constructor for handler " + handlerClass, e);
    }
  }

  // scan

  public void register(String packageName) {
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(TypeHandler.class), packageName);
    Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
    for (Class<?> type : handlerSet) {
      // Ignore inner classes and interfaces (including package-info.java) and abstract classes
      if (!type.isAnonymousClass() && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
        register(type);
      }
    }
  }

  // get information

  /**
   * Gets the type handlers. Used by mybatis-guice.
   *
   * @return the type handlers
   *
   * @since 3.2.2
   */
  public Collection<TypeHandler<?>> getTypeHandlers() {
    return Collections.unmodifiableCollection(allTypeHandlersMap.values());
  }

}
