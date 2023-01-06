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
package org.apache.ibatis.plugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.util.MapUtil;

/**
 * @author Clinton Begin
 */
public class Plugin implements InvocationHandler {

  private final Object target;
  private final Map<Method, List<Interceptor>> interceptorMap;

  private Plugin(Object target, Map<Method, List<Interceptor>> interceptorMap) {
    this.target = target;
    this.interceptorMap = interceptorMap;
  }

  public Map<Method, List<Interceptor>> getInterceptorMap() {
    return interceptorMap;
  }

  public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
    Class<?> type = target.getClass();
    Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
    if (interfaces.length > 0) {
      if (Proxy.isProxyClass(target.getClass())) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(target);
        if (invocationHandler instanceof Plugin) {
          Map<Method, List<Interceptor>> interceptorMap = ((Plugin) invocationHandler).getInterceptorMap();
          mapping(interceptor, signatureMap, interceptorMap);
          return target;
        }
      }

      Map<Method, List<Interceptor>> interceptorMap = new HashMap<>();
      mapping(interceptor, signatureMap, interceptorMap);
      return Proxy.newProxyInstance(type.getClassLoader(), interfaces, new Plugin(target, interceptorMap));
    }
    return target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      List<Interceptor> interceptors = interceptorMap.get(method);
      if (interceptors != null) {
        return new Invocation(target, method, args, interceptors).proceed();
      }
      return method.invoke(target, args);
    } catch (Exception e) {
      throw ExceptionUtil.unwrapThrowable(e);
    }
  }

  private static void mapping(Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap, Map<Method, List<Interceptor>> interceptorMap) {
    for (Set<Method> methods : signatureMap.values()) {
      for (Method method : methods) {
        interceptorMap.computeIfAbsent(method, (key) -> new ArrayList<>()).add(interceptor);
      }
    }
  }

  private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
    Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
    // issue #251
    if (interceptsAnnotation == null) {
      throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
    }
    Signature[] sigs = interceptsAnnotation.value();
    Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
    for (Signature sig : sigs) {
      Set<Method> methods = MapUtil.computeIfAbsent(signatureMap, sig.type(), k -> new HashSet<>());
      try {
        Method method = sig.type().getMethod(sig.method(), sig.args());
        methods.add(method);
      } catch (NoSuchMethodException e) {
        throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
      }
    }
    return signatureMap;
  }

  private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
    Set<Class<?>> interfaces = new HashSet<>();
    while (type != null) {
      for (Class<?> c : type.getInterfaces()) {
        if (signatureMap.containsKey(c)) {
          interfaces.add(c);
        }
      }
      type = type.getSuperclass();
    }
    return interfaces.toArray(new Class<?>[0]);
  }

}
