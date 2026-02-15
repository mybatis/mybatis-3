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
package org.apache.ibatis.reflection.invoker;

import org.apache.ibatis.reflection.ReflectionUtil;
import org.apache.ibatis.reflection.Reflector;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Clinton Begin
 */
public class MethodInvoker implements Invoker {

  private final MethodHandle methodHandle;
  private final Method method;
  private final Class<?> type;

  public MethodInvoker(Method method) {
    this.method = method;

    if (method.getParameterTypes().length == 1) {
      this.type = method.getParameterTypes()[0];
    } else {
      this.type = method.getReturnType();
    }

    Class<?> methodClass = method.getDeclaringClass();
    if (ReflectionUtil.jdk8Class(methodClass)) {
      // JDK inner class or JDK 8 → fallback
      this.methodHandle = null;
    } else {
      // JDK 9+ business class → MethodHandle
      try {
        if (Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(methodClass.getModifiers())) {
          this.methodHandle = MethodHandles.lookup().unreflect(method);
        } else {
          this.methodHandle = ReflectionUtil.getMethodHandlesLookup(methodClass).unreflect(method);
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public Object invoke(Object target, Object[] args) throws Throwable {
    if (methodHandle != null) {
      return ReflectionUtil.invoke(methodHandle, target, args);
    } else {
      try {
        return method.invoke(target, args);
      } catch (IllegalAccessException e) {
        if (Reflector.canControlMemberAccessible()) {
          method.setAccessible(true);
          return method.invoke(target, args);
        }
        throw e;
      }
    }
  }

  @Override
  public Class<?> getType() {
    return type;
  }
}
