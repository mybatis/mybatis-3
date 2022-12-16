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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.reflection.type.ResolvedType;
import org.apache.ibatis.reflection.type.ResolvedTypeFactory;

/**
 * @author FlyInWind
 */
@MappedJdbcTypes(value = JdbcType.ARRAY, includeNullJdbcType = true)
public class ListTypeHandler extends BaseCollectionTypeHandler<List<Object>> {

  public ListTypeHandler(ResolvedType resolvedType) {
    super(resolvedType);
  }

  public ListTypeHandler(String arrayTypeName) {
    super(arrayTypeName);
  }

  @Override
  protected List<Object> toCollection(Object[] array) {
    return new ArrayList<>(Arrays.asList(array));
  }

  protected static void register(TypeHandlerRegistry registry) {
    ResolvedTypeFactory resolvedTypeFactory = registry.getResolvedTypeFactory();
    ArrayTypeHandler.STANDARD_MAPPING.forEach((componentType, arrayTypeName) -> {
      if (componentType.isPrimitive()) {
        return;
      }
      TypeHandler<?> typeHandler = new ListTypeHandler(arrayTypeName);
      registry.register(resolvedTypeFactory.constructParametricType(Collection.class, componentType), typeHandler);
      registry.register(resolvedTypeFactory.constructParametricType(List.class, componentType), typeHandler);
    });
    ListTypeHandler objectListTypeHandler = new ListTypeHandler(ArrayTypeHandler.DEFAULT_TYPE_NAME);
    registry.register(resolvedTypeFactory.constructType(Collection.class), objectListTypeHandler);
    registry.register(resolvedTypeFactory.constructType(List.class), objectListTypeHandler);
    registry.addCreator(ListTypeHandler.class, ListTypeHandler::new);
  }
}
