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
package org.apache.ibatis.executor.loader.cglib;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.ibatis.executor.loader.AbstractEnhancedDeserializationProxy;
import org.apache.ibatis.executor.loader.AbstractSerialStateHolder;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.reflection.factory.ObjectFactory;

final class CglibDeserializationProxy extends AbstractEnhancedDeserializationProxy implements MethodInterceptor {

  private final Object target;

  public CglibDeserializationProxy(Object target, Class<?>[] ctorTypes, Object[] ctorArgs, ObjectFactory factory, Map<String, ResultLoaderMap.LoadPair> unloaded) {
    super(target.getClass(), ctorTypes, ctorArgs, factory, unloaded);
    this.target = target;
  }

  @Override
  protected AbstractSerialStateHolder newSerialStateHolder(Object enhanced, Object userBean, Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
          ObjectFactory objectFactory, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    return new CglibSerialStateHolder(enhanced, userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
  }

  @Override
  public Object intercept(final Object enhanced, final Method method, final Object[] args, final MethodProxy methodProxy) throws Throwable {
    final Object o = super.invoke(this.target, method, args);
    if (o instanceof AbstractSerialStateHolder) {
      return o;
    } else {
      return methodProxy.invoke(this.target, args);
    }
  }

}
