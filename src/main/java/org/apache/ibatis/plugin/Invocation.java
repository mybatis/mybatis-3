/*
 *    Copyright 2009-2022 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Clinton Begin
 */
public class Invocation {

  private final Object target;
  private final Method method;
  private final Object[] args;
  private final List<Interceptor> interceptors;
  private int index;

  public Invocation(Object target, Method method, Object[] args, List<Interceptor> interceptors) {
    this.target = target;
    this.method = method;
    this.args = args;
    this.interceptors = interceptors;
    index = interceptors.size();
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

    if ((index--) == 0) {
      return method.invoke(target, args);
    }

    Interceptor interceptor = interceptors.get(index);
    return interceptor.intercept(this);

  }

}
