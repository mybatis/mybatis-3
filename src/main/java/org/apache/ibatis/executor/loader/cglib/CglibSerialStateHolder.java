/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.executor.loader.cglib;

import java.util.List;
import java.util.Map;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import org.apache.ibatis.executor.loader.AbstractSerialStateHolder;
import org.apache.ibatis.executor.loader.ResultLoaderMap;
import org.apache.ibatis.reflection.factory.ObjectFactory;

class CglibSerialStateHolder extends AbstractSerialStateHolder {

  private static final long serialVersionUID = 8940388717901644661L;

  public CglibSerialStateHolder() {
  }

  public CglibSerialStateHolder(
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
    return new CglibProxyFactory().createDeserializationProxy(target, ctorTypes, ctorArgs, factory, unloaded);
  }

  @Override
  protected Object newCyclicReferenceMarker(final Class<?> target, final Class<?>[] ctorTypes, final Object[] ctorArgs, final AbstractSerialStateHolder ssh) {
    final Enhancer enhancer = new Enhancer();
    enhancer.setCallback(new FixedValue() {

      @Override
      public Object loadObject() throws Exception {
        return ssh;
      }
    });
    enhancer.setSuperclass(target);
    enhancer.setInterfaces(new Class<?>[]{CyclicReferenceMarker.class});

    return enhancer.create(ctorTypes, ctorArgs);
  }

}
