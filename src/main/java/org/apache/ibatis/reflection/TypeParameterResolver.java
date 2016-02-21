/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * @author Iwao AVE!
 */
public class TypeParameterResolver {

  /**
   * @return The return type of the method as {@link Type}. If it has type parameters in the declaration,<br>
   *         they will be resolved to the actual runtime {@link Type}.
   */
  public static Type resolveReturnType(Method method, Type mapper) {
    Type returnType = method.getGenericReturnType();
    Class<?> declaringClass = method.getDeclaringClass();
    Type result = null;
    if (returnType instanceof TypeVariable) {
      result = resolveTypeVar((TypeVariable<?>) returnType, mapper, declaringClass);
    } else if (returnType instanceof ParameterizedType) {
      result = resolveParameterizedType((ParameterizedType) returnType, mapper, declaringClass);
    } else if (returnType instanceof GenericArrayType) {
      result = resolveGenericArrayType((GenericArrayType) returnType, mapper, declaringClass);
    } else {
      result = returnType;
    }
    return result;
  }

  public static Type[] resolveParamTypes(Method method, Type mapper) {
    Type[] paramTypes = method.getGenericParameterTypes();
    Class<?> declaringClass = method.getDeclaringClass();
    Type[] result = new Type[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      if (paramTypes[i] instanceof Class) {
        result[i] = paramTypes[i];
      } else if (paramTypes[i] instanceof TypeVariable) {
        result[i] = resolveTypeVar((TypeVariable<?>) paramTypes[i], mapper, declaringClass);
      } else if (paramTypes[i] instanceof ParameterizedType) {
        result[i] = resolveParameterizedType((ParameterizedType) paramTypes[i], mapper, declaringClass);
      } else if (paramTypes[i] instanceof GenericArrayType) {
        result[i] = resolveGenericArrayType((GenericArrayType) paramTypes[i], mapper, declaringClass);
      } else {
        // should we support other types?
      }
    }
    return result;
  }

  private static Type resolveGenericArrayType(GenericArrayType genericArrayType, Type mapper, Class<?> declaringClass) {
    Type componentType = genericArrayType.getGenericComponentType();
    Type resolvedComponentType = null;
    if (componentType instanceof TypeVariable) {
      resolvedComponentType = resolveTypeVar((TypeVariable<?>) componentType, mapper, declaringClass);
    } else if (componentType instanceof GenericArrayType) {
      resolvedComponentType = resolveGenericArrayType((GenericArrayType) componentType, mapper, declaringClass);
    } else if (componentType instanceof ParameterizedType) {
      resolvedComponentType = resolveParameterizedType((ParameterizedType) componentType, mapper, declaringClass);
    }
    if (resolvedComponentType instanceof Class) {
      return Array.newInstance((Class<?>) resolvedComponentType, 0).getClass();
    } else {
      return new GenericArrayTypeImpl(resolvedComponentType);
    }
  }

  private static ParameterizedType resolveParameterizedType(ParameterizedType parameterizedType, Type mapper, Class<?> declaringClass) {
    Class<?> rawType = (Class<?>) parameterizedType.getRawType();
    Type[] typeArgs = parameterizedType.getActualTypeArguments();
    Type[] args = new Type[typeArgs.length];
    for (int i = 0; i < typeArgs.length; i++) {
      if (typeArgs[i] instanceof TypeVariable) {
        args[i] = resolveTypeVar((TypeVariable<?>) typeArgs[i], mapper, declaringClass);
      } else if (typeArgs[i] instanceof ParameterizedType) {
        args[i] = resolveParameterizedType((ParameterizedType) typeArgs[i], mapper, declaringClass);
      } else if (typeArgs[i] instanceof WildcardType) {
        args[i] = resolveWildcardType((WildcardType) typeArgs[i], mapper, declaringClass);
      } else {
        args[i] = typeArgs[i];
      }
    }
    return new ParameterizedTypeImpl(rawType, null, args);
  }

  private static Type resolveWildcardType(WildcardType wildcardType, Type mapper, Class<?> declaringClass) {
    Type[] lowerBounds = resolveWildcardTypeBounds(wildcardType.getLowerBounds(), mapper, declaringClass);
    Type[] upperBounds = resolveWildcardTypeBounds(wildcardType.getUpperBounds(), mapper, declaringClass);
    return new WildcardTypeImpl(lowerBounds, upperBounds);
  }

