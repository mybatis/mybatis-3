/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.executor.resultset;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveTypes {
  private final Map<Class<?>, Class<?>> primitiveToWrappers;
  private final Map<Class<?>, Class<?>> wrappersToPrimitives;

  public PrimitiveTypes() {
    this.primitiveToWrappers = new HashMap<Class<?>, Class<?>>();
    this.wrappersToPrimitives = new HashMap<Class<?>, Class<?>>();

    add(boolean.class, Boolean.class);
    add(byte.class, Byte.class);
    add(char.class, Character.class);
    add(double.class, Double.class);
    add(float.class, Float.class);
    add(int.class, Integer.class);
    add(long.class, Long.class);
    add(short.class, Short.class);
    add(void.class, Void.class);
  }

  private void add(final Class<?> primitiveType, final Class<?> wrapperType) {
    primitiveToWrappers.put(primitiveType, wrapperType);
    wrappersToPrimitives.put(wrapperType, primitiveType);
  }

  public Class<?> getWrapper(final Class<?> primitiveType) {
    return primitiveToWrappers.get(primitiveType);
  }

  public Class<?> getPrimitive(final Class<?> wrapperType) {
    return wrappersToPrimitives.get(wrapperType);
  }
}
