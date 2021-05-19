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
package org.apache.ibatis.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that conditional mapping definition for {@link TypeDiscriminator}.
 *
 * @see TypeDiscriminator
 * @see Result
 * @see Arg
 * @see Results
 * @see ConstructorArgs
 * @author Clinton Begin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Case {

  /**
   * Return the condition value to apply this mapping.
   *
   * @return the condition value
   */
  String value();

  /**
   * Return the object type that create a object using this mapping.
   *
   * @return the object type
   */
  Class<?> type();

  /**
   * Return mapping definitions for property.
   *
   * @return mapping definitions for property
   */
  Result[] results() default {};

  /**
   * Return mapping definitions for constructor.
   *
   * @return mapping definitions for constructor
   */
  Arg[] constructArgs() default {};

}
