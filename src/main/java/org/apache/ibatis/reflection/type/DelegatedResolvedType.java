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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class DelegatedResolvedType implements ResolvedType {

  protected final ResolvedType delegate;

  public DelegatedResolvedType(ResolvedType delegate) {
    this.delegate = delegate;
  }

  @Override
  public Class<?> getRawClass() {
    return delegate.getRawClass();
  }

  @Override
  public boolean isPrimitive() {
    return delegate.isPrimitive();
  }

  @Override
  public boolean isArrayType() {
    return delegate.isArrayType();
  }

  @Override
  public boolean isEnumType() {
    return delegate.isEnumType();
  }

  @Override
  public boolean isRecordType() {
    return delegate.isRecordType();
  }

  @Override
  public boolean isInterface() {
    return delegate.isInterface();
  }

  @Override
  public boolean isTypeOrSubTypeOf(Class<?> clazz) {
    return delegate.isTypeOrSubTypeOf(clazz);
  }

  @Override
  public boolean isTypeOrSuperTypeOf(Class<?> clazz) {
    return delegate.isTypeOrSuperTypeOf(clazz);
  }

  @Override
  public boolean isJavaLangObject() {
    return delegate.isJavaLangObject();
  }

  @Override
  public boolean hasRawClass(Class<?> clazz) {
    return delegate.hasRawClass(clazz);
  }

  @Override
  public boolean hasContentType() {
    return delegate.hasContentType();
  }

  @Override
  public ResolvedType findSuperType(Class<?> clazz) {
    return delegate.findSuperType(clazz);
  }

  @Override
  public ResolvedType[] getInterfaces() {
    return delegate.getInterfaces();
  }

  @Override
  public ResolvedType getSuperclass() {
    return delegate.getSuperclass();
  }

  @Override
  public ResolvedType getContentType() {
    return delegate.getContentType();
  }

  @Override
  public ResolvedType[] findTypeParameters(Class<?> rawClass) {
    return delegate.findTypeParameters(rawClass);
  }

  @Override
  public ResolvedType[] getTypeParameters() {
    return delegate.getTypeParameters();
  }

  @Override
  public ResolvedMethod findMapperMethod(String name) {
    return delegate.findMapperMethod(name);
  }

  @Override
  public ResolvedMethod resolveMethod(String name, Class<?>... parameterTypes) {
    return delegate.resolveMethod(name, parameterTypes);
  }

  @Override
  public ResolvedMethod resolveMethod(Method method) {
    return delegate.resolveMethod(method);
  }

  @Override
  public ResolvedType resolveMemberType(Type type) {
    return this.delegate.resolveMemberType(type);
  }

  @Override
  public ResolvedType resolveFieldType(Field field) {
    return delegate.resolveFieldType(field);
  }

  @Override
  public ResolvedTypeFactory getResolvedTypeFactory() {
    return delegate.getResolvedTypeFactory();
  }

  @Override
  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public String toString() {
    return delegate.toString();
  }

  @Override
  public String toCanonical() {
    return delegate.toCanonical();
  }

  @Override
  public String getTypeName() {
    return delegate.getTypeName();
  }

}
