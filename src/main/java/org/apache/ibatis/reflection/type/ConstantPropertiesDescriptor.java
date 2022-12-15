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

import org.apache.ibatis.reflection.PropertiesDescriptor;

import java.util.Objects;

public class ConstantPropertiesDescriptor implements PropertiesDescriptor {
  protected final ResolvedType resolvedType;

  public ConstantPropertiesDescriptor(ResolvedType resolvedType) {
    this.resolvedType = resolvedType;
  }

  public static ConstantPropertiesDescriptor forContentType(ResolvedType collectionType) {
    ResolvedType contentType = collectionType.getContentType();
    Objects.requireNonNull(contentType, () -> String.format("Type %s do not have contentType", contentType.toCanonical()));
    return new ConstantPropertiesDescriptor(contentType);
  }

  @Override
  public boolean hasGetter(String name) {
    return true;
  }

  @Override
  public ResolvedType getGetterResolvedType(String name) {
    return resolvedType;
  }
}
