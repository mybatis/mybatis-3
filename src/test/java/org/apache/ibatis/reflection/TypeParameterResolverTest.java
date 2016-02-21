/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.reflection;

import static org.junit.Assert.*;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.typeparam.Calculator;
import org.apache.ibatis.reflection.typeparam.Level0Mapper;
import org.apache.ibatis.reflection.typeparam.Level0Mapper.Level0InnerMapper;
import org.apache.ibatis.reflection.typeparam.Level1Mapper;
import org.apache.ibatis.reflection.typeparam.Level2Mapper;
import org.junit.Test;

public class TypeParameterResolverTest {
  @Test
  public void testReturn_Lv0SimpleClass() throws Exception {
    Class<?> clazz = Level0Mapper.class;
    Method method = clazz.getMethod("simpleSelect");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Double.class, result);
  }

  @Test
  public void testReturn_SimpleVoid() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectVoid", Integer.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(void.class, result);
  }

  @Test
  public void testReturn_SimplePrimitive() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectPrimitive", int.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(double.class, result);
  }

  @Test
  public void testReturn_SimpleClass() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelect");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Double.class, result);
  }

  @Test
  public void testReturn_SimpleList() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectList");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(List.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertEquals(Double.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  public void testReturn_SimpleMap() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectMap");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Map.class, paramType.getRawType());
    assertEquals(2, paramType.getActualTypeArguments().length);
    assertEquals(Integer.class, paramType.getActualTypeArguments()[0]);
    assertEquals(Double.class, paramType.getActualTypeArguments()[1]);
  }

  @Test
  public void testReturn_SimpleWildcard() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectWildcard");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(List.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertTrue(paramType.getActualTypeArguments()[0] instanceof WildcardType);
    WildcardType wildcard = (WildcardType) paramType.getActualTypeArguments()[0];
    assertEquals(String.class, wildcard.getUpperBounds()[0]);
  }

  @Test
  public void testReturn_SimpleArray() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertEquals(String.class, resultClass.getComponentType());
  }

  @Test
  public void testReturn_SimpleArrayOfArray() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectArrayOfArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertTrue(resultClass.getComponentType().isArray());
    assertEquals(String.class, resultClass.getComponentType().getComponentType());
  }

  @Test
  public void testReturn_SimpleTypeVar() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectTypeVar");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof TypeVariable);
    TypeVariable<?> typeVar = (TypeVariable<?>) result;
    assertEquals("K", typeVar.getName());
  }

  @Test
  public void testReturn_Lv1Class() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(String.class, result);
  }

  @Test
  public void testReturn_Lv2CustomClass() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculator", Calculator.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Calculator.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  public void testReturn_Lv2CustomClassList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculatorList");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramTypeOuter = (ParameterizedType) result;
    assertEquals(List.class, paramTypeOuter.getRawType());
    assertEquals(1, paramTypeOuter.getActualTypeArguments().length);
    ParameterizedType paramTypeInner = (ParameterizedType) paramTypeOuter.getActualTypeArguments()[0];
    assertEquals(Calculator.class, paramTypeInner.getRawType());
    assertEquals(Date.class, paramTypeInner.getActualTypeArguments()[0]);
  }

  @Test
  public void testReturn_Lv0InnerClass() throws Exception {
    Class<?> clazz = Level0InnerMapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Float.class, result);
  }

  @Test
  public void testReturn_Lv2Class() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(String.class, result);
  }

  @Test
  public void testReturn_Lv1List() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectList", Object.class, Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType type = (ParameterizedType) result;
    assertEquals(List.class, type.getRawType());
    assertEquals(1, type.getActualTypeArguments().length);
    assertEquals(String.class, type.getActualTypeArguments()[0]);
  }

  @Test
  public void testReturn_Lv1Array() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertEquals(String.class, resultClass.getComponentType());
  }

  @Test
  public void testReturn_Lv2ArrayOfArray() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectArrayOfArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(result instanceof Class);
    assertTrue(resultClass.isArray());
    assertTrue(resultClass.getComponentType().isArray());
    assertEquals(String.class, resultClass.getComponentType().getComponentType());
  }

  @Test
  public void testReturn_Lv2ArrayOfList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectArrayOfList");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof GenericArrayType);
    GenericArrayType genericArrayType = (GenericArrayType) result;
    assertTrue(genericArrayType.getGenericComponentType() instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) genericArrayType.getGenericComponentType();
    assertEquals(List.class, paramType.getRawType());
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  public void testReturn_Lv2WildcardList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectWildcardList");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType type = (ParameterizedType) result;
    assertEquals(List.class, type.getRawType());
    assertEquals(1, type.getActualTypeArguments().length);
    assertTrue(type.getActualTypeArguments()[0] instanceof WildcardType);
    WildcardType wildcard = (WildcardType) type.getActualTypeArguments()[0];
    assertEquals(0, wildcard.getLowerBounds().length);
    assertEquals(1, wildcard.getUpperBounds().length);
    assertEquals(String.class, wildcard.getUpperBounds()[0]);
  }

  @Test
  public void testReturn_LV2Map() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectMap");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Map.class, paramType.getRawType());
    assertEquals(2, paramType.getActualTypeArguments().length);
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
    assertEquals(Integer.class, paramType.getActualTypeArguments()[1]);
  }
}
