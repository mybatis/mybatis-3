/**
 *    Copyright 2009-2016 the original author or authors.
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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class TypeParameterResolver {

  public static Type resolveReturnType(Method method, Type mapper) {
    Type returnType = method.getGenericReturnType();
    Class<?> declaringClass = method.getDeclaringClass();
    return getReturnType(returnType, mapper, declaringClass);
  }

  private static Type getReturnType(Type returnType, Type mapper, Class<?> declaringClass) {
    Type result = null;
    if (returnType instanceof Class) {
      result = returnType;
    } else if (returnType instanceof TypeVariable) {
      if (mapper instanceof Class) {
        result = resolveTypeVar((TypeVariable<?>) returnType, mapper, declaringClass);
      }
    } else if (returnType instanceof ParameterizedType) {
      result = scanParameterizedType((ParameterizedType) returnType, mapper, declaringClass);
    }
    assert result != null;
    return result;
  }

  private static ParameterizedType scanParameterizedType(ParameterizedType parameterizedType, Type mapper, Class<?> declaringClass) {
    Class<?> rawType = (Class<?>) parameterizedType.getRawType();
    Type[] typeArgs = parameterizedType.getActualTypeArguments();
    Type[] args = new Type[typeArgs.length];
    for (int i = 0; i < typeArgs.length; i++) {
      if (typeArgs[i] instanceof Class) {
        args[i] = typeArgs[i];
      } else if (typeArgs[i] instanceof TypeVariable) {
        args[i] = resolveTypeVar((TypeVariable<?>) typeArgs[i], mapper, declaringClass);
      } else if (typeArgs[i] instanceof ParameterizedType) {
        args[i] = scanParameterizedType((ParameterizedType) typeArgs[i], mapper, declaringClass);
      }
    }
    return new ParameterizedTypeImpl(rawType, null, args);
  }

  private static Type resolveTypeVar(TypeVariable<?> typeVar, Type mapper, Class<?> declaringClass) {
    Type result = null;
    Class<?> mapperClass = null;
    if (mapper instanceof Class) {
      mapperClass = (Class<?>) mapper;
    } else if (mapper instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) mapper;
      mapperClass = (Class<?>) parameterizedType.getRawType();
    } else {
      throw new IllegalArgumentException("The 2nd arg must be Class or ParameterizedType, but was: " + mapper.getClass());
    }

    Type[] superInterfaces = mapperClass.getGenericInterfaces();
    for (Type superInterface : superInterfaces) {
      Class<?> parentAsClass = null;
      if (superInterface instanceof ParameterizedType) {
        ParameterizedType parentAsType = (ParameterizedType) superInterface;
        parentAsClass = (Class<?>) parentAsType.getRawType();
        Type[] typeArgs = parentAsType.getActualTypeArguments();
        if (declaringClass == parentAsClass) {
          TypeVariable<?>[] declaredTypeVars = declaringClass.getTypeParameters();
          for (int i = 0; i < declaredTypeVars.length; i++) {
            if (declaredTypeVars[i] == typeVar) {
              if (typeArgs[i] instanceof TypeVariable) {
                TypeVariable<?>[] mapperTypeParams = mapperClass.getTypeParameters();
                for (int j = 0; j < mapperTypeParams.length; j++) {
                  if (mapperTypeParams[j] == typeArgs[i]) {
                    result = ((ParameterizedType) mapper).getActualTypeArguments()[j];
                  }
                }
              } else {
                result = typeArgs[i];
              }
            }
          }
        } else {
          result = resolveTypeVar(typeVar, parentAsType, declaringClass);
        }
      } else if (superInterface instanceof Class) {
        result = resolveTypeVar(typeVar, superInterface, declaringClass);
      }
    }
    assert result != null;
    return result;
  }

  private TypeParameterResolver() {
    super();
  }
}
