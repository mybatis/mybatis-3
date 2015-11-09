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

import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * Strategy interface that invoke a custom method.
 * <p>
 * This interface provide three strategies.
 * <ul>
 *   <li>Method that lookup a target object which implements a custom method</li>
 *   <li>Method that lookup a custom method from the target object</li>
 *   <li>Method that invoke a custom method</li>
 * </ul>
 * @author Kazuki Shimizu
 * @since 3.4.0
 * @see DefaultCustomMethodStrategy
 */
public interface CustomMethodStrategy {

  /**
   * Lookup a target object which implements a custom method.
   *
   * @param mapperInterface Mapper interface which define mapper method under execution
   * @param customType Custom type which specified by developer using {@link org.apache.ibatis.annotations.CustomMethod#type} (Default is {@code null})
   * @return A target object
   * @throws Exception Target object not found
   */
  Object lookupTargetObject(Class<?> mapperInterface, Class<?> customType) throws Exception;

  /**
   * Lookup a custom method from the target object.
   *
   * @param targetObject A target object which implements a custom method
   * @param methodName Method name of custom method (Default is same with mapper method name,
   *                   it can customize using {@link org.apache.ibatis.annotations.CustomMethod#method})
   * @param mapperMethodArgTypes Method argument types of mapper method under execution
   * @return A custom method
   * @throws Exception Target method not found
   */
  Method lookupTargetMethod(Object targetObject, String methodName, Class<?>[] mapperMethodArgTypes) throws Exception;

  /**
   * Invoke a custom method.
   *
   * @param targetObject A target object which implements a custom method
   * @param targetMethod A custom method
   * @param sqlSession A {@link SqlSession} under execution
   * @param mapperMethodArgs Method argument values of mapper method under execution
   * @return Return value of custom method
   * @throws Exception Custom method invocation is failed, or an exception is occurred under a custom method execution
   */
  Object invoke(Object targetObject, Method targetMethod, SqlSession sqlSession, Object[] mapperMethodArgs) throws Exception;

}
