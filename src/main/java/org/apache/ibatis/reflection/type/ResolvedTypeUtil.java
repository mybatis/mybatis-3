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

import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author FlyInWind
 */
public class ResolvedTypeUtil {

  public static final ResolvedType[] EMPTY_TYPE_ARRAY = new ResolvedType[0];

  public static Class<?> getRawClass(ResolvedType type) {
    if (type == null) {
      return null;
    }
    return type.getRawClass();
  }

  public static boolean isInstance(ResolvedType type, Object obj) {
    if (type == null) {
      return false;
    }
    return type.getRawClass().isInstance(obj);
  }

  public static ResolvedTypeFactory getResolvedTypeFactory() {
    return getJacksonTypeFactory();
  }

  private static class JacksonResolvedTypeFactoryHolder {
    private static JacksonResolvedTypeFactory instance;

    static {
      try {
        Class.forName("com.fasterxml.jackson.databind.type.TypeFactory");
        instance = new JacksonResolvedTypeFactory(TypeFactory.defaultInstance());
      } catch (ClassNotFoundException e) {
        // ignore
      }
    }
  }

  public static ResolvedTypeFactory getJacksonTypeFactory() {
    return JacksonResolvedTypeFactoryHolder.instance;
  }

}
