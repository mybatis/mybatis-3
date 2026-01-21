/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.annotations;

import static org.apache.ibatis.annotations.AnnotationConstants.NULL_TYPE_DISCRIMINATOR;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining standalone result maps.
 * <p>
 * A standalone result map can be used to define a result map that can be reused with multiple statements. The
 * standalone result map must be attached to an accessible static field in a mapper. The value of the field will be used
 * as the result map id.
 * </p>
 * <p>
 * <b>How to use:</b>
 *
 * <pre>
 * public interface UserMapper {
 *   &#064;StandaloneResultMap(javaType = User.class, propertyMappings = {
 *       &#064;Result(property = "id", column = "id", id = true),
 *       &#064;Result(property = "name", column = "name"),
 *       &#064;Result(property = "email" column = "id", one = @One(select = "selectUserEmailById", fetchType = FetchType.LAZY)),
 *       &#064;Result(property = "telephoneNumbers" column = "id", many = @Many(select = "selectAllUserTelephoneNumberById", fetchType = FetchType.LAZY))
 *   })
 *   String userResult = "userResult";
 *
 *   &#064;Select("SELECT id, name FROM users WHERE id = #{id}")
 *   &#064;ResultMap(userResult)
 *   User selectById(int id);
 *
 *   &#064;Select("SELECT id, name FROM users")
 *   &#064;ResultMap(userResult)
 *   List&lt;User&gt; selectAll();
 * }
 * </pre>
 *
 * @since 3.6.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface StandaloneResultMap {
  /**
   * Returns the Java type created with this result map.
   *
   * @return the Java type that should be created with this result map
   */
  Class<?> javaType();

  /**
   * Returns mapping definitions for constructor arguments.
   * <p>
   * Either <code>constructorArgs</code> or <code>propertyMappings</code>, but not both, must be specified
   *
   * @return mapping definitions
   */
  Arg[] constructorArguments() default {};

  /**
   * Returns mapping definitions for property.
   * <p>
   * Either <code>constructorArgs</code> or <code>propertyMappings</code>, but not both, must be specified
   *
   * @return mapping definitions
   */
  Result[] propertyMappings() default {};

  TypeDiscriminator typeDiscriminator() default @TypeDiscriminator(column = NULL_TYPE_DISCRIMINATOR, cases = {});
}
