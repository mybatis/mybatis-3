package org.apache.ibatis.reflection;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * @author LuckyZhan
 **/
public final class GenericTypeRawClassPair {
  private final Type type;
  private final Class<?> rawClass;

  private GenericTypeRawClassPair(Type type, Class<?> rawClass){
    this.type = type;
    this.rawClass = rawClass;
  }

  public Type getType(){
    return type;
  }

  public Class<?> getRawClass(){
    return rawClass;
  }

  public static GenericTypeRawClassPair newGenericTypeRawClassPair(Type type, Class<?> rawClass){
    Objects.requireNonNull(rawClass, "rawClass must be present");
    if(type instanceof Class){
      return newOnlyRawClassPair(rawClass);
    }
    return new GenericTypeRawClassPair(type, rawClass);
  }

  public static GenericTypeRawClassPair newOnlyRawClassPair(Class<?> rawClass){
    Objects.requireNonNull(rawClass, "rawClass must be present");
    return new GenericTypeRawClassPair(null, rawClass);
  }

}
