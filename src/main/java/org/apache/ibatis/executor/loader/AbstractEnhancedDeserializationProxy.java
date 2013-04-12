/*
 * Copyright 2013 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.executor.loader;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;

/**
 *
 * @author Franta Mejta
 * @sa.date 2013-04-12T14:58:19+0200
 */
public abstract class AbstractEnhancedDeserializationProxy {

  protected static final String FINALIZE_METHOD = "finalize";
  protected static final String WRITE_REPLACE_METHOD = "writeReplace";
  private Class<?> type;
  private Set<String> unloadedProperties;
  private ObjectFactory objectFactory;
  private List<Class<?>> constructorArgTypes;
  private List<Object> constructorArgs;

  protected AbstractEnhancedDeserializationProxy(Class<?> type, Set<String> unloadedProperties,
          ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    this.type = type;
    this.unloadedProperties = unloadedProperties;
    this.objectFactory = objectFactory;
    this.constructorArgTypes = constructorArgTypes;
    this.constructorArgs = constructorArgs;
  }

  public final Object invoke(Object enhanced, Method method, Object[] args) throws Throwable {
    final String methodName = method.getName();
    try {
      if (WRITE_REPLACE_METHOD.equals(methodName)) {
        final Object original;
        if (constructorArgTypes.isEmpty()) {
          original = objectFactory.create(type);
        } else {
          original = objectFactory.create(type, constructorArgTypes, constructorArgs);
        }

        PropertyCopier.copyBeanProperties(type, enhanced, original);
        return this.newSerialStateHolder(original, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
      } else {
        if (!FINALIZE_METHOD.equals(methodName) && PropertyNamer.isProperty(methodName)) {
          final String property = PropertyNamer.methodToProperty(methodName);
          if (unloadedProperties.contains(property.toUpperCase(Locale.ENGLISH))) {
            throw new ExecutorException("An attempt has been made to read a not loaded lazy property '"
                    + property
                    + "' of a disconnected object");
          }
        }

        return enhanced;
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  protected abstract AbstractSerialStateHolder newSerialStateHolder(
          Object userBean,
          Set<String> unloadedProperties,
          ObjectFactory objectFactory,
          List<Class<?>> constructorArgTypes,
          List<Object> constructorArgs);

}
