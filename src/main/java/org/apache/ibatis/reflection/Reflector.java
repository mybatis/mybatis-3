/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.reflection;

import java.lang.reflect.Constructor;

import org.apache.ibatis.reflection.invoker.Invoker;

/**
 * This interface represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 */
public interface Reflector {
  /*
     * Gets the name of the class the instance provides information for
     *
     * @return The class name
     */
  Class<?> getType();

  Constructor<?> getDefaultConstructor();

  boolean hasDefaultConstructor();

  Invoker getSetInvoker(String propertyName);

  Invoker getGetInvoker(String propertyName);

  /*
     * Gets the type for a property setter
     *
     * @param propertyName - the name of the property
     * @return The Class of the propery setter
     */
  Class<?> getSetterType(String propertyName);

  /*
     * Gets the type for a property getter
     *
     * @param propertyName - the name of the property
     * @return The Class of the propery getter
     */
  Class<?> getGetterType(String propertyName);

  /*
     * Gets an array of the readable properties for an object
     *
     * @return The array
     */
  String[] getGetablePropertyNames();

  /*
     * Gets an array of the writeable properties for an object
     *
     * @return The array
     */
  String[] getSetablePropertyNames();

  /*
     * Check to see if a class has a writeable property by name
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a writeable property by the name
     */
  boolean hasSetter(String propertyName);

  /*
     * Check to see if a class has a readable property by name
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a readable property by the name
     */
  boolean hasGetter(String propertyName);

  String findPropertyName(String name);
}
