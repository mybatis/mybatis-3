/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.executor.loader;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.reflection.factory.ObjectFactory;

class JavassistSerialStateHolder implements Serializable {

  private static final long serialVersionUID = 9018585337519878124L;
  private Object userBean;
  private String[] unloadedProperties;
  private ObjectFactory objectFactory;
  private Class<?>[] constructorArgTypes;
  private Object[] constructorArgs;

  public JavassistSerialStateHolder(
      final Object userBean, 
      final Set<String> unloadedProperties,
      final ObjectFactory objectFactory, 
      List<Class<?>> constructorArgTypes, 
      List<Object> constructorArgs) {
    this.userBean = userBean;
    this.unloadedProperties = unloadedProperties.toArray(new String[unloadedProperties.size()]);
    this.objectFactory = objectFactory;
    this.constructorArgTypes = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
    this.constructorArgs = constructorArgs.toArray(new Object[constructorArgs.size()]);
  }

  protected Object readResolve() throws ObjectStreamException {
    Set<String> arrayProps = new HashSet<String>(Arrays.asList(this.unloadedProperties));
    List<Class<?>> arrayTypes = Arrays.asList(this.constructorArgTypes);
    List<Object> arrayValues = Arrays.asList(this.constructorArgs);
    return new JavassistProxyFactory().createDeserializationProxy(userBean, arrayProps, objectFactory, arrayTypes, arrayValues);
  }

}
