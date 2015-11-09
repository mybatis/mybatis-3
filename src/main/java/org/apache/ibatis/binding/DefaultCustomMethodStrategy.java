/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.binding;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * Default implementation class of {@link CustomMethodStrategy}.
 * <p>
 * Implements a custom method at default implementation class("FQCN of Mapper interface" + "Impl") or
 * user defined implementation class which specify using annotation attributes
 * ({@link org.apache.ibatis.annotations.CustomMethod#type} and {@link org.apache.ibatis.annotations.CustomMethod#method}).<br>
 * Note that first argument needs to define {@link org.apache.ibatis.session.SqlSession},
 * and subsequent arguments needs to define same signatures with mapper interface.
 *
 * @author Kazuki Shimizu
 * @since 3.4.0
 */
public class DefaultCustomMethodStrategy implements CustomMethodStrategy {

  /**
   * {@inheritDoc}
   */
  @Override
  public Object lookupTargetObject(Class<?> mapperInterface, Class<?> customType) throws Exception {
    Class<?> targetType;
    if (customType == null) {
      targetType = Resources.classForName(generateDefaultImplementationClassName(mapperInterface));
    } else {
      targetType = customType;
    }
    return targetType.getConstructor().newInstance();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Method lookupTargetMethod(Object targetObject, String methodName, Class<?>[] mapperMethodArgTypes) throws Exception {
    int argsCount = mapperMethodArgTypes.length;
    Class<?>[] methodArgTypes = new Class<?>[argsCount + 1];
    methodArgTypes[0] = SqlSession.class;
    System.arraycopy(mapperMethodArgTypes, 0, methodArgTypes, 1, argsCount);
    return targetObject.getClass().getDeclaredMethod(methodName, methodArgTypes);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object invoke(Object targetObject, Method targetMethod, SqlSession sqlSession, Object[] mapperMethodArgs) throws Exception {
    Object[] methodArgs;
    if (mapperMethodArgs == null) {
      methodArgs = new Object[]{sqlSession};
    } else {
      methodArgs = new Object[mapperMethodArgs.length + 1];
      methodArgs[0] = sqlSession;
      System.arraycopy(mapperMethodArgs, 0, methodArgs, 1, mapperMethodArgs.length);
    }
    return targetMethod.invoke(targetObject, methodArgs);
  }

  /**
   * Generate default implementation class name (FQCN).
   * @param mapperInterface Mapper interface which define mapper method under execution
   * @return "FQCN of Mapper interface" + "Impl"
   *         (e.g. {@code com.example.PersonMapper} -&gt; {@code com.example.PersonMapperImpl})
   */
  protected String generateDefaultImplementationClassName(Class<?> mapperInterface) {
    return mapperInterface.getName() + "Impl";
  }

}
