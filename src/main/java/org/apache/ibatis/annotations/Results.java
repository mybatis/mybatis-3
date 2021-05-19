/*
 *    Copyright 2009-2020 the original author or authors.
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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that be grouping mapping definitions for property.
 *
 * <p>
 * <b>How to use:</b>
 *
 * <pre>
 * public interface UserMapper {
 *   &#064;Results({
 *     &#064;Result(property = "id", column = "id", id = true),
 *     &#064;Result(property = "name", column = "name"),
 *     &#064;Result(property = "email" column = "id", one = @One(select = "selectUserEmailById", fetchType = FetchType.LAZY)),
 *     &#064;Result(property = "telephoneNumbers" column = "id", many = @Many(select = "selectAllUserTelephoneNumberById", fetchType = FetchType.LAZY))
 *   })
 *   &#064;Select("SELECT id, name FROM users WHERE id = #{id}")
 *   User selectById(int id);
 * }
 * </pre>
 *
 * @author Clinton Begin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Results {
  /**
   * Returns the id of this result map.
   *
   * @return the id of this result map
   */
  String id() default "";

  /**
   * Returns mapping definitions for property.
   *
   * @return mapping definitions
   */
  Result[] value() default {};
}
