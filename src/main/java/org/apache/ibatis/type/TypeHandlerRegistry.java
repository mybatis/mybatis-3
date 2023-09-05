/*
 *    Copyright 2009-2023 the original author or authors.
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;

/**
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public final class TypeHandlerRegistry {

  private final Map<JdbcType, TypeHandler<?>> jdbcTypeHandlerMap = new EnumMap<>(JdbcType.class);
  private final Map<Type, Map<JdbcType, TypeHandler<?>>> typeHandlerMap = new ConcurrentHashMap<>();
  private final TypeHandler<Object> unknownTypeHandler;
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
   * @param configuration
   *          a MyBatis configuration
   *
   * @since 3.5.4
   */
  public TypeHandlerRegistry(Configuration configuration) {
    this.unknownTypeHandler = new UnknownTypeHandler(configuration);

    BooleanTypeHandler booleanTypeHandler = new BooleanTypeHandler();
    register(Boolean.class, booleanTypeHandler);
    register(boolean.class, booleanTypeHandler);
    register(JdbcType.BOOLEAN, booleanTypeHandler);
    register(JdbcType.BIT, booleanTypeHandler);

    ByteTypeHandler byteTypeHandler = new ByteTypeHandler();
    register(Byte.class, byteTypeHandler);
    register(byte.class, byteTypeHandler);
    register(JdbcType.TINYINT, byteTypeHandler);

    ShortTypeHandler shortTypeHandler = new ShortTypeHandler();
    register(Short.class, shortTypeHandler);
    register(short.class, shortTypeHandler);
    register(JdbcType.SMALLINT, shortTypeHandler);

    IntegerTypeHandler integerTypeHandler = new IntegerTypeHandler();
    register(Integer.class, integerTypeHandler);
    register(int.class, integerTypeHandler);
    register(JdbcType.INTEGER, integerTypeHandler);

    LongTypeHandler longTypeHandler = new LongTypeHandler();
    register(Long.class, longTypeHandler);
    register(long.class, longTypeHandler);

    FloatTypeHandler floatTypeHandler = new FloatTypeHandler();
    register(Float.class, floatTypeHandler);
    register(float.class, floatTypeHandler);
    register(JdbcType.FLOAT, floatTypeHandler);

    DoubleTypeHandler doubleTypeHandler = new DoubleTypeHandler();
    register(Double.class, doubleTypeHandler);
    register(double.class, doubleTypeHandler);
    register(JdbcType.DOUBLE, doubleTypeHandler);

    register(Reader.class, new ClobReaderTypeHandler());
    StringTypeHandler stringTypeHandler = new StringTypeHandler();
    register(String.class, stringTypeHandler);
    register(String.class, JdbcType.CHAR, stringTypeHandler);
    ClobTypeHandler clobTypeHandler = new ClobTypeHandler();
    register(String.class, JdbcType.CLOB, clobTypeHandler);
    register(String.class, JdbcType.VARCHAR, stringTypeHandler);
    register(String.class, JdbcType.LONGVARCHAR, stringTypeHandler);
    NStringTypeHandler nStringTypeHandler = new NStringTypeHandler();
    register(String.class, JdbcType.NVARCHAR, nStringTypeHandler);
    register(String.class, JdbcType.NCHAR, nStringTypeHandler);
    NClobTypeHandler nClobTypeHandler = new NClobTypeHandler();
    register(String.class, JdbcType.NCLOB, nClobTypeHandler);
    register(JdbcType.CHAR, stringTypeHandler);
    register(JdbcType.VARCHAR, stringTypeHandler);
    register(JdbcType.CLOB, clobTypeHandler);
    register(JdbcType.LONGVARCHAR, stringTypeHandler);
    register(JdbcType.NVARCHAR, nStringTypeHandler);
    register(JdbcType.NCHAR, nStringTypeHandler);
    register(JdbcType.NCLOB, nClobTypeHandler);

    ArrayTypeHandler arrayTypeHandler = new ArrayTypeHandler();
    register(Object.class, JdbcType.ARRAY, arrayTypeHandler);
    register(JdbcType.ARRAY, arrayTypeHandler);

    register(BigInteger.class, new BigIntegerTypeHandler());
    register(JdbcType.BIGINT, longTypeHandler);

    BigDecimalTypeHandler bigDecimalTypeHandler = new BigDecimalTypeHandler();
    register(BigDecimal.class, bigDecimalTypeHandler);
    register(JdbcType.REAL, bigDecimalTypeHandler);
    register(JdbcType.DECIMAL, bigDecimalTypeHandler);
    register(JdbcType.NUMERIC, bigDecimalTypeHandler);

    register(InputStream.class, new BlobInputStreamTypeHandler());
    register(Byte[].class, new ByteObjectArrayTypeHandler());
    BlobByteObjectArrayTypeHandler blobByteObjectArrayTypeHandler = new BlobByteObjectArrayTypeHandler();
    register(Byte[].class, JdbcType.BLOB, blobByteObjectArrayTypeHandler);
    register(Byte[].class, JdbcType.LONGVARBINARY, blobByteObjectArrayTypeHandler);
    register(byte[].class, new ByteArrayTypeHandler());
    BlobTypeHandler blobTypeHandler = new BlobTypeHandler();
    register(byte[].class, JdbcType.BLOB, blobTypeHandler);
    register(byte[].class, JdbcType.LONGVARBINARY, blobTypeHandler);
    register(JdbcType.LONGVARBINARY, blobTypeHandler);
    register(JdbcType.BLOB, blobTypeHandler);

    register(Object.class, unknownTypeHandler);
    register(Object.class, JdbcType.OTHER, unknownTypeHandler);
    register(JdbcType.OTHER, unknownTypeHandler);

    DateTypeHandler dateTypeHandler = new DateTypeHandler();
    register(Date.class, dateTypeHandler);
    DateOnlyTypeHandler dateOnlyTypeHandler = new DateOnlyTypeHandler();
    register(Date.class, JdbcType.DATE, dateOnlyTypeHandler);
    TimeOnlyTypeHandler timeOnlyTypeHandler = new TimeOnlyTypeHandler();
    register(Date.class, JdbcType.TIME, timeOnlyTypeHandler);
    register(JdbcType.TIMESTAMP, dateTypeHandler);
    register(JdbcType.DATE, dateOnlyTypeHandler);
    register(JdbcType.TIME, timeOnlyTypeHandler);

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
    CharacterTypeHandler characterTypeHandler = new CharacterTypeHandler();
    register(Character.class, characterTypeHandler);
    register(char.class, characterTypeHandler);
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
  public void setDefaultEnumTypeHandler(Class<? extends TypeHandler> typeHandler) {
    this.defaultEnumTypeHandler = typeHandler;
  }

  public boolean hasTypeHandler(Class<?> javaType) {
    return hasTypeHandler(javaType, null);
  }

  public boolean hasTypeHandler(TypeReference<?> javaTypeReference) {
    return hasTypeHandler(javaTypeReference, null);
  }

  public boolean hasTypeHandler(Class<?> javaType, JdbcType jdbcType) {
    return javaType != null && getTypeHandler((Type) javaType, jdbcType) != null;
  }

  public boolean hasTypeHandler(TypeReference<?> javaTypeReference, JdbcType jdbcType) {
    return javaTypeReference != null && getTypeHandler(javaTypeReference, jdbcType) != null;
  }

  public TypeHandler<?> getMappingTypeHandler(Class<? extends TypeHandler<?>> handlerType) {
    return allTypeHandlersMap.get(handlerType);
  }

  public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
    return getTypeHandler((Type) type, null);
  }

  public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference) {
    return getTypeHandler(javaTypeReference, null);
  }

  public TypeHandler<?> getTypeHandler(JdbcType jdbcType) {
    return jdbcTypeHandlerMap.get(jdbcType);
  }

  public <T> TypeHandler<T> getTypeHandler(Class<T> type, JdbcType jdbcType) {
    return getTypeHandler((Type) type, jdbcType);
  }

  public <T> TypeHandler<T> getTypeHandler(TypeReference<T> javaTypeReference, JdbcType jdbcType) {
    return getTypeHandler(javaTypeReference.getRawType(), jdbcType);
  }

  @SuppressWarnings("unchecked")
  private <T> TypeHandler<T> getTypeHandler(Type type, JdbcType jdbcType) {
    if (ParamMap.class.equals(type)) {
      return null;
    }
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = getJdbcHandlerMap(type);
    TypeHandler<?> handler = null;
    if (jdbcHandlerMap != null) {
      handler = jdbcHandlerMap.get(jdbcType);
      if (handler == null) {
        handler = jdbcHandlerMap.get(null);
        if (handler == null) {
          // #591
          handler = pickSoleHandler(jdbcHandlerMap);
        }
      }
    }
    // type drives generics here
    return (TypeHandler<T>) handler;
  }

  private Map<JdbcType, TypeHandler<?>> getJdbcHandlerMap(Type type) {
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = typeHandlerMap.get(type);
    if (jdbcHandlerMap != null) {
      return NULL_TYPE_HANDLER_MAP.equals(jdbcHandlerMap) ? null : jdbcHandlerMap;
    }
    if (type instanceof Class) {
      Class<?> clazz = (Class<?>) type;
      if (Enum.class.isAssignableFrom(clazz)) {
        Class<?> enumClass = clazz.isAnonymousClass() ? clazz.getSuperclass() : clazz;
        jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(enumClass, enumClass);
        if (jdbcHandlerMap == null) {
          register(enumClass, getInstance(enumClass, defaultEnumTypeHandler));
          return typeHandlerMap.get(enumClass);
        }
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
      if (jdbcHandlerMap != null) {
        // Found a type handler registered to a super interface
        HashMap<JdbcType, TypeHandler<?>> newMap = new HashMap<>();
        for (Entry<JdbcType, TypeHandler<?>> entry : jdbcHandlerMap.entrySet()) {
          // Create a type handler instance with enum type as a constructor arg
          newMap.put(entry.getKey(), getInstance(enumClazz, entry.getValue().getClass()));
        }
        return newMap;
      }
      jdbcHandlerMap = getJdbcHandlerMapForEnumInterfaces(iface, enumClazz);
      if (jdbcHandlerMap != null) {
        return jdbcHandlerMap;
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

  @SuppressWarnings("unchecked")
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
      // @since 3.1.0 - try to auto-discover the mapped type
      if (typeHandler instanceof TypeReference) {
        try {
          TypeReference<T> typeReference = (TypeReference<T>) typeHandler;
          register(typeReference.getRawType(), typeHandler);
          mappedTypeFound = true;
        } catch (Throwable t) {
          // maybe users define the TypeReference with a different type and are not assignable, so just ignore it
        }
      }
      if (!mappedTypeFound) {
        register((Class<T>) null, typeHandler);
      }
    }
  }

  // java type + handler

  public <T> void register(Class<T> javaType, TypeHandler<? extends T> typeHandler) {
    register((Type) javaType, typeHandler);
  }

  private <T> void register(Type javaType, TypeHandler<? extends T> typeHandler) {
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

  public <T> void register(TypeReference<T> javaTypeReference, TypeHandler<? extends T> handler) {
    register(javaTypeReference.getRawType(), handler);
  }

  // java type + jdbc type + handler

  // Cast is required here
  @SuppressWarnings("cast")
  public <T> void register(Class<T> type, JdbcType jdbcType, TypeHandler<? extends T> handler) {
    register((Type) type, jdbcType, handler);
  }

  private void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
    if (javaType != null) {
      Map<JdbcType, TypeHandler<?>> map = typeHandlerMap.get(javaType);
      if (map == null || map == NULL_TYPE_HANDLER_MAP) {
        map = new HashMap<>();
      }
      map.put(jdbcType, handler);
      typeHandlerMap.put(javaType, map);
    }
    allTypeHandlersMap.put(handler.getClass(), handler);
  }

  //
  // REGISTER CLASS
  //

  // Only handler type

  public void register(Class<?> typeHandlerClass) {
    boolean mappedTypeFound = false;
    MappedTypes mappedTypes = typeHandlerClass.getAnnotation(MappedTypes.class);
    if (mappedTypes != null) {
      for (Class<?> javaTypeClass : mappedTypes.value()) {
        register(javaTypeClass, typeHandlerClass);
        mappedTypeFound = true;
      }
    }
    if (!mappedTypeFound) {
      register(getInstance(null, typeHandlerClass));
    }
  }

  // java type + handler type

  public void register(String javaTypeClassName, String typeHandlerClassName) throws ClassNotFoundException {
    register(Resources.classForName(javaTypeClassName), Resources.classForName(typeHandlerClassName));
  }

  public void register(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
    register(javaTypeClass, getInstance(javaTypeClass, typeHandlerClass));
  }

  // java type + jdbc type + handler type

  public void register(Class<?> javaTypeClass, JdbcType jdbcType, Class<?> typeHandlerClass) {
    register(javaTypeClass, jdbcType, getInstance(javaTypeClass, typeHandlerClass));
  }

  // Construct a handler (used also from Builders)

  @SuppressWarnings("unchecked")
  public <T> TypeHandler<T> getInstance(Class<?> javaTypeClass, Class<?> typeHandlerClass) {
    if (javaTypeClass != null) {
      try {
        Constructor<?> c = typeHandlerClass.getConstructor(Class.class);
        return (TypeHandler<T>) c.newInstance(javaTypeClass);
      } catch (NoSuchMethodException ignored) {
        // ignored
      } catch (Exception e) {
        throw new TypeException("Failed invoking constructor for handler " + typeHandlerClass, e);
      }
    }
    try {
      Constructor<?> c = typeHandlerClass.getConstructor();
      return (TypeHandler<T>) c.newInstance();
    } catch (Exception e) {
      throw new TypeException("Unable to find a usable constructor for " + typeHandlerClass, e);
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
   * Gets the type handlers.
   *
   * @return the type handlers
   *
   * @since 3.2.2
   */
  public Collection<TypeHandler<?>> getTypeHandlers() {
    return Collections.unmodifiableCollection(allTypeHandlersMap.values());
  }

}
