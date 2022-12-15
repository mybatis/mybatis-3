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

import java.util.Map;

import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.type.ConstantPropertiesDescriptor;
import org.apache.ibatis.reflection.type.ResolvedType;

/**
 * @author Clinton Begin
 */
public class MetaClass {

  private final ReflectorFactory reflectorFactory;
  private final Reflector reflector;
  private final PropertiesDescriptor propertiesDescriptor;

  private MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
    this(reflectorFactory.getResolvedTypeFactory().constructType(type), reflectorFactory);
  }

  private MetaClass(ResolvedType type, ReflectorFactory reflectorFactory) {
    this.reflectorFactory = reflectorFactory;
    this.reflector = reflectorFactory.findForType(type);
    if (type instanceof PropertiesDescriptor) {
      this.propertiesDescriptor = (PropertiesDescriptor) type;
    } else if (type.isTypeOrSubTypeOf(Map.class)) {
      this.propertiesDescriptor = ConstantPropertiesDescriptor.forContentType(type);
    } else {
      this.propertiesDescriptor = reflector;
    }
  }

  public static MetaClass forClass(Class<?> type, ReflectorFactory reflectorFactory) {
    return new MetaClass(type, reflectorFactory);
  }

  public static MetaClass forClass(ResolvedType type, ReflectorFactory reflectorFactory) {
    return new MetaClass(type, reflectorFactory);
  }

  public MetaClass metaClassForProperty(String name) {
    ResolvedType propType = propertiesDescriptor.getGetterResolvedType(name);
    return MetaClass.forClass(propType, reflectorFactory);
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
    return getSetterResolvedType(name).getRawClass();
  }

  public ResolvedType getSetterResolvedType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop.getName());
      return metaProp.getSetterResolvedType(prop.getChildren());
    } else {
      return reflector.getSetterResolvedType(prop.getName());
    }
  }

  public Class<?> getGetterType(String name) {
    return getGetterResolvedType(name).getRawClass();
  }

  public ResolvedType getGetterResolvedType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop);
      return metaProp.getGetterResolvedType(prop.getChildren());
    }
    // issue #506. Resolve the type inside a Collection Object
    return getGetterResolvedType(prop);
  }

  private MetaClass metaClassForProperty(PropertyTokenizer prop) {
    ResolvedType propType = getGetterResolvedType(prop);
    return MetaClass.forClass(propType, reflectorFactory);
  }

  private ResolvedType getGetterResolvedType(PropertyTokenizer prop) {
    ResolvedType resolvedType = propertiesDescriptor.getGetterResolvedType(prop.getName());
    if (prop.getIndex() != null) {
      ResolvedType contentType = resolvedType.getContentType();
      if (contentType == null) {
        throw new ReflectionException("Cannot get collection content type of '" + resolvedType.toCanonical() + "'");
      }
      return contentType;
    }
    return resolvedType;
  }

  public boolean hasSetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (reflector.hasSetter(prop.getName())) {
        MetaClass metaProp = metaClassForProperty(prop.getName());
        return metaProp.hasSetter(prop.getChildren());
      } else {
        return false;
      }
    } else {
      return reflector.hasSetter(prop.getName());
    }
  }

  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (propertiesDescriptor.hasGetter(prop.getName())) {
        MetaClass metaProp = metaClassForProperty(prop);
        return metaProp.hasGetter(prop.getChildren());
      } else {
        return false;
      }
    } else {
      return propertiesDescriptor.hasGetter(prop.getName());
    }
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
