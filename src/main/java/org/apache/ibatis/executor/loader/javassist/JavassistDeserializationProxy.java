/*
 * Copyright 2014 MyBatis.org.
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
package org.apache.ibatis.executor.loader.javassist;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javassist.util.proxy.MethodHandler;
import org.apache.ibatis.executor.loader.AbstractEnhancedDeserializationProxy;
import org.apache.ibatis.executor.loader.AbstractSerialStateHolder;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.reflection.factory.ObjectFactory;

final class JavassistDeserializationProxy extends AbstractEnhancedDeserializationProxy implements MethodHandler {

  private final Object target;

  public JavassistDeserializationProxy(Object target, Class<?>[] ctorTypes, Object[] ctorArgs, ObjectFactory factory, Map<String, ResultLoaderMap.LoadPair> unloaded) {
    super(target.getClass(), ctorTypes, ctorArgs, factory, unloaded);
    this.target = target;
  }

  @Override
  protected AbstractSerialStateHolder newSerialStateHolder(Object enhanced, Object userBean, Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
          ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    return new JavassistSerialStateHolder(enhanced, userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
  }

  @Override
  public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
    final Object o = super.invoke(this.target, thisMethod, args);
    if (o instanceof AbstractSerialStateHolder) {
      return o;
    } else {
      return thisMethod.invoke(this.target, args);
    }
  }

}
