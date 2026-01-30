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
import java.lang.reflect.Field;

import org.apache.ibatis.reflection.Reflector;

/**
 * @author Clinton Begin
 */
public class GetFieldInvoker implements Invoker {

  private final MethodHandle getter;
  private final Class<?> type;

  public GetFieldInvoker(Field field) {
    try {
      field.setAccessible(true);

      MethodHandles.Lookup privateLookup = getMethodHandlesLookup(field.getDeclaringClass());

      this.getter = privateLookup.unreflectGetter(field);
      this.type = field.getType();

    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Object invoke(Object target, Object[] args) throws Throwable {
    return getter.invoke(target);
  }

  @Override
  public Class<?> getType() {
    return type;
  }
}

