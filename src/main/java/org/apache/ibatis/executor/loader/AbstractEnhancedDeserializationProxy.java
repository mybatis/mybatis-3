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
import java.util.Map;
import org.apache.ibatis.executor.ExecutorException;

import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;

/**
 * @author Clinton Begin
 */
public abstract class AbstractEnhancedDeserializationProxy {

  protected static final String FINALIZE_METHOD = "finalize";
  protected static final String WRITE_REPLACE_METHOD = "writeReplace";
  private Class<?> type;
  private Map<String, ResultLoaderMap.LoadPair> unloadedProperties;
  private ObjectFactory objectFactory;
  private List<Class<?>> constructorArgTypes;
  private List<Object> constructorArgs;
  private final Object reloadingPropertyLock;
  private boolean reloadingProperty;

  protected AbstractEnhancedDeserializationProxy(Class<?> type, Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
          ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    this.type = type;
    this.unloadedProperties = unloadedProperties;
    this.objectFactory = objectFactory;
    this.constructorArgTypes = constructorArgTypes;
    this.constructorArgs = constructorArgs;
    this.reloadingPropertyLock = new Object();
    this.reloadingProperty = false;
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
        synchronized (this.reloadingPropertyLock) {
          if (!FINALIZE_METHOD.equals(methodName) && PropertyNamer.isProperty(methodName) && !reloadingProperty) {
            final String property = PropertyNamer.methodToProperty(methodName);
            final String propertyKey = property.toUpperCase(Locale.ENGLISH);
            if (unloadedProperties.containsKey(propertyKey)) {
              final ResultLoaderMap.LoadPair loadPair = unloadedProperties.remove(propertyKey);
              if (loadPair != null) {
                try {
                  reloadingProperty = true;
                  loadPair.load(enhanced);
                } finally {
                  reloadingProperty = false;
                }
              } else {
                /* I'm not sure if this case can really happen or is just in tests -
                 * we have an unread property but no loadPair to load it. */
                throw new ExecutorException("An attempt has been made to read a not loaded lazy property '"
                        + property + "' of a disconnected object");
              }
            }
          }

          return enhanced;
        }
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
  }

  protected abstract AbstractSerialStateHolder newSerialStateHolder(
          Object userBean,
          Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
          ObjectFactory objectFactory,
          List<Class<?>> constructorArgTypes,
          List<Object> constructorArgs);

}
