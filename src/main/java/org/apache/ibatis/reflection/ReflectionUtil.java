/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.reflection;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * @author hubin
 */
public class ReflectionUtil {

  public static boolean jdk8Class(Class<?> clazz) {
    // JDK inner class or JDK 8
    return clazz.getModule().isNamed() && clazz.getModule().getName().startsWith("java.");
  }

  public static MethodHandle getMethodHandle(Method method) {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      return lookup.unreflect(method);
    } catch (IllegalAccessException ignored) {
      // ignored
    }
    return null;
  }

  public static MethodHandles.Lookup getMethodHandlesLookup(Class<?> targetClass) throws IllegalAccessException {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    return MethodHandles.privateLookupIn(targetClass, lookup);
  }

  public static Object invoke(MethodHandle methodHandle, Object target, Object[] args) throws Throwable {
    if (args == null || args.length == 0) {
      return methodHandle.invoke(target);
    } else if (args.length == 1) {
      return methodHandle.invoke(target, args[0]);
    }

    // Execute multiple parameters
    Object[] fullArgs = new Object[args.length + 1];
    fullArgs[0] = target;
    System.arraycopy(args, 0, fullArgs, 1, args.length);
    return methodHandle.invokeWithArguments(fullArgs);
  }

}
