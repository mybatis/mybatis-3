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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author FlyInWind
 */
public abstract class BaseResolvedType<T, F extends ResolvedTypeFactory> implements ResolvedType {
  protected final F resolvedTypeFactory;
  protected final T type;

  protected BaseResolvedType(T type, F resolvedTypeFactory) {
    Objects.requireNonNull(type);
    this.resolvedTypeFactory = resolvedTypeFactory;
    this.type = type;
  }

  protected abstract ResolvedType toResolvedType(T type);

  protected ResolvedType[] toResolvedTypes(T[] types) {
    int size = types.length;
    if (size == 0) {
      return ResolvedTypeUtil.EMPTY_TYPE_ARRAY;
    }
    ResolvedType[] result = new ResolvedType[size];
    for (int i = 0; i < size; i++) {
      T inf = types[i];
      result[i] = toResolvedType(inf);
    }
    return result;
  }

  protected ResolvedType[] toResolvedTypes(List<T> types) {
    int size = types.size();
    if (size == 0) {
      return ResolvedTypeUtil.EMPTY_TYPE_ARRAY;
    }
    ResolvedType[] result = new ResolvedType[size];
    for (int i = 0; i < size; i++) {
      T inf = types.get(i);
      result[i] = toResolvedType(inf);
    }
    return result;
  }

  @Override
  public ResolvedMethod findMapperMethod(String name) {
    Method[] methods = getRawClass().getMethods();
    Method foundMethod = null;
    for (Method method : methods) {
      if (method.getName().equals(name) && !method.isBridge() && !method.isDefault()) {
        foundMethod = method;
        break;
      }
    }
    if (foundMethod != null) {
      return this.resolveMethod(foundMethod);
    }
    return null;
  }

  @Override
  public ResolvedMethod resolveMethod(String name, Class<?>... parameterTypes) {
    try {
      Method method = getRawClass().getMethod(name, parameterTypes);
      return resolveMethod(method);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  @Override
  public ResolvedMethod resolveMethod(Method method) {
    return new DefaultResolvedMethod(this, method);
  }

  @Override
  public ResolvedTypeFactory getResolvedTypeFactory() {
    return resolvedTypeFactory;
  }

  @Override
  public ResolvedType[] getTypeParameters() {
    return findTypeParameters(getRawClass());
  }

  @Override
  public ResolvedType resolveFieldType(Field field) {
    return findSuperType(field.getDeclaringClass()).resolveMemberType(field.getGenericType());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(getClass().isInstance(o))) return false;
    return type.equals(((BaseResolvedType<?, ?>) o).type);
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    return type.toString();
  }

}
