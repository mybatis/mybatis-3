/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.reflection.invoker;

import org.apache.ibatis.reflection.ReflectionUtil;
import org.apache.ibatis.reflection.Reflector;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author Clinton Begin
 */
public class GetFieldInvoker implements Invoker {
  private final VarHandle varHandle;
  private final Field field;
  private final boolean isStatic;
  private final Class<?> type;

  public GetFieldInvoker(Field field) {
    this.type = field.getType();
    this.isStatic = Modifier.isStatic(field.getModifiers());

    Class<?> fieldClass = field.getDeclaringClass();
    if (ReflectionUtil.jdk8Class(fieldClass)) {
      // JDK internal class (e.g., java.lang.Integer) → use reflection with Field
      this.field = field;
      this.varHandle = null;
      // Use trySetAccessible() which handles both static and instance fields safely
      field.trySetAccessible();
    } else {
      // User's business class → use VarHandle for better performance
      this.field = null;
      try {
        this.varHandle = ReflectionUtil.getMethodHandlesLookup(fieldClass).unreflectVarHandle(field);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Cannot access field: " + field, e);
      }
    }
  }

  @Override
  public Object invoke(Object target, Object[] args) throws IllegalAccessException {
    if (varHandle != null) {
      return isStatic ? varHandle.get() : varHandle.get(target);
    } else {
      try {
        return field.get(target);
      } catch (IllegalAccessException e) {
        if (Reflector.canControlMemberAccessible()) {
          field.setAccessible(true);
          return field.get(target);
        }
        throw e;
      }
    }
  }

  @Override
  public Class<?> getType() {
    return type;
  }
}
