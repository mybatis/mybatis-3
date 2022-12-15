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

import org.apache.ibatis.reflection.ReflectionException;

import java.util.Map;

public class MapDescriptorResolvedType extends DelegatedResolvedType implements PropertiesDescriptorResolvedType {
  protected final Map<String, ResolvedType> typeMap;

  public MapDescriptorResolvedType(ResolvedType resolvedType, Map<String, ResolvedType> typeMap) {
    super(resolvedType);
    this.typeMap = typeMap;
  }

  public static MapDescriptorResolvedType paramMap(ResolvedTypeFactory resolvedTypeFactory, Map<String, ResolvedType> typeMap) {
    return new MapDescriptorResolvedType(resolvedTypeFactory.getParamMapType(), typeMap);
  }

  @Override
  public boolean hasGetter(String name) {
    return typeMap.containsKey(name);
  }

  @Override
  public ResolvedType getGetterResolvedType(String name) {
    ResolvedType type = typeMap.get(name);
    if (type == null) {
      throw new ReflectionException("Parameter '" + name + "' not found. Available parameters are " + typeMap.keySet());
    }
    return type;
  }

}
