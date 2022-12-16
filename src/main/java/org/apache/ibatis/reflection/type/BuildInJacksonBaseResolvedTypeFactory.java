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

import org.apache.ibatis.binding.MapperMethod.ParamMap;
import org.apache.ibatis.reflection.type.jackson.databind.JavaType;
import org.apache.ibatis.reflection.type.jackson.databind.type.TypeFactory;
import org.apache.ibatis.type.TypeReference;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.Map;

/**
 * @author FlyInWind
 */
public class BuildInJacksonBaseResolvedTypeFactory implements ResolvedTypeFactory {
  protected final TypeFactory typeFactory;

  protected final ResolvedType objectType;
  protected final ResolvedType integerType;
  protected final ResolvedType longType;
  protected final ResolvedType resultSetType;
  protected final ResolvedType mapType;
  protected final ResolvedType paramMapType;

  public BuildInJacksonBaseResolvedTypeFactory() {
    this(TypeFactory.defaultInstance());
  }

  public BuildInJacksonBaseResolvedTypeFactory(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
    this.objectType = constructType(Object.class);
    this.integerType = constructType(Integer.class);
    this.longType = constructType(Long.class);
    this.resultSetType = constructType(ResultSet.class);
    this.mapType = constructType(Map.class);
    this.paramMapType = constructType(ParamMap.class);
  }

  public ResolvedType toResolvedType(JavaType javaType) {
    return new BuildInJacksonBaseResolvedType(javaType, this);
  }

  @Override
  public ResolvedType constructType(Type type) {
    if (type == null) {
      return null;
    } else if (type instanceof ResolvedType) {
      return (ResolvedType) type;
    }
    return toResolvedType(typeFactory.constructType(type));
  }

  @Override
  public ResolvedType constructType(Class<?> clazz) {
    if (clazz == null) {
      return null;
    }
    return toResolvedType(typeFactory.constructType(clazz));
  }

  @Override
  public <T> ResolvedType constructType(TypeReference<T> typeReference) {
    return constructType(typeReference.getClass()).findTypeParameters(TypeReference.class)[0];
  }

  @Override
  public ResolvedType constructParametricType(Class<?> rawClass, Class<?>... typeParameters) {
    return toResolvedType(typeFactory.constructParametricType(rawClass, typeParameters));
  }

  @Override
  public ResolvedType constructFromCanonical(String canonical) {
    if (canonical == null || canonical.isEmpty()) {
      return null;
    }
    return toResolvedType(typeFactory.constructFromCanonical(canonical));
  }

  @Override
  public ResolvedType getObjectType() {
    return objectType;
  }

  @Override
  public ResolvedType getIntegerType() {
    return integerType;
  }

  @Override
  public ResolvedType getLongType() {
    return longType;
  }

  @Override
  public ResolvedType getMapType() {
    return mapType;
  }

  @Override
  public ResolvedType getResultSetType() {
    return resultSetType;
  }

  @Override
  public ResolvedType getParamMapType() {
    return paramMapType;
  }

}
