/**
 *    Copyright 2009-2019 the original author or authors.
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
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParamNameUtil {
  public static List<String> getParamNames(Method method) {
    return getParameterNames(method);
  }

  public static List<String> getParamNames(Constructor<?> constructor) {
    return getParameterNames(constructor);
  }

  private static List<String> getParameterNames(Executable executable) {
    return Arrays.stream(executable.getParameters()).map(Parameter::getName).collect(Collectors.toList());
  }

  private ParamNameUtil() {
    super();
  }
}
