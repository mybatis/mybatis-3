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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 */
public class MetaClass {

  private final ReflectorFactory reflectorFactory;
  private final Reflector reflector;

  private MetaClass(Type type, ReflectorFactory reflectorFactory) {
    this.reflectorFactory = reflectorFactory;
    this.reflector = reflectorFactory.findForClass(type);
  }

  public static MetaClass forClass(Type type, ReflectorFactory reflectorFactory) {
    return new MetaClass(type, reflectorFactory);
  }

  public MetaClass metaClassForProperty(String name) {
    Entry<Type, Class<?>> propertyType = reflector.getGenericGetterType(name);
    return metaClassForType(propertyType);
  }

  public String findProperty(String name) {
    StringBuilder prop = buildProperty(name, new StringBuilder());
    return prop.length() > 0 ? prop.toString() : null;
  }

  public String findProperty(String name, boolean useCamelCaseMapping) {
    if (useCamelCaseMapping) {
      name = name.replace("_", "");
    }
    return findProperty(name);
  }

  public String[] getGetterNames() {
    return reflector.getGetablePropertyNames();
  }

  public String[] getSetterNames() {
    return reflector.getSetablePropertyNames();
  }

  public Class<?> getSetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop.getName());
      return metaProp.getSetterType(prop.getChildren());
    }
    return reflector.getSetterType(prop.getName());
  }

  public Entry<Type, Class<?>> getGenericSetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop);
      return metaProp.getGenericSetterType(prop.getChildren());
    }
    return reflector.getGenericSetterType(prop.getName());
  }

  public Class<?> getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop);
      return metaProp.getGetterType(prop.getChildren());
    }
    return getGetterType(prop).getValue();
  }

  public Entry<Type, Class<?>> getGenericGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop);
      return metaProp.getGenericGetterType(prop.getChildren());
    }
    return getGetterType(prop);
  }

  private MetaClass metaClassForProperty(PropertyTokenizer prop) {
    return metaClassForType(getGetterType(prop));
  }

  private MetaClass metaClassForType(Entry<Type, Class<?>> propertyType) {
    Type type = navigableType(propertyType.getKey());
    return MetaClass.forClass(type instanceof ParameterizedType ? type : propertyType.getValue(), reflectorFactory);
  }

  private Entry<Type, Class<?>> getGetterType(PropertyTokenizer prop) {
    Entry<Type, Class<?>> pair = reflector.getGenericGetterType(prop.getName());
    if (prop.getIndex() == null) {
      return pair;
    }
    Type type = navigableType(pair.getKey());
    Type element;
    if (type instanceof GenericArrayType) {
      element = ((GenericArrayType) type).getGenericComponentType();
    } else if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
      element = ((Class<?>) type).getComponentType();
    } else if (type instanceof ParameterizedType && Collection.class.isAssignableFrom(pair.getValue())) {
      element = TypeParameterResolver.resolveMemberType(Collection.class.getTypeParameters()[0], type,
          Collection.class);
    } else if (type instanceof ParameterizedType && Map.class.isAssignableFrom(pair.getValue())) {
      element = TypeParameterResolver.resolveMemberType(Map.class.getTypeParameters()[1], type, Map.class);
    } else {
      return pair;
    }
    return Map.entry(element, TypeParameterResolver.rawType(navigableType(element)));
  }

  private Type navigableType(Type type) {
    if (type instanceof WildcardType) {
      return navigableType(((WildcardType) type).getUpperBounds()[0]);
    }
    if (type instanceof TypeVariable<?>) {
      return navigableType(((TypeVariable<?>) type).getBounds()[0]);
    }
    return type;
  }

  public boolean hasSetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (!prop.hasNext()) {
      return reflector.hasSetter(prop.getName());
    }
    if (reflector.hasSetter(prop.getName())) {
      MetaClass metaProp = metaClassForProperty(prop.getName());
      return metaProp.hasSetter(prop.getChildren());
    }
    return false;
  }

  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (!prop.hasNext()) {
      return reflector.hasGetter(prop.getName());
    }
    if (reflector.hasGetter(prop.getName())) {
      MetaClass metaProp = metaClassForProperty(prop);
      return metaProp.hasGetter(prop.getChildren());
    }
    return false;
  }

  public Invoker getGetInvoker(String name) {
    return reflector.getGetInvoker(name);
  }

  public Invoker getSetInvoker(String name) {
    return reflector.getSetInvoker(name);
  }

  private StringBuilder buildProperty(String name, StringBuilder builder) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      String propertyName = reflector.findPropertyName(prop.getName());
      if (propertyName != null) {
        builder.append(propertyName);
        builder.append(".");
        MetaClass metaProp = metaClassForProperty(propertyName);
        metaProp.buildProperty(prop.getChildren(), builder);
      }
    } else {
      String propertyName = reflector.findPropertyName(name);
      if (propertyName != null) {
        builder.append(propertyName);
      }
    }
    return builder;
  }

  public boolean hasDefaultConstructor() {
    return reflector.hasDefaultConstructor();
  }

}
