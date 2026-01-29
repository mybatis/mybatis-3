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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * @author Clinton Begin
 */
public class MethodInvoker implements Invoker {

  private final Class<?> type;
  private final MethodHandle handle;

  public MethodInvoker(Method method) {
    try {
      method.setAccessible(true);
      this.handle = MethodHandles.lookup().unreflect(method);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

    if (method.getParameterCount() == 1) {
      type = method.getParameterTypes()[0];
    } else {
      type = method.getReturnType();
    }
  }

  @Override
  public Object invoke(Object target, Object[] args) throws Throwable {
    if (args == null || args.length == 0) {
      return handle.invoke(target);
    }
    return handle.invokeWithArguments(prepend(target, args));
  }

  private Object[] prepend(Object target, Object[] args) {
    Object[] arr = new Object[args.length + 1];
    arr[0] = target;
    System.arraycopy(args, 0, arr, 1, args.length);
    return arr;
  }

  @Override
  public Class<?> getType() {
    return type;
  }
}
