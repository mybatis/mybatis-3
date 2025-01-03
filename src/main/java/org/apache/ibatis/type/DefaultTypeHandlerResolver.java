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
package org.apache.ibatis.type;

import java.lang.reflect.Type;

public class DefaultTypeHandlerResolver implements TypeHandlerResolver {

  private final TypeHandlerRegistry typeHandlerRegistry;

  public DefaultTypeHandlerResolver(TypeHandlerRegistry typeHandlerRegistry) {
    super();
    this.typeHandlerRegistry = typeHandlerRegistry;
  }

  @Override
  public TypeHandler<?> resolve(Class<?> declaringClass, Type propertyType, String propertyName, JdbcType jdbcType,
      Class<? extends TypeHandler<?>> typeHandlerClass) {
    Type javaType = propertyType == null ? declaringClass : propertyType;
    return typeHandlerRegistry.getTypeHandler(javaType, jdbcType, typeHandlerClass);
  }

}
