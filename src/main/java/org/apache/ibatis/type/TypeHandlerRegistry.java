/*
 *    Copyright 2009-2022 the original author or authors.
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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.util.MapUtil;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public final class TypeHandlerRegistry {

  private final Map<JdbcType, TypeHandler<?>> jdbcTypeHandlerMap = new EnumMap<>(JdbcType.class);
  private final Map<Type, Map<JdbcType, TypeHandler<?>>> typeHandlerMap = new ConcurrentHashMap<>();
  private final Map<Type, Entry<Constructor<?>, Set<JdbcType>>> smartHandlers = new ConcurrentHashMap<>();
  private final TypeHandler<Object> unknownTypeHandler;
  @Deprecated
  private final Map<Class<?>, TypeHandler<?>> allTypeHandlersMap = new HashMap<>();

  private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();

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
   * @param configuration a MyBatis configuration
   * @since 3.5.4
   */
  public TypeHandlerRegistry(Configuration configuration) {
    this.unknownTypeHandler = new UnknownTypeHandler(configuration);

    register(Boolean.class, new BooleanTypeHandler());
    register(boolean.class, new BooleanTypeHandler());
    register(JdbcType.BOOLEAN, new BooleanTypeHandler());
    register(JdbcType.BIT, new BooleanTypeHandler());

    register(Byte.class, new ByteTypeHandler());
    register(byte.class, new ByteTypeHandler());
    register(JdbcType.TINYINT, new ByteTypeHandler());

    register(Short.class, new ShortTypeHandler());
    register(short.class, new ShortTypeHandler());
    register(JdbcType.SMALLINT, new ShortTypeHandler());

    register(Integer.class, new IntegerTypeHandler());
    register(int.class, new IntegerTypeHandler());
    register(JdbcType.INTEGER, new IntegerTypeHandler());

    register(Long.class, new LongTypeHandler());
    register(long.class, new LongTypeHandler());

    register(Float.class, new FloatTypeHandler());
    register(float.class, new FloatTypeHandler());
    register(JdbcType.FLOAT, new FloatTypeHandler());

    register(Double.class, new DoubleTypeHandler());
    register(double.class, new DoubleTypeHandler());
    register(JdbcType.DOUBLE, new DoubleTypeHandler());

    register(Reader.class, new ClobReaderTypeHandler());
    register(String.class, new StringTypeHandler());
    register(String.class, JdbcType.CHAR, new StringTypeHandler());
    register(String.class, JdbcType.CLOB, new ClobTypeHandler());
    register(String.class, JdbcType.VARCHAR, new StringTypeHandler());
    register(String.class, JdbcType.LONGVARCHAR, new StringTypeHandler());
    register(String.class, JdbcType.NVARCHAR, new NStringTypeHandler());
    register(String.class, JdbcType.NCHAR, new NStringTypeHandler());
    register(String.class, JdbcType.NCLOB, new NClobTypeHandler());
    register(JdbcType.CHAR, new StringTypeHandler());
    register(JdbcType.VARCHAR, new StringTypeHandler());
    register(JdbcType.CLOB, new ClobTypeHandler());
    register(JdbcType.LONGVARCHAR, new StringTypeHandler());
    register(JdbcType.NVARCHAR, new NStringTypeHandler());
    register(JdbcType.NCHAR, new NStringTypeHandler());
    register(JdbcType.NCLOB, new NClobTypeHandler());

    register(Object.class, JdbcType.ARRAY, new ArrayTypeHandler());
    register(JdbcType.ARRAY, new ArrayTypeHandler());

    register(BigInteger.class, new BigIntegerTypeHandler());
    register(JdbcType.BIGINT, new LongTypeHandler());

    register(BigDecimal.class, new BigDecimalTypeHandler());
    register(JdbcType.REAL, new BigDecimalTypeHandler());
    register(JdbcType.DECIMAL, new BigDecimalTypeHandler());
    register(JdbcType.NUMERIC, new BigDecimalTypeHandler());

    register(InputStream.class, new BlobInputStreamTypeHandler());
    register(Byte[].class, new ByteObjectArrayTypeHandler());
    register(Byte[].class, JdbcType.BLOB, new BlobByteObjectArrayTypeHandler());
    register(Byte[].class, JdbcType.LONGVARBINARY, new BlobByteObjectArrayTypeHandler());
    register(byte[].class, new ByteArrayTypeHandler());
    register(byte[].class, JdbcType.BLOB, new BlobTypeHandler());
    register(byte[].class, JdbcType.LONGVARBINARY, new BlobTypeHandler());
    register(JdbcType.LONGVARBINARY, new BlobTypeHandler());
    register(JdbcType.BLOB, new BlobTypeHandler());

    register(Object.class, unknownTypeHandler);
    register(Object.class, JdbcType.OTHER, unknownTypeHandler);
    register(JdbcType.OTHER, unknownTypeHandler);

    register(Date.class, new DateTypeHandler());
    register(Date.class, JdbcType.DATE, new DateOnlyTypeHandler());
    register(Date.class, JdbcType.TIME, new TimeOnlyTypeHandler());
    register(JdbcType.TIMESTAMP, new DateTypeHandler());
    register(JdbcType.DATE, new DateOnlyTypeHandler());
    register(JdbcType.TIME, new TimeOnlyTypeHandler());

    register(java.sql.Date.class, new SqlDateTypeHandler());
    register(java.sql.Time.class, new SqlTimeTypeHandler());
    register(java.sql.Timestamp.class, new SqlTimestampTypeHandler());

    register(String.class, JdbcType.SQLXML, new SqlxmlTypeHandler());

    register(Instant.class, new InstantTypeHandler());
    register(LocalDateTime.class, new LocalDateTimeTypeHandler());
    register(LocalDate.class, new LocalDateTypeHandler());
    register(LocalTime.class, new LocalTimeTypeHandler());
    register(OffsetDateTime.class, new OffsetDateTimeTypeHandler());
    register(OffsetTime.class, new OffsetTimeTypeHandler());
    register(ZonedDateTime.class, new ZonedDateTimeTypeHandler());
    register(Month.class, new MonthTypeHandler());
    register(Year.class, new YearTypeHandler());
    register(YearMonth.class, new YearMonthTypeHandler());
    register(JapaneseDate.class, new JapaneseDateTypeHandler());

    // issue #273
    register(Character.class, new CharacterTypeHandler());
    register(char.class, new CharacterTypeHandler());
  }

  /**
   * Set a default {@link TypeHandler} class for {@link Enum}.
   * A default {@link TypeHandler} is {@link org.apache.ibatis.type.EnumTypeHandler}.
   * @param typeHandler a type handler class for {@link Enum}
   * @since 3.4.5
   */
  public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
    this.defaultEnumTypeHandler = typeHandler;
  }

  public boolean hasTypeHandler(Class<?> javaType) {
    return hasTypeHandler(javaType, null);
  }

  @Deprecated
  public boolean hasTypeHandler(TypeReference<?> javaTypeReference) {
    return hasTypeHandler(javaTypeReference, null);
  }

  public boolean hasTypeHandler(Type javaType, JdbcType jdbcType) {
    return javaType != null && getTypeHandler(javaType, jdbcType) != null;
  }

  @Deprecated
  public boolean hasTypeHandler(TypeReference<?> javaTypeReference, JdbcType jdbcType) {
    return javaTypeReference != null && getTypeHandler(javaTypeReference, jdbcType) != null;
  }

  public TypeHandler<?> getMappingTypeHandler(Class<? extends TypeHandler<?>> handlerType) {
    return allTypeHandlersMap.get(handlerType);
  }

  @Deprecated
  @SuppressWarnings("unchecked")
  public <T> TypeHandler<T> getTypeHandler(Class<T> clazz) {
    return (TypeHandler<T>) getTypeHandler((Type)clazz);
  }

  public TypeHandler<?> getTypeHandler(Type type) {
    return getTypeHandler(type, null);
  }

  @Deprecated
  public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference) {
    return getTypeHandler(javaTypeReference, null);
  }

  public TypeHandler<?> getTypeHandler(JdbcType jdbcType) {
    return jdbcTypeHandlerMap.get(jdbcType);
  }

  @Deprecated
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
    }
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMap(type);
    TypeHandler<?> handler = null;
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
      return getTypeHandler((Class<?>) ((ParameterizedType) type).getRawType(), jdbcType);
    }
    // type drives generics here
    return handler;
  }

  private TypeHandler<?> getSmartHandler(Type type, JdbcType jdbcType) {
    Entry<Constructor<?>, Set<JdbcType>> candidate = null;
    for (Entry<Type, Entry<Constructor<?>, Set<JdbcType>>> entry : smartHandlers.entrySet()) {
      Type registeredType = entry.getKey();
      if (registeredType.equals(type)) {
        candidate = entry.getValue();
        break;
      }
      if (registeredType instanceof Class) {
        if (type instanceof Class && ((Class<?>)registeredType).isAssignableFrom((Class<?>)type)) {
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
          Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(enumClass, enumClass);
          if (jdbcHandlerMap == null) {
            TypeHandler<?> enumHandler = getInstance(enumClass, defaultEnumTypeHandler);
            register(enumClass, enumHandler);
            return enumHandler;
          }
        }
      }
      return null;
    }
    try {
      TypeHandler<?> typeHandler = (TypeHandler<?>) candidate.getKey().newInstance(type);
      register(type, jdbcType, typeHandler);
      return typeHandler;
    } catch (ReflectiveOperationException e) {
      throw new TypeException("Failed to invoke constructor " + candidate.getKey().toString(), e);
    }
  }

  private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMap(Type type) {
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(type);
    if (jdbcHandlerMap != null) {
      return NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap) ? null : jdbcHandlerMap;
    }
    if (type instanceof Class) {
      Class<?> clazz = (Class<?>) type;
      if (Enum.class.isAssignableFrom(clazz)) {
      } else {
        jdbcHandlerMap = getJdbcHandlerMapForSuperclass(clazz);
      }
    }
    typeHandlerMap.put(type, jdbcHandlerMap == null ? NULL_TYPE_HANDLER_MAP : jdbcHandlerMap);
    return jdbcHandlerMap;
  }

  private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForEnumInterfaces(Class<?> clazz, Class<?> enumClazz) {
    for (Class<?> iface : clazz.getInterfaces()) {
      Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(iface);
      if (jdbcHandlerMap == null) {
        jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(iface, enumClazz);
      }
      if (jdbcHandlerMap != null) {
        // Found a type handler registered to a super interface
        HashMap<JdbcType, TypeHandler<?>> newMap = new HashMap<>();
        for (Entry<JdbcType, TypeHandler<?>> entry : jdbcHandlerMap.entrySet()) {
          // Create a type handler instance with enum type as a constructor arg
          newMap.put(entry.getKey(), getInstance(enumClazz, entry.getValue().getClass()));
        }
        return newMap;
      }
    }
    return null;
  }

  private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMapForSuperclass(Class<?> clazz) {
    Class<?> superclass = clazz.getSuperclass();
    if (superclass == null || Object.class.equals(superclass)) {
      return null;
    }
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(superclass);
    if (jdbcHandlerMap != null) {
      return jdbcHandlerMap;
    } else {
      return getJdbcHandlerMapForSuperclass(superclass);
    }
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

  public TypeHandler<Object> getUnknownTypeHandler() {
    return unknownTypeHandler;
  }

  public void register(JdbcType jdbcType, TypeHandler<?> handler) {
    jdbcTypeHandlerMap.put(jdbcType, handler);
  }

  //
  // REGISTER INSTANCE
  //

  // Only handler

  public <T> void register(TypeHandler<T> typeHandler) {
    boolean mappedTypeFound = false;
    MappedTypes mappedTypes = typeHandler.getClass().getAnnotation(MappedTypes.class);
    if (mappedTypes != null) {
      for (Class<?> handledType : mappedTypes.value()) {
        register(handledType, typeHandler);
        mappedTypeFound = true;
      }
    }
    if (!mappedTypeFound) {
      Type detectedMappedType = TypeParameterResolver.resolveClassTypeParams(TypeHandler.class, typeHandler.getClass())[0];
      register(detectedMappedType, typeHandler);
    }
    if (!mappedTypeFound) {
      register((Class<T>) null, typeHandler);
    }
  }

  // java type + handler

  public void register(Class<?> javaType, TypeHandler<?> typeHandler) {
    register((Type) javaType, typeHandler);
  }

  private void register(Type javaType, TypeHandler<?> typeHandler) {
    MappedJdbcTypes mappedJdbcTypes = typeHandler.getClass().getAnnotation(MappedJdbcTypes.class);
    if (mappedJdbcTypes != null) {
      for (JdbcType handledJdbcType : mappedJdbcTypes.value()) {
        register(javaType, handledJdbcType, typeHandler);
      }
      if (mappedJdbcTypes.includeNullJdbcType()) {
        register(javaType, null, typeHandler);
      }
    } else {
      register(javaType, null, typeHandler);
    }
  }

  @Deprecated
  public <T> void register(TypeReference<T> javaTypeReference, TypeHandler<? extends T> handler) {
    register(javaTypeReference.getRawType(), handler);
  }

  // java type + jdbc type + handler

  public void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
    if (javaType != null) {
      typeHandlerMap.compute(javaType, (k, v) -> {
        Map<JdbcType, TypeHandler<?>> map = (v == null || v == NULL_TYPE_HANDLER_MAP ? new HashMap<>()
            : v);
        map.put(jdbcType, handler);
        return map;
      });
      if (javaType instanceof ParameterizedType) {
        // MEMO: add annotation to skip this?
        Type rawType = ((ParameterizedType) javaType).getRawType();
        typeHandlerMap.compute(rawType, (k, v) -> {
          Map<JdbcType, TypeHandler<?>> map = (v == null || v == NULL_TYPE_HANDLER_MAP ? new HashMap<>() : v);
          map.merge(jdbcType, handler, (handler1, handler2) -> handler1.equals(handler2) ? handler1
              : new ConflictedTypeHandler((Class<?>) rawType, jdbcType, handler1, handler2));
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

  public void register(Class<?> typeHandlerClass) {
    MappedTypes mappedTypes = typeHandlerClass.getAnnotation(MappedTypes.class);
    if (mappedTypes == null) {
      Type typeFromSignature = TypeParameterResolver.resolveClassTypeParams(TypeHandler.class, typeHandlerClass)[0];
      register(typeFromSignature, typeHandlerClass);
    } else {
      for (Class<?> javaTypeClass : mappedTypes.value()) {
        register(javaTypeClass, typeHandlerClass);
      }
    }
  }

  // java type + handler type

  public void register(String javaTypeClassName, String typeHandlerClassName) throws ClassNotFoundException {
    register(Resources.classForName(javaTypeClassName), Resources.classForName(typeHandlerClassName));
  }

  public void register(Type javaTypeClass, Class<?> typeHandlerClass) {
    // TODO: change the argument type to Class<? extends TypeHandler<?>> ?
    if (!TypeHandler.class.isAssignableFrom(typeHandlerClass)) {
      throw new IllegalArgumentException(
          String.format("'%s' does not implement TypeHandler.", typeHandlerClass.getName()));
    }
    @SuppressWarnings("unchecked")
    Class<? extends TypeHandler<?>> clazz = (Class<? extends TypeHandler<?>>) typeHandlerClass;
    MappedJdbcTypes mappedJdbcTypes = typeHandlerClass.getAnnotation(MappedJdbcTypes.class);
    if (mappedJdbcTypes != null) {
      for (JdbcType handledJdbcType : mappedJdbcTypes.value()) {
        register(javaTypeClass, handledJdbcType, clazz);
      }
      if (mappedJdbcTypes.includeNullJdbcType()) {
        register(javaTypeClass, null, clazz);
      }
    } else {
      register(javaTypeClass, null, clazz);
    }
  }

  // java type + jdbc type + handler type

  public void register(Type javaTypeClass, JdbcType jdbcType, Class<?> typeHandlerClass) {
    @SuppressWarnings("unchecked")
    Class<? extends TypeHandler<?>> clazz = (Class<? extends TypeHandler<?>>) typeHandlerClass;
    for (Constructor<?> constructor : typeHandlerClass.getConstructors()) {
      if (constructor.getParameterCount() != 1) {
        continue;
      }
      Class<?> argType = constructor.getParameterTypes()[0];
      if (Type.class.equals(argType) || Class.class.equals(argType)) {
        MapUtil.computeIfAbsent(smartHandlers, javaTypeClass, k -> {
          for (Entry<Constructor<?>, Set<JdbcType>> entry : smartHandlers.values()) {
            if (entry.getKey().equals(constructor)) {
              return entry;
            }
          }
          // Might have to Collections.synchronizedSet(new HashSet<>())
          return MapUtil.entry(constructor, new HashSet<>());
        }).getValue().add(jdbcType);
        return;
      }
    }
    register(javaTypeClass, jdbcType, getInstance(javaTypeClass, typeHandlerClass));
  }

  // Construct a handler (used also from Builders)

  @SuppressWarnings("unchecked")
  public <T> TypeHandler<T> getInstance(Type javaTypeClass, Class<?> typeHandlerClass) {
    Constructor<?> c;
    try {
      if (javaTypeClass != null) {
        try {
          c = typeHandlerClass.getConstructor(Type.class);
          return (TypeHandler<T>) c.newInstance(javaTypeClass);
        } catch (NoSuchMethodException ignored) {
        }
        if (javaTypeClass instanceof Class) {
          try {
            c = typeHandlerClass.getConstructor(Class.class);
            return (TypeHandler<T>) c.newInstance(javaTypeClass);
          } catch (NoSuchMethodException ignored) {
          }
        }
      }
      try {
        c = typeHandlerClass.getConstructor();
        return (TypeHandler<T>) c.newInstance();
      } catch (NoSuchMethodException e) {
        throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
      }
    } catch (ReflectiveOperationException e) {
      throw new TypeException("Failed to invoke constructor for handler " + typeHandlerClass, e);
    }
  }

  // scan

  public void register(String packageName) {
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    resolverUtil.find(new ResolverUtil.IsA(TypeHandler.class), packageName);
    Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
    for (Class<?> type : handlerSet) {
      //Ignore inner classes and interfaces (including package-info.java) and abstract classes
      if (!type.isAnonymousClass() && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
        register(type);
      }
    }
  }

  // get information

  /**
   * Gets the type handlers.
   *
   * @return the type handlers
   * @since 3.2.2
   */
  public Collection<TypeHandler<?>> getTypeHandlers() {
    return Collections.unmodifiableCollection(allTypeHandlersMap.values());
  }

}
