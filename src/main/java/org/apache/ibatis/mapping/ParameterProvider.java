/*
 *    Copyright 2009-2014 the original author or authors.
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
package org.apache.ibatis.mapping;

import org.apache.ibatis.builder.BuilderException;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Craig St. Jean
 */
public class ParameterProvider {
  private Class<?> parameterProvider;
  private String method;

  public ParameterProvider(Class<?> parameterProvider, String method) {
    this.parameterProvider = parameterProvider;
    this.method = method;
  }

  public Object invoke(
      Object nestedQueryParameterObject,
      Class<?> nestedQueryParameterType,
      List<Object> originalParameters) {
    try {
      Object provider = parameterProvider.newInstance();
      Method invokeMethod = parameterProvider.getDeclaredMethod(method, Object.class, Class.class, List.class);
      return invokeMethod.invoke(provider, nestedQueryParameterObject, nestedQueryParameterType, originalParameters);
    } catch (Exception e) {
      throw new BuilderException("Error invoking parameter provider method ("
          + parameterProvider.getName() + "." + method
          + ").  Cause: " + e, e);
    }
  }

  public Class<?> getParameterProvider() {
    return parameterProvider;
  }

  public String getMethod() {
    return method;
  }
}
