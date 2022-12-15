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
package org.apache.ibatis.reflection.type;

import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * Wrap class for {@link Class} with addon ParameterType info
 *
 * @author FlyInWind
 */
public interface ResolvedType extends Type {

  Class<?> getRawClass();

  /**
   * @return {@link Class#isPrimitive}
   */
  boolean isPrimitive();

  /**
   * @return {@link Class#isArray}
   */
  boolean isArrayType();

  /**
   * @return {@link Enum}.class.isAssignableFrom({@link #getRawClass})
   */
  boolean isEnumType();

  /**
   * Java14-added new {@code Record} types
   */
  boolean isRecordType();

  /**
   * @return {@link Class#isInterface}
   */
  boolean isInterface();

  /**
   * @return {@code clazz.isAssignedFrom(this)}
   */
  boolean isTypeOrSubTypeOf(Class<?> clazz);

  /**
   * @return {@code this.isAssignedFrom(clazz)}
   */
  boolean isTypeOrSuperTypeOf(Class<?> clazz);

  /**
   * @return {@link #getRawClass()} {@code == Object.class}
   */
  boolean isJavaLangObject();

  /**
   * {@link #getRawClass()} == clazz
   */
  boolean hasRawClass(Class<?> clazz);

  /**
   * Array, {@link Collection}, {@link Map} has content type
   */
  boolean hasContentType();

  /**
   * @param clazz class or super class of {@link #getRawClass()}
   * @return clazz type with addon ParameterType info
   */
  ResolvedType findSuperType(Class<?> clazz);

  ResolvedType[] getInterfaces();

  ResolvedType getSuperclass();

  /**
   * for {@link Collection}{@code <T>} , {@code T[]} , {@link Map}{@code <K,T>} return T
   *
   * @return collection content type
   */
  ResolvedType getContentType();

  ResolvedType[] findTypeParameters(Class<?> rawClass);

  ResolvedType[] getTypeParameters();

  /**
   * find first method may be a mapper method with special name, the method must not bridge or default
   *
   * @see MapperAnnotationBuilder#canHaveStatement
   */
  ResolvedMethod findMapperMethod(String name);

  ResolvedMethod resolveMethod(String name, Class<?>... parameterTypes);

  ResolvedMethod resolveMethod(Method method);

  ResolvedType resolveMemberType(Type type);

  ResolvedType resolveFieldType(Field field);

  ResolvedTypeFactory getResolvedTypeFactory();

  @Override
  boolean equals(Object o);

  @Override
  int hashCode();

  @Override
  String toString();

  String toCanonical();

}
