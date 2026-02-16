/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.reflection.ReflectionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Clinton Begin
 */
public class Invocation {
  private static final Set<Class<?>> targetClasses = Set.of(
    Executor.class,
    ParameterHandler.class,
    ResultSetHandler.class,
    StatementHandler.class
  );
  private final Object target;
  private final Method method;
  private final Object[] args;
  private final MethodHandle methodHandle;

  public Invocation(Object target, Method method, Object[] args) {
    if (!targetClasses.contains(method.getDeclaringClass())) {
      throw new IllegalArgumentException("Method '" + method + "' is not supported as a plugin target.");
    }
    this.target = target;
    this.method = method;
    this.args = args;
    this.methodHandle = ReflectionUtil.getMethodHandle(method);
  }

  public Object getTarget() {
    return target;
  }

  public Method getMethod() {
    return method;
  }

  public Object[] getArgs() {
    return args;
  }

  public Object proceed() throws Throwable {
    if (methodHandle != null) {
      return ReflectionUtil.invoke(methodHandle, target, args);
    } else {
      return method.invoke(target, args);
    }
  }

}
