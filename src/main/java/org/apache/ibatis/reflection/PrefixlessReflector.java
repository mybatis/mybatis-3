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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

class PrefixlessReflector extends DefaultReflector {

  private static final Set<String> NON_GETTERS = new HashSet<String>(asList(
      "toString", "hashCode", "getClass", "clone", "notify", "notifyAll", "wait", "finalize"
  ));

  private static final Set<String> NON_SETTERS = new HashSet<String>(asList(
      "equals", "wait"
  ));

  PrefixlessReflector(Class<?> clazz) {
    super(clazz);
  }

  @Override
  boolean maybeAddGetMethodConflict(Map<String, List<Method>> conflictingGetters, Method method) {
    if (method.getParameterTypes().length > 0) {
      return false;
    }
    boolean added = super.maybeAddGetMethodConflict(conflictingGetters, method);
    if (added) {
      return true;
    }
    String name = method.getName();
    if (NON_GETTERS.contains(name)) {
      return false;
    }
    addMethodConflict(conflictingGetters, name, method);
    return true;
  }

  @Override
  boolean maybeAddSetMethodConflict(Map<String, List<Method>> conflictingSetters, Method method) {
    if (method.getParameterTypes().length != 1) {
      return false;
    }
    boolean added = super.maybeAddSetMethodConflict(conflictingSetters, method);
    if (added) {
      return true;
    }
    String name = method.getName();
    if (NON_SETTERS.contains(name)) {
      return false;
    }
    addMethodConflict(conflictingSetters, name, method);
    return true;
  }
}
