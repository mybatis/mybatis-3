/*
 *    Copyright 2009-2026 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeReference;
import org.junit.jupiter.api.Test;

class TypeDescriptorTest {
  private Class<?> resolve(String name) {
    try {
      return Resources.classForName(name);
    } catch (ClassNotFoundException exception) {
      throw new TypeException("Unresolved Java type: " + name, exception);
    }
  }

  @Test
  void roundTripsNestedTypesWildcardsArraysAndOwners() {
    Type[] types = { new TypeReference<List<String>>() {
    }.getRawType(), new TypeReference<Map<String, List<Integer>>>() {
    }.getRawType(), new TypeReference<List<? extends Number>>() {
    }.getRawType(), new TypeReference<List<? super Integer>>() {
    }.getRawType(), new TypeReference<List<?>>() {
    }.getRawType(), new TypeReference<List<String>[]>() {
    }.getRawType(), new TypeReference<Map.Entry<String, Integer>>() {
    }.getRawType(), new TypeReference<Owner<String>.Child<Integer>>() {
    }.getRawType(), new TypeReference<Owner<String>.Leaf>() {
    }.getRawType(), String[][].class };
    for (Type type : types) {
      Type parsed = TypeDescriptor.parse(type.getTypeName(), this::resolve);
      assertEquals(type, parsed, type.getTypeName());
      assertEquals(type.hashCode(), parsed.hashCode());
      TypeDescriptor.requireResolved(parsed, "test field");
    }
  }

  @Test
  void resolvesOwnerAndInnerTypeVariablesTogether() throws Exception {
    Type context = new TypeReference<Owner<String>.Child<Integer>>() {
    }.getRawType();
    Type declaration = Owner.Child.class.getDeclaredField("values").getGenericType();
    assertEquals(new TypeReference<Map<String, Integer>>() {
    }.getRawType(), TypeDescriptor.resolveMemberType(declaration, context, Owner.Child.class));
  }

  @Test
  void rejectsMalformedTypesAndWrongArity() {
    for (String description : List.of("java.util.List<>", "java.util.List<java.lang.String, java.lang.Integer>",
        "java.util.Map<java.lang.String>", "java.util.List<java.lang.String>>", "java.util.List<java.lang.String")) {
      assertThrows(TypeException.class, () -> TypeDescriptor.parse(description, this::resolve), description);
    }
  }

  @Test
  void reportsUnresolvedVariableAndItsLocation() throws Exception {
    Type type = Generic.class.getDeclaredField("values").getGenericType();
    TypeException exception = assertThrows(TypeException.class,
        () -> TypeDescriptor.requireResolved(type, "Generic.values"));
    assertTrue(exception.getMessage().contains("T"));
    assertTrue(exception.getMessage().contains("Generic.values"));
  }

  static class Owner<T> {
    class Child<U> {
      Map<T, U> values;
    }

    class Leaf {
    }
  }

  static class Generic<T> {
    List<T> values;
  }
}
