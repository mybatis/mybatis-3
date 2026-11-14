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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

import org.apache.ibatis.type.TypeException;

/** Parses Java type expressions carried in SQL parameter mappings. */
public final class TypeParser {
  private static final Map<String, Class<?>> PRIMITIVE_TYPES = Map.of("boolean", boolean.class, "byte", byte.class,
      "short", short.class, "int", int.class, "long", long.class, "float", float.class, "double", double.class, "char",
      char.class);

  private TypeParser() {
  }

  public static Type parse(String description, Function<String, Class<?>> classResolver) {
    Parser parser = new Parser(description, classResolver);
    Type type = parser.type(false);
    parser.whitespace();
    if (parser.position != description.length()) {
      throw parser.invalid();
    }
    return type;
  }

  private static final class Parser {
    private final String description;
    private final Function<String, Class<?>> classResolver;
    private int position;

    private Parser(String description, Function<String, Class<?>> classResolver) {
      this.description = description;
      this.classResolver = classResolver;
    }

    private Type type(boolean allowWildcard) {
      Type result;
      if (take("?")) {
        if (!allowWildcard) {
          throw invalid();
        }
        result = wildcard();
      } else {
        Class<?> raw = resolveClass(name());
        result = parameterize(raw, raw.getDeclaringClass());
        while (take("$")) {
          raw = classResolver.apply(raw.getName() + "$" + name());
          result = parameterize(raw, result);
        }
      }
      while (take("[]")) {
        if (result instanceof WildcardType) {
          throw invalid();
        }
        result = TypeParameterResolver.arrayType(result);
      }
      return result;
    }

    private Type wildcard() {
      if (keyword("extends")) {
        return new TypeParameterResolver.WildcardTypeImpl(new Type[0], new Type[] { bound() });
      }
      if (keyword("super")) {
        return new TypeParameterResolver.WildcardTypeImpl(new Type[] { bound() }, new Type[] { Object.class });
      }
      return new TypeParameterResolver.WildcardTypeImpl(new Type[0], new Type[] { Object.class });
    }

    private Type bound() {
      Type bound = type(false);
      if (bound instanceof Class<?> && ((Class<?>) bound).isPrimitive()) {
        throw invalid();
      }
      return bound;
    }

    private Class<?> resolveClass(String name) {
      Class<?> primitive = PRIMITIVE_TYPES.get(name);
      return primitive == null ? classResolver.apply(name) : primitive;
    }

    private Type parameterize(Class<?> raw, Type owner) {
      if (!take("<")) {
        return owner instanceof ParameterizedType
            ? new TypeParameterResolver.ParameterizedTypeImpl(raw, owner, new Type[0]) : raw;
      }
      ArrayList<Type> arguments = new ArrayList<>();
      do {
        Type argument = type(true);
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

    private boolean keyword(String token) {
      whitespace();
      int end = position + token.length();
      if (description.startsWith(token, position) && end < description.length()
          && Character.isWhitespace(description.charAt(end))) {
        position = end;
        return true;
      }
      return false;
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
