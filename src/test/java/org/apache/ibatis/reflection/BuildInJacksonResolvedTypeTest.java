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
package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.type.ResolvedMethod;
import org.apache.ibatis.reflection.type.ResolvedType;
import org.apache.ibatis.reflection.type.ResolvedTypeFactory;
import org.apache.ibatis.reflection.type.ResolvedTypeUtil;
import org.apache.ibatis.reflection.typeparam.Calculator;
import org.apache.ibatis.reflection.typeparam.Calculator.SubCalculator;
import org.apache.ibatis.reflection.typeparam.Level0Mapper;
import org.apache.ibatis.reflection.typeparam.Level0Mapper.Level0InnerMapper;
import org.apache.ibatis.reflection.typeparam.Level1Mapper;
import org.apache.ibatis.reflection.typeparam.Level2Mapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildInJacksonResolvedTypeTest {
  ResolvedTypeFactory resolvedTypeFactory = ResolvedTypeUtil.getBuildInJacksonTypeFactory();

  @Test
  void testReturn_Lv0SimpleClass() throws Exception {
    Class<?> clazz = Level0Mapper.class;
    Method method = clazz.getMethod("simpleSelect");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(Double.class, paramType.getRawClass());
  }

  @Test
  void testReturn_SimpleVoid() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectVoid", Integer.class);
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(void.class, paramType.getRawClass());
  }

  @Test
  void testReturn_SimplePrimitive() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectPrimitive", int.class);
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(double.class, paramType.getRawClass());
  }

  @Test
  void testReturn_SimpleClass() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelect");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(Double.class, paramType.getRawClass());
  }

  @Test
  void testReturn_SimpleList() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectList");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(List.class, paramType.getRawClass());
    assertEquals(1, paramType.findTypeParameters(List.class).length);
    assertEquals(Double.class, paramType.findTypeParameters(List.class)[0].getRawClass());
  }

  @Test
  void testReturn_SimpleMap() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectMap");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(Map.class, paramType.getRawClass());
    ResolvedType[] typeParameters = paramType.findTypeParameters(Map.class);
    assertEquals(2, typeParameters.length);
    assertEquals(Integer.class, typeParameters[0].getRawClass());
    assertEquals(Double.class, typeParameters[1].getRawClass());
  }

  @Test
  void testReturn_SimpleArray() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectArray");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertTrue(paramType.isArrayType());
    assertEquals(String.class, paramType.getContentType().getRawClass());
  }

  @Test
  void testReturn_SimpleArrayOfArray() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectArrayOfArray");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertTrue(paramType.isArrayType());
    assertTrue(paramType.getContentType().isArrayType());
    assertEquals(String.class, paramType.getContentType().getContentType().getRawClass());
  }

  @Test
  void testReturn_SimpleTypeVar() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectTypeVar");
    ResolvedType returnType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(Calculator.class, returnType.getRawClass());
    assertEquals(1, returnType.getTypeParameters().length);
    assertEquals(Object.class, returnType.getTypeParameters()[0].getRawClass());
  }

  @Test
  void testReturn_Lv1Class() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(String.class, paramType.getRawClass());
  }

  @Test
  void testReturn_Lv2CustomClass() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculator", Calculator.class);
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(Calculator.class, paramType.getRawClass());
    assertEquals(1, paramType.getTypeParameters().length);
    assertEquals(String.class, paramType.getTypeParameters()[0].getRawClass());
  }

  @Test
  void testReturn_Lv2CustomClassList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculatorList");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(List.class, paramType.getRawClass());
    assertEquals(1, paramType.getTypeParameters().length);
    ResolvedType paramTypeInner = paramType.getContentType();
    assertEquals(Calculator.class, paramTypeInner.getRawClass());
    assertEquals(Date.class, paramTypeInner.getTypeParameters()[0].getRawClass());
  }

  @Test
  void testReturn_Lv0InnerClass() throws Exception {
    Class<?> clazz = Level0InnerMapper.class;
    Method method = clazz.getMethod("select", Object.class);
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(Float.class, paramType.getRawClass());
  }

  @Test
  void testReturn_Lv2Class() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(String.class, paramType.getRawClass());
  }

  @Test
  void testReturn_Lv1List() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectList", Object.class, Object.class);
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(List.class, paramType.getRawClass());
    assertEquals(1, paramType.getTypeParameters().length);
    assertEquals(String.class, paramType.getTypeParameters()[0].getRawClass());
  }

  @Test
  void testReturn_Lv1Array() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectArray", List[].class);
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertTrue(paramType.isArrayType());
    assertEquals(String.class, paramType.getContentType().getRawClass());
  }

  @Test
  void testReturn_Lv2ArrayOfArray() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectArrayOfArray");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertTrue(paramType.isArrayType());
    assertTrue(paramType.getContentType().isArrayType());
    assertEquals(String.class, paramType.getContentType().getContentType().getRawClass());
  }

  @Test
  void testReturn_Lv2ArrayOfList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectArrayOfList");
    ResolvedType result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertTrue(result.isArrayType());
    ResolvedType paramType = result.getContentType();
    assertEquals(List.class, paramType.getRawClass());
    assertEquals(String.class, paramType.getTypeParameters()[0].getRawClass());
  }

  @Test
  void testReturn_Lv2WildcardList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectWildcardList");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(List.class, paramType.getRawClass());
    assertEquals(1, paramType.getTypeParameters().length);
    assertEquals(String.class, paramType.getTypeParameters()[0].getRawClass());
  }

  @Test
  void testReturn_LV2Map() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectMap");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(Map.class, paramType.getRawClass());
    assertEquals(2, paramType.getTypeParameters().length);
    assertEquals(String.class, paramType.getTypeParameters()[0].getRawClass());
    assertEquals(Integer.class, paramType.getTypeParameters()[1].getRawClass());
  }

  @Test
  void testReturn_Subclass() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Method method = clazz.getMethod("getId");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(String.class, paramType.getRawClass());
  }

  @Test
  void testParam_Primitive() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("simpleSelectPrimitive", int.class);
    ResolvedType[] result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getParameterTypes();
    assertEquals(1, result.length);
    assertEquals(int.class, result[0].getRawClass());
  }

  @Test
  void testParam_Simple() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectVoid", Integer.class);
    ResolvedType[] result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getParameterTypes();
    assertEquals(1, result.length);
    assertEquals(Integer.class, result[0].getRawClass());
  }

  @Test
  void testParam_Lv1Single() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    ResolvedType[] result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getParameterTypes();
    assertEquals(1, result.length);
    assertEquals(String.class, result[0].getRawClass());
  }

  @Test
  void testParam_Lv2Single() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    ResolvedType[] result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getParameterTypes();
    assertEquals(1, result.length);
    assertEquals(String.class, result[0].getRawClass());
  }

  @Test
  void testParam_Lv2Multiple() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectList", Object.class, Object.class);
    ResolvedType[] result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getParameterTypes();
    assertEquals(2, result.length);
    assertEquals(Integer.class, result[0].getRawClass());
    assertEquals(String.class, result[1].getRawClass());
  }

  @Test
  void testParam_Lv2CustomClass() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculator", Calculator.class);
    ResolvedType[] result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getParameterTypes();
    assertEquals(1, result.length);
    ResolvedType paramType = result[0];
    assertEquals(Calculator.class, paramType.getRawClass());
    assertEquals(1, paramType.getTypeParameters().length);
    assertEquals(String.class, paramType.getTypeParameters()[0].getRawClass());
  }

  @Test
  void testParam_Lv1Array() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectArray", List[].class);
    ResolvedType[] result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getParameterTypes();
    ResolvedType paramType = result[0].getContentType();
    assertEquals(List.class, paramType.getRawClass());
    assertEquals(String.class, paramType.getTypeParameters()[0].getRawClass());
  }

  @Test
  void testParam_Subclass() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Method method = clazz.getMethod("setId", Object.class);
    ResolvedType[] result = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getParameterTypes();
    assertEquals(String.class, result[0].getRawClass());
  }

  @Test
  void testReturn_Anonymous() throws Exception {
    Calculator<?> instance = new Calculator<Integer>();
    Class<?> clazz = instance.getClass();
    Method method = clazz.getMethod("getId");
    ResolvedType paramType = resolvedTypeFactory.constructType(clazz).resolveMethod(method).getReturnType();
    assertEquals(Object.class, paramType.getRawClass());
  }

  @Test
  void testField_GenericField() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Class<?> declaredClass = Calculator.class;
    Field field = declaredClass.getDeclaredField("fld");
    ResolvedType result = resolvedTypeFactory.constructType(clazz).resolveFieldType(field);
    assertEquals(String.class, result.getRawClass());
  }

  @Test
  void testReturnParam_WildcardWithUpperBounds() throws Exception {
    class Key {
    }
    @SuppressWarnings("unused")
    class KeyBean<S extends Key & Cloneable, T extends Key> {
      private S key1;
      private T key2;

      public S getKey1() {
        return key1;
      }

      public void setKey1(S key1) {
        this.key1 = key1;
      }

      public T getKey2() {
        return key2;
      }

      public void setKey2(T key2) {
        this.key2 = key2;
      }
    }
    Class<?> clazz = KeyBean.class;
    ResolvedType resolvedType = resolvedTypeFactory.constructType(clazz);
    ResolvedMethod getter1 = resolvedType.resolveMethod("getKey1");
    assertEquals(Key.class, getter1.getReturnType().getRawClass());
    ResolvedMethod setter1 = resolvedType.resolveMethod("setKey1", Key.class);
    assertEquals(Key.class, setter1.getParameterTypes()[0].getRawClass());
    ResolvedMethod getter2 = resolvedType.resolveMethod("getKey2");
    assertEquals(Key.class, getter2.getReturnType().getRawClass());
    ResolvedMethod setter2 = resolvedType.resolveMethod("setKey2", Key.class);
    assertEquals(Key.class, setter2.getParameterTypes()[0].getRawClass());

    getter1 = resolvedType.findMapperMethod("getKey1");
    assertEquals(Key.class, getter1.getReturnType().getRawClass());
    setter1 = resolvedType.findMapperMethod("setKey1");
    assertEquals(Key.class, setter1.getParameterTypes()[0].getRawClass());
    getter2 = resolvedType.findMapperMethod("getKey2");
    assertEquals(Key.class, getter2.getReturnType().getRawClass());
    setter2 = resolvedType.findMapperMethod("setKey2");
    assertEquals(Key.class, setter2.getParameterTypes()[0].getRawClass());
  }

  @Test
  void testDeepHierarchy() throws Exception {
    @SuppressWarnings("unused")
    abstract class A<S> {
      protected S id;

      public S getId() {
        return this.id;
      }

      public void setId(S id) {
        this.id = id;
      }
    }
    abstract class B<T> extends A<T> {
    }
    abstract class C<U> extends B<U> {
    }
    class D extends C<Integer> {
    }
    Class<?> clazz = D.class;
    ResolvedType resolvedType = resolvedTypeFactory.constructType(clazz);
    ResolvedMethod method = resolvedType.resolveMethod("getId");
    assertEquals(Integer.class, method.getReturnType().getRawClass());
    Field field = A.class.getDeclaredField("id");
    assertEquals(Integer.class, resolvedType.resolveFieldType(field).getRawClass());
  }

  @Test
  void shouldTypeVariablesBeComparedWithEquals() throws Exception {
    // #1794
    ExecutorService executor = Executors.newFixedThreadPool(2);
    Future<Type> futureA = executor.submit(() -> {
      ResolvedType retType = resolvedTypeFactory.constructType(IfaceA.class).resolveMethod(IfaceA.class.getMethods()[0]).getReturnType();
      return retType.getTypeParameters()[0].getRawClass();
    });
    Future<Type> futureB = executor.submit(() -> {
      ResolvedType retType = resolvedTypeFactory.constructType(IfaceB.class).resolveMethod(IfaceB.class.getMethods()[0]).getReturnType();
      return retType.getTypeParameters()[0].getRawClass();
    });
    assertEquals(AA.class, futureA.get());
    assertEquals(BB.class, futureB.get());
    executor.shutdown();
  }

  // @formatter:off
  class AA {
  }

  class BB {
  }

  interface IfaceA extends ParentIface<AA> {
  }

  interface IfaceB extends ParentIface<BB> {
  }

  interface ParentIface<T> {
    List<T> m();
  }
  // @formatter:on
}
