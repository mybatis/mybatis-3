/*
 *    Copyright 2009-2013 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.loader.javassist;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.apache.ibatis.executor.loader.AbstractSerialStateHolder;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.reflection.factory.ObjectFactory;

class JavassistSerialStateHolder extends AbstractSerialStateHolder {

  private static final long serialVersionUID = 8940388717901644661L;

  public JavassistSerialStateHolder() {
  }

  public JavassistSerialStateHolder(
          final Object enhanced,
          final Object userBean,
          final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
          final ObjectFactory objectFactory,
          List<Class<?>> constructorArgTypes,
          List<Object> constructorArgs) {
    super(enhanced, userBean, unloadedProperties, objectFactory, constructorArgTypes, constructorArgs);
  }

  @Override
  protected Object newDeserializationProxy(Object target, Class<?>[] ctorTypes, Object[] ctorArgs, ObjectFactory factory, Map<String, ResultLoaderMap.LoadPair> unloaded) {
    return new JavassistProxyFactory().createDeserializationProxy(target, ctorTypes, ctorArgs, factory, unloaded);
  }

  @Override
  protected Object newCyclicReferenceMarker(final Class<?> target, final Class<?>[] ctorTypes, final Object[] ctorArgs, final AbstractSerialStateHolder ssh) {
    final ProxyFactory enhancer = new ProxyFactory();
    enhancer.setSuperclass(target);
    enhancer.setInterfaces(new Class<?>[]{CyclicReferenceMarker.class});

    final Proxy enhanced;
    try {
      enhanced = (Proxy) enhancer.create(ctorTypes, ctorArgs);
    } catch (final Exception ex) {
      throw new IllegalStateException(ex);
    }

    enhanced.setHandler(new MethodHandler() {

      @Override
      public Object invoke(Object o, Method method, Method method1, Object[] os) throws Throwable {
        return ssh;
      }
    });

    return enhanced;
  }

}
