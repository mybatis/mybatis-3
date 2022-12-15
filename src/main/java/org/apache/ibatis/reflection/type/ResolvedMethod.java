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

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * @author FlyInWind
 */
public interface ResolvedMethod {

  ResolvedTypeFactory getResolvedTypeFactory();

  /**
   * @return may be {@link Method#getDeclaringClass()} or the subclass of {@link Method#getDeclaringClass()}
   */
  ResolvedType getImplementationType();

  /**
   * @return the wrapped {@link Method}
   */
  Method getMethod();

  /**
   * @return {@link Method#getParameterCount()}
   */
  int getParameterCount();

  /**
   * @return the resolved {@link Method#getGenericParameterTypes()}
   */
  ResolvedType[] getParameterTypes();

  /**
   * @return the resolved {@link Method#getGenericReturnType()}
   */
  ResolvedType getReturnType();

  /**
   * @return null if no parameter, return the first type if only one parameter, or {@link PropertiesDescriptorResolvedType} if more than one parameter,
   * the {@link PropertiesDescriptorResolvedType} contains the types of {@link ParamMap} values
   * @see ParamNameResolver#namedParamsType
   */
  ResolvedType namedParamsType(Configuration configuration);

  /**
   * @return {@link Method#getName}
   */
  String getName();

  @Override
  String toString();

}
