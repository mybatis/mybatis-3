/*
 *    Copyright 2009-2025 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.ibatis.reflection.typeparam.Calculator;
import org.apache.ibatis.reflection.typeparam.Calculator.SubCalculator;
import org.apache.ibatis.reflection.typeparam.Level0Mapper;
import org.apache.ibatis.reflection.typeparam.Level0Mapper.Level0InnerMapper;
import org.apache.ibatis.reflection.typeparam.Level1Mapper;
import org.apache.ibatis.reflection.typeparam.Level2Mapper;
import org.apache.ibatis.type.TypeReference;
import org.junit.jupiter.api.Test;

class TypeParameterResolverTest {

  @Test
  void returnLv0SimpleClass() throws Exception {
    Class<?> clazz = Level0Mapper.class;
    Method method = clazz.getMethod("simpleSelect");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Double.class, result);
  }

  @Test
  void returnSimpleVoid() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectVoid", Integer.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(void.class, result);
  }

  @Test
  void returnSimplePrimitive() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectPrimitive", int.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(double.class, result);
  }

  @Test
  void returnSimpleClass() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelect");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Double.class, result);
  }

  @Test
  void returnSimpleList() throws Exception {
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
  void returnSimpleMap() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectMap");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Map.class, paramType.getRawType());
    assertEquals(2, paramType.getActualTypeArguments().length);
    assertEquals(Integer.class, paramType.getActualTypeArguments()[0]);
    assertEquals(Double.class, paramType.getActualTypeArguments()[1]);
    assertEquals("java.util.Map<java.lang.Integer, java.lang.Double>", result.toString());
  }

  @Test
  void returnSimpleWildcard() throws Exception {
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
    assertEquals("java.util.List<? extends java.lang.String>", paramType.toString());
  }

  @Test
  void returnSimpleArray() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertEquals(String.class, resultClass.getComponentType());
  }

  @Test
  void returnSimpleArrayOfArray() throws Exception {
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
  void returnSimpleTypeVar() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectTypeVar");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Calculator.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertTrue(paramType.getActualTypeArguments()[0] instanceof WildcardType);
  }

  @Test
  void returnLv1Class() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(String.class, result);
  }

  @Test
  void returnLv2CustomClass() throws Exception {
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
  void returnLv2CustomClassList() throws Exception {
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
  void returnLv0InnerClass() throws Exception {
    Class<?> clazz = Level0InnerMapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Float.class, result);
  }

  @Test
  void returnLv2Class() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(String.class, result);
  }

  @Test
  void returnLv1List() throws Exception {
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
  void returnLv1Array() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectArray", List[].class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertEquals(String.class, resultClass.getComponentType());
  }

  @Test
  void returnLv2ArrayOfArray() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectArrayOfArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertTrue(resultClass.getComponentType().isArray());
    assertEquals(String.class, resultClass.getComponentType().getComponentType());
  }

  @Test
  void returnLv2ArrayOfList() throws Exception {
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
  void returnLv2WildcardList() throws Exception {
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
  void returnLV1Map() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectMap");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Map.class, paramType.getRawType());
    assertEquals(2, paramType.getActualTypeArguments().length);
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
    assertEquals(Object.class, paramType.getActualTypeArguments()[1]);
  }

  @Test
  void returnLV2Map() throws Exception {
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

  @Test
  void returnSubclass() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Method method = clazz.getMethod("getId");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(String.class, result);
  }

  @Test
  void paramPrimitive() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("simpleSelectPrimitive", int.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertEquals(int.class, result[0]);
  }

  @Test
  void paramSimple() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectVoid", Integer.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertEquals(Integer.class, result[0]);
  }

  @Test
  void paramLv1Single() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertEquals(String.class, result[0]);
  }

  @Test
  void paramLv2Single() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertEquals(String.class, result[0]);
  }

  @Test
  void paramLv2Multiple() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectList", Object.class, Object.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(2, result.length);
    assertEquals(Integer.class, result[0]);
    assertEquals(String.class, result[1]);
  }

  @Test
  void paramLv2CustomClass() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculator", Calculator.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertTrue(result[0] instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result[0];
    assertEquals(Calculator.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  void paramLv1Array() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectArray", List[].class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertTrue(result[0] instanceof GenericArrayType);
    GenericArrayType genericArrayType = (GenericArrayType) result[0];
    assertTrue(genericArrayType.getGenericComponentType() instanceof ParameterizedType);
    assertEquals("java.util.List<java.lang.String>[]", genericArrayType.toString());
    ParameterizedType paramType = (ParameterizedType) genericArrayType.getGenericComponentType();
    assertEquals(List.class, paramType.getRawType());
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  void paramSubclass() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Method method = clazz.getMethod("setId", Object.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(String.class, result[0]);
  }

  @Test
  void paramGeneric() throws Exception {
    TypeReference<ParentIface<String>> type = new TypeReference<ParentIface<String>>() {
    };
    Method method = ParentIface.class.getMethod("m");
    Type result = TypeParameterResolver.resolveReturnType(method, type.getRawType());
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType parameterizedType = (ParameterizedType) result;
    assertEquals(List.class, parameterizedType.getRawType());
    Type[] typeArgs = parameterizedType.getActualTypeArguments();
    assertEquals(1, typeArgs.length);
    assertEquals(String.class, typeArgs[0]);
  }

  @Test
  void returnAnonymous() throws Exception {
    Calculator<?> instance = new Calculator<Integer>();
    Class<?> clazz = instance.getClass();
    Method method = clazz.getMethod("getId");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Object.class, result);
  }

  @Test
  void fieldGenericField() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Class<?> declaredClass = Calculator.class;
    Field field = declaredClass.getDeclaredField("fld");
    Type result = TypeParameterResolver.resolveFieldType(field, clazz);
    assertEquals(String.class, result);
  }

  @Test
  void returnParamWildcardWithUpperBounds() throws Exception {
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
    Method getter1 = clazz.getMethod("getKey1");
    assertEquals(Key.class, TypeParameterResolver.resolveReturnType(getter1, clazz));
    Method setter1 = clazz.getMethod("setKey1", Key.class);
    assertEquals(Key.class, TypeParameterResolver.resolveParamTypes(setter1, clazz)[0]);
    Method getter2 = clazz.getMethod("getKey2");
    assertEquals(Key.class, TypeParameterResolver.resolveReturnType(getter2, clazz));
    Method setter2 = clazz.getMethod("setKey2", Key.class);
    assertEquals(Key.class, TypeParameterResolver.resolveParamTypes(setter2, clazz)[0]);
  }

  @Test
  void deepHierarchy() throws Exception {
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
    Method method = clazz.getMethod("getId");
    assertEquals(Integer.class, TypeParameterResolver.resolveReturnType(method, clazz));
    Field field = A.class.getDeclaredField("id");
    assertEquals(Integer.class, TypeParameterResolver.resolveFieldType(field, clazz));
  }

  @Test
  void shouldTypeVariablesBeComparedWithEquals() throws Exception {
    // #1794
    ExecutorService executor = Executors.newFixedThreadPool(2);
    Future<Type> futureA = executor.submit(() -> {
      Type retType = TypeParameterResolver.resolveReturnType(IfaceA.class.getMethods()[0], IfaceA.class);
      return ((ParameterizedType) retType).getActualTypeArguments()[0];
    });
    Future<Type> futureB = executor.submit(() -> {
      Type retType = TypeParameterResolver.resolveReturnType(IfaceB.class.getMethods()[0], IfaceB.class);
      return ((ParameterizedType) retType).getActualTypeArguments()[0];
    });
    assertEquals(AA.class, futureA.get());
    assertEquals(BB.class, futureB.get());
    executor.shutdown();
  }

  @Test
  void shouldResolveInterfaceTypeParams() {
    Type[] types = TypeParameterResolver.resolveClassTypeParams(ParentIface.class, IntegerHandler.class);
    assertEquals(1, types.length);
    assertEquals(Integer.class, types[0]);
  }

  @Test
  void shouldResolveInterfaceTypeParamsDeep() {
    Type[] types = TypeParameterResolver.resolveClassTypeParams(ParentIface.class, StringHandler.class);
    assertEquals(1, types.length);
    assertEquals(String.class, types[0]);
  }

  @Test
  void shouldResolveInterfaceTypeParamsDeeper() {
    Type[] types = TypeParameterResolver.resolveClassTypeParams(ParentIface.class, CustomStringHandler3.class);
    assertEquals(1, types.length);
    assertEquals(String.class, types[0]);
  }

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

  abstract class BaseHandler<T> implements ParentIface<T> {
  }

  class StringHandler extends BaseHandler<String> {
    public List<String> m() {
      return null;
    }
  }

  class CustomStringHandler extends StringHandler {
  }

  class CustomStringHandler2<T> extends CustomStringHandler {
  }

  class CustomStringHandler3 extends CustomStringHandler2<Integer> {
  }

  class IntegerHandler implements Cloneable, ParentIface<Integer> {
    public List<Integer> m() {
      return null;
    }
  }

  @Test
  void shouldParameterizedTypesWithOwnerTypeBeEqual() throws Exception {
    class Clazz {
      @SuppressWarnings("unused")
      public Entry<String, Integer> entry() {
        return null;
      }
    }

    Type typeJdk = Clazz.class.getMethod("entry").getGenericReturnType();

    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectEntry");
    Type typeMybatis = TypeParameterResolver.resolveReturnType(method, clazz);

    assertTrue(
        typeJdk instanceof ParameterizedType && !(typeJdk instanceof TypeParameterResolver.ParameterizedTypeImpl));
    assertTrue(typeMybatis instanceof TypeParameterResolver.ParameterizedTypeImpl);
    assertTrue(typeJdk.equals(typeMybatis));
    assertTrue(typeMybatis.equals(typeJdk));
  }

  @Test
  void shouldWildcardTypeBeEqual() throws Exception {
    class WildcardTypeTester {
      @SuppressWarnings("unused")
      public List<? extends Serializable> foo() {
        return null;
      }
    }

    Class<?> clazz = WildcardTypeTester.class;
    Method foo = clazz.getMethod("foo");
    Type typeMybatis = TypeParameterResolver.resolveReturnType(foo, clazz);
    Type typeJdk = foo.getGenericReturnType();

    Type wildcardMybatis = ((ParameterizedType) typeMybatis).getActualTypeArguments()[0];
    Type wildcardJdk = ((ParameterizedType) typeJdk).getActualTypeArguments()[0];

    assertTrue(wildcardJdk instanceof WildcardType && !(wildcardJdk instanceof TypeParameterResolver.WildcardTypeImpl));
    assertTrue(wildcardMybatis instanceof TypeParameterResolver.WildcardTypeImpl);
    assertTrue(typeJdk.equals(typeMybatis));
    assertTrue(typeMybatis.equals(typeJdk));
  }

  @Test
  void shouldGenericArrayTypeBeEqual() throws Exception {
    class GenericArrayTypeTester {
      @SuppressWarnings("unused")
      public List<String>[] foo() {
        return null;
      }
    }

    Class<?> clazz = GenericArrayTypeTester.class;
    Method foo = clazz.getMethod("foo");
    Type typeMybatis = TypeParameterResolver.resolveReturnType(foo, clazz);
    Type typeJdk = foo.getGenericReturnType();

    assertTrue(typeJdk instanceof GenericArrayType && !(typeJdk instanceof TypeParameterResolver.GenericArrayTypeImpl));
    assertTrue(typeMybatis instanceof TypeParameterResolver.GenericArrayTypeImpl);
    assertTrue(typeJdk.equals(typeMybatis));
    assertTrue(typeMybatis.equals(typeJdk));
  }

  @Test
  void shouldNestedParamTypeToStringOmitCommonFqn() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectMapEntry");
    Type type = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals("java.util.Map<java.util.Map$Entry<java.lang.String, java.lang.Integer>, java.util.Date>",
        type.toString());
  }

  static class Outer<T> {

    class Inner {
    }

    public Inner foo() {
      return null;
    }

  }

  static class InnerTester {

    public Outer<?>.Inner noTypeOuter() {
      return null;
    }

    public Outer<String>.Inner stringTypeOuter() {
      return null;
    }

  }

  @Test
  void shouldToStringHandleInnerClass() throws Exception {
    Class<?> outerClass = Outer.class;
    Class<?> innerTesterClass = InnerTester.class;
    Method foo = outerClass.getMethod("foo");
    Method noTypeOuter = innerTesterClass.getMethod("noTypeOuter");
    Method stringTypeOuter = innerTesterClass.getMethod("stringTypeOuter");

    Type fooReturnType = TypeParameterResolver.resolveReturnType(foo, outerClass);
    Type noTypeOuterReturnType = TypeParameterResolver.resolveReturnType(noTypeOuter, innerTesterClass);
    Type stringTypeOuterReturnType = TypeParameterResolver.resolveReturnType(stringTypeOuter, innerTesterClass);

    assertEquals("org.apache.ibatis.reflection.TypeParameterResolverTest$Outer<T>$Inner", fooReturnType.toString());
    assertEquals("org.apache.ibatis.reflection.TypeParameterResolverTest$Outer<?>$Inner",
        noTypeOuterReturnType.toString());
    assertEquals("org.apache.ibatis.reflection.TypeParameterResolverTest$Outer<java.lang.String>$Inner",
        stringTypeOuterReturnType.toString());
  }

}
