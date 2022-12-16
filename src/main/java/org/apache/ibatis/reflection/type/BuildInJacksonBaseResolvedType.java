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

import org.apache.ibatis.reflection.type.jackson.databind.JavaType;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author FlyInWind
 */
public class BuildInJacksonBaseResolvedType extends BaseResolvedType<JavaType, BuildInJacksonBaseResolvedTypeFactory> {

  public BuildInJacksonBaseResolvedType(JavaType type, BuildInJacksonBaseResolvedTypeFactory resolvedTypeFactory) {
    super(type, resolvedTypeFactory);
  }

  @Override
  protected ResolvedType toResolvedType(JavaType javaType) {
    if (javaType == null) {
      return null;
    }
    return resolvedTypeFactory.toResolvedType(javaType);
  }

  @Override
  public Class<?> getRawClass() {
    return type.getRawClass();
  }

  @Override
  public boolean isPrimitive() {
    return type.isPrimitive();
  }

  @Override
  public boolean isArrayType() {
    return type.isArrayType();
  }

  @Override
  public boolean isEnumType() {
    return type.isEnumType();
  }

  @Override
  public boolean isRecordType() {
    return type.isRecordType();
  }

  @Override
  public boolean isInterface() {
    return type.isInterface();
  }

  @Override
  public boolean isTypeOrSubTypeOf(Class<?> clazz) {
    return type.isTypeOrSubTypeOf(clazz);
  }

  @Override
  public boolean isTypeOrSuperTypeOf(Class<?> clazz) {
    return type.isTypeOrSuperTypeOf(clazz);
  }

  @Override
  public boolean isJavaLangObject() {
    return type.isJavaLangObject();
  }

  @Override
  public boolean hasRawClass(Class<?> clazz) {
    return type.hasRawClass(clazz);
  }

  @Override
  public boolean hasContentType() {
    return type.hasContentType();
  }

  @Override
  public ResolvedType findSuperType(Class<?> clazz) {
    return toResolvedType(type.findSuperType(clazz));
  }

  @Override
  public ResolvedType[] getInterfaces() {
    List<JavaType> interfaces = type.getInterfaces();
    return toResolvedTypes(interfaces);
  }

  @Override
  public ResolvedType getSuperclass() {
    JavaType superClass = type.getSuperClass();
    if (superClass == null) {
      return null;
    }
    return toResolvedType(superClass);
  }

  @Override
  public ResolvedType getContentType() {
    return toResolvedType(type.getContentType());
  }

  @Override
  public ResolvedType[] findTypeParameters(Class<?> clazz) {
    JavaType[] types = type.findTypeParameters(clazz);
    return toResolvedTypes(types);
  }

  @Override
  public ResolvedType resolveMemberType(Type type) {
    JavaType javaType = resolvedTypeFactory.typeFactory.resolveMemberType(type, this.type.getBindings());
    return toResolvedType(javaType);
  }

  @Override
  public String toCanonical() {
    return type.toCanonical();
  }

  @Override
  public String toString() {
    return type.toCanonical();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BuildInJacksonBaseResolvedType)) return false;
    return type.equals(((BuildInJacksonBaseResolvedType) o).type);
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

}
