/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.plugin;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that specify target methods to intercept.
 *
 * <b>How to use:</b>
 * <pre>
 * &#064;Intercepts({&#064;Signature(
 *   type= Executor.class,
 *   method = "update",
 *   args = {MappedStatement.class ,Object.class})})
 * public class ExamplePlugin implements Interceptor {
 *   &#064;Override
 *   public Object intercept(Invocation invocation) throws Throwable {
 *     // implement pre-processing if needed
 *     Object returnObject = invocation.proceed();
 *     // implement post-processing if needed
 *     return returnObject;
 *   }
 * }
 * </pre>
 * @author Clinton Begin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Intercepts {
  /**
   * Returns method signatures to intercept.
   *
   * @return method signatures
   */
  Signature[] value();
}

