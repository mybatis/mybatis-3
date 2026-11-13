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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.ibatis.type.TypeException;

/** Preserves reflective types carried in SQL parameter mappings. */
public final class TypeDescriptor {
  private TypeDescriptor() {
  }

  public static Type parse(String description, Function<String, Class<?>> classResolver) {
    Parser parser = new Parser(description, classResolver);
    Type type = parser.type();
    parser.whitespace();
    if (parser.position != description.length()) {
      throw parser.invalid();
    }
    return type;
  }

  public static Class<?> rawType(Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    }
    if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    }
    if (type instanceof GenericArrayType) {
      return Array.newInstance(rawType(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
    }
    if (type instanceof WildcardType) {
      return rawType(((WildcardType) type).getUpperBounds()[0]);
    }
    throw new TypeException("Unresolved Java type: " + type.getTypeName());
  }

  public static void requireResolved(Type type, String location) {
    if (type instanceof TypeVariable<?>) {
      throw new TypeException("Unresolved type variable '" + type.getTypeName() + "' in " + location);
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) type;
      if (parameterized.getOwnerType() != null) {
        requireResolved(parameterized.getOwnerType(), location);
      }
      for (Type argument : parameterized.getActualTypeArguments()) {
        requireResolved(argument, location);
      }
    } else if (type instanceof GenericArrayType) {
      requireResolved(((GenericArrayType) type).getGenericComponentType(), location);
    } else if (type instanceof WildcardType) {
      for (Type bound : ((WildcardType) type).getUpperBounds()) {
        requireResolved(bound, location);
      }
      for (Type bound : ((WildcardType) type).getLowerBounds()) {
        requireResolved(bound, location);
      }
    }
  }

  /** Resolves member type arguments while retaining variables that have no concrete binding. */
  public static Type resolveMemberType(Type declaration, Type context, Class<?> declaringClass) {
    Map<TypeVariable<?>, Type> bindings = new HashMap<>();
    collectBindings(context, bindings);
    Class<?> raw = rawType(context);
    if (raw == declaringClass) {
      return substitute(declaration, bindings);
    }
    Type parent = raw.getGenericSuperclass();
    if (parent != null && declaringClass.isAssignableFrom(rawType(parent))) {
      return resolveMemberType(declaration, substitute(parent, bindings), declaringClass);
    }
    for (Type candidate : raw.getGenericInterfaces()) {
      if (declaringClass.isAssignableFrom(rawType(candidate))) {
        return resolveMemberType(declaration, substitute(candidate, bindings), declaringClass);
      }
    }
    return declaration;
  }

  private static void collectBindings(Type context, Map<TypeVariable<?>, Type> bindings) {
    if (context instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) context;
      if (parameterized.getOwnerType() != null) {
        collectBindings(parameterized.getOwnerType(), bindings);
      }
      TypeVariable<?>[] variables = rawType(context).getTypeParameters();
      Type[] arguments = parameterized.getActualTypeArguments();
      for (int index = 0; index < variables.length; index++) {
        bindings.put(variables[index], substitute(arguments[index], bindings));
      }
    }
  }

  private static Type substitute(Type type, Map<TypeVariable<?>, Type> bindings) {
    if (type instanceof TypeVariable<?>) {
      return bindings.getOrDefault(type, type);
    }
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) type;
      Type owner = parameterized.getOwnerType();
      return new TypeParameterResolver.ParameterizedTypeImpl((Class<?>) parameterized.getRawType(),
          owner == null ? null : substitute(owner, bindings),
          substitute(parameterized.getActualTypeArguments(), bindings));
    }
    if (type instanceof GenericArrayType) {
      Type component = substitute(((GenericArrayType) type).getGenericComponentType(), bindings);
      return component instanceof Class<?> ? Array.newInstance((Class<?>) component, 0).getClass()
          : new TypeParameterResolver.GenericArrayTypeImpl(component);
    }
    if (type instanceof WildcardType) {
      WildcardType wildcard = (WildcardType) type;
      return new TypeParameterResolver.WildcardTypeImpl(substitute(wildcard.getLowerBounds(), bindings),
          substitute(wildcard.getUpperBounds(), bindings));
    }
    return type;
  }

  private static Type[] substitute(Type[] types, Map<TypeVariable<?>, Type> bindings) {
    Type[] result = new Type[types.length];
    for (int index = 0; index < types.length; index++) {
      result[index] = substitute(types[index], bindings);
    }
    return result;
  }

  private static final class Parser {
    private final String description;
    private final Function<String, Class<?>> classResolver;
    private int position;

    private Parser(String description, Function<String, Class<?>> classResolver) {
      this.description = description;
      this.classResolver = classResolver;
    }

    private Type type() {
      whitespace();
      Type result;
      if (take("?")) {
        if (take("extends ")) {
          result = new TypeParameterResolver.WildcardTypeImpl(new Type[0], new Type[] { type() });
        } else if (take("super ")) {
          result = new TypeParameterResolver.WildcardTypeImpl(new Type[] { type() }, new Type[] { Object.class });
        } else {
          result = new TypeParameterResolver.WildcardTypeImpl(new Type[0], new Type[] { Object.class });
        }
      } else {
        Class<?> raw = resolveClass(name());
        result = parameterize(raw, raw.getDeclaringClass());
        while (take("$")) {
          raw = classResolver.apply(raw.getName() + "$" + name());
          result = parameterize(raw, result);
        }
      }
      while (take("[]")) {
        result = result instanceof Class<?> ? Array.newInstance((Class<?>) result, 0).getClass()
            : new TypeParameterResolver.GenericArrayTypeImpl(result);
      }
      return result;
    }

    private Class<?> resolveClass(String name) {
      switch (name) {
        case "boolean":
          return boolean.class;
        case "byte":
          return byte.class;
        case "short":
          return short.class;
        case "int":
          return int.class;
        case "long":
          return long.class;
        case "float":
          return float.class;
        case "double":
          return double.class;
        case "char":
          return char.class;
        default:
          return classResolver.apply(name);
      }
    }

    private Type parameterize(Class<?> raw, Type owner) {
      if (!take("<")) {
        return owner instanceof ParameterizedType
            ? new TypeParameterResolver.ParameterizedTypeImpl(raw, owner, new Type[0]) : raw;
      }
      ArrayList<Type> arguments = new ArrayList<>();
      do {
        Type argument = type();
        if (argument instanceof Class<?> && ((Class<?>) argument).isPrimitive()) {
          throw invalid();
        }
        arguments.add(argument);
      } while (take(","));
      if (!take(">") || arguments.size() != raw.getTypeParameters().length) {
        throw invalid();
      }
      return new TypeParameterResolver.ParameterizedTypeImpl(raw, owner, arguments.toArray(new Type[0]));
    }

    private String name() {
      whitespace();
      int start = position;
      while (position < description.length()) {
        char character = description.charAt(position);
        if (!Character.isJavaIdentifierPart(character) && character != '.') {
          break;
        }
        position++;
      }
      if (start == position) {
        throw invalid();
      }
      return description.substring(start, position);
    }

    private boolean take(String token) {
      whitespace();
      if (description.startsWith(token, position)) {
        position += token.length();
        return true;
      }
      return false;
    }

    private void whitespace() {
      while (position < description.length() && Character.isWhitespace(description.charAt(position))) {
        position++;
      }
    }

    private TypeException invalid() {
      return new TypeException("Invalid Java type '" + description + "' at position " + position);
    }
  }
}