  private static Type[] resolveWildcardTypeBounds(Type[] bounds, Type mapper, Class<?> declaringClass) {
    Type[] result = new Type[bounds.length];
    for (int i = 0; i < bounds.length; i++) {
      if (bounds[i] instanceof TypeVariable) {
        result[i] = resolveTypeVar((TypeVariable<?>) bounds[i], mapper, declaringClass);
      } else if (bounds[i] instanceof ParameterizedType) {
        result[i] = resolveParameterizedType((ParameterizedType) bounds[i], mapper, declaringClass);
      } else if (bounds[i] instanceof WildcardType) {
        result[i] = resolveWildcardType((WildcardType) bounds[i], mapper, declaringClass);
      } else {
        result[i] = bounds[i];
      }
    }
    return result;
  }

  private static Type resolveTypeVar(TypeVariable<?> typeVar, Type type, Class<?> declaringClass) {
    Type result = null;
    Class<?> clazz = null;
    if (type instanceof Class) {
      clazz = (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      clazz = (Class<?>) parameterizedType.getRawType();
    } else {
      throw new IllegalArgumentException("The 2nd arg must be Class or ParameterizedType, but was: " + type.getClass());
    }

    if (clazz == declaringClass) {
      return Object.class;
    }

    Type superclass = clazz.getGenericSuperclass();
    result = scanSuperTypes(typeVar, type, declaringClass, clazz, superclass);
    if (result != null) {
      return result;
    }

    Type[] superInterfaces = clazz.getGenericInterfaces();
    for (Type superInterface : superInterfaces) {
      result = scanSuperTypes(typeVar, type, declaringClass, clazz, superInterface);
      if (result != null) {
        return result;
      }
    }
    return Object.class;
  }

  private static Type scanSuperTypes(TypeVariable<?> typeVar, Type type, Class<?> declaringClass, Class<?> clazz, Type superclass) {
    Type result = null;
    if (superclass instanceof ParameterizedType) {
      ParameterizedType parentAsType = (ParameterizedType) superclass;
      Class<?> parentAsClass = (Class<?>) parentAsType.getRawType();
      if (declaringClass == parentAsClass) {
        Type[] typeArgs = parentAsType.getActualTypeArguments();
        TypeVariable<?>[] declaredTypeVars = declaringClass.getTypeParameters();
        for (int i = 0; i < declaredTypeVars.length; i++) {
          if (declaredTypeVars[i] == typeVar) {
            if (typeArgs[i] instanceof TypeVariable) {
              TypeVariable<?>[] typeParams = clazz.getTypeParameters();
              for (int j = 0; j < typeParams.length; j++) {
                if (typeParams[j] == typeArgs[i]) {
                  if (type instanceof ParameterizedType) {
                    result = ((ParameterizedType) type).getActualTypeArguments()[j];
                  }
                  break;
                }
              }
            } else {
              result = typeArgs[i];
            }
          }
        }
      } else if (declaringClass.isAssignableFrom(parentAsClass)) {
        result = resolveTypeVar(typeVar, parentAsType, declaringClass);
      }
    } else if (superclass instanceof Class) {
      if (declaringClass.isAssignableFrom((Class<?>) superclass)) {
        result = resolveTypeVar(typeVar, superclass, declaringClass);
      }
    }
    return result;
  }

  private TypeParameterResolver() {
    super();
  }

  static class ParameterizedTypeImpl implements ParameterizedType {
    private Class<?> rawType;

    private Type ownerType;

    private Type[] actualTypeArguments;

    public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
      super();
      this.rawType = rawType;
      this.ownerType = ownerType;
      this.actualTypeArguments = actualTypeArguments;
    }

    @Override
    public Type[] getActualTypeArguments() {
      return actualTypeArguments;
    }

    @Override
    public Type getOwnerType() {
      return ownerType;
    }

    @Override
    public Type getRawType() {
      return rawType;
    }
  }

  static class WildcardTypeImpl implements WildcardType {
    private Type[] lowerBounds;

    private Type[] upperBounds;

    private WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
      super();
      this.lowerBounds = lowerBounds;
      this.upperBounds = upperBounds;
    }

    @Override
    public Type[] getLowerBounds() {
      return lowerBounds;
    }

    @Override
    public Type[] getUpperBounds() {
      return upperBounds;
    }
  }

  static class GenericArrayTypeImpl implements GenericArrayType {
    private Type genericComponentType;

    private GenericArrayTypeImpl(Type genericComponentType) {
      super();
      this.genericComponentType = genericComponentType;
    }

    @Override
    public Type getGenericComponentType() {
      return genericComponentType;
    }
  }
}
