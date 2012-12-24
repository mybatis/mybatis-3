/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.binding;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

public class MapperProxy implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private final SqlSession sqlSession;
  private final Class<?>[] interfaces;
  private final Map<Method, MapperMethod> methodCache;

  protected MapperProxy(SqlSession sqlSession, Class<?>[] interfaces, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.interfaces = interfaces;
    this.methodCache = methodCache;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    final Object result = mapperMethod.execute(sqlSession, args);
    if (result == null && method.getReturnType().isPrimitive() && !method.getReturnType().equals(Void.TYPE)) {
      throw new BindingException("Mapper method '" + method.getName() + "' (" + method.getDeclaringClass() + ") attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
    }
    return result;
  }

  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = (MapperMethod) methodCache.get(method);
    if (mapperMethod == null) {
      mapperMethod = newMapperMethod(method);
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }

  private MapperMethod newMapperMethod(Method method) {
    final Class<?> declaringInterface = findDeclaringInterface(method);
    return new MapperMethod(declaringInterface, method, sqlSession.getConfiguration());
  }

  private Class<?> findDeclaringInterface(Method method) {
    Class<?> declaringInterface = null;
    for (Class<?> iface : interfaces) {
      try {
        Method m = iface.getMethod(method.getName(), method.getParameterTypes());
        if (declaringInterface != null) {
          throw new BindingException("Ambiguous method mapping.  Two mapper interfaces contain the identical method signature for " + method);
        } else if (m != null) {
          declaringInterface = iface;
        }
      } catch (Exception e) {
        // Intentionally ignore.
        // This is using exceptions for flow control,
        // but it's definitely faster.
      }
    }
    if (declaringInterface == null) {
      throw new BindingException("Could not find interface with the given method " + method);
    }
    return declaringInterface;
  }

  @SuppressWarnings("unchecked")
  public static <T> T newMapperProxy(Class<T> mapperInterface, SqlSession sqlSession, Map<Method, MapperMethod> methodCache) {
    ClassLoader classLoader = mapperInterface.getClassLoader();
    Class<?>[] interfaces = new Class[]{mapperInterface};
    MapperProxy proxy = new MapperProxy(sqlSession, interfaces, methodCache);
    return (T) Proxy.newProxyInstance(classLoader, interfaces, proxy);
  }

}
