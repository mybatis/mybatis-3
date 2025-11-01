/*
 *    Copyright 2009-2025 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation that specify an SQL for retrieving record(s).
 * <p>
 * <b>How to use:</b>
 * <ul>
 * <li>Simple:
 *
 * <pre>
 * <code>public interface UserMapper {
 *   &#064;Select("SELECT id, name FROM users WHERE id = #{id}")
 *   User selectById(int id);
 * }
 * </code>
 * </pre>
 *
 * </li>
 * <li>Dynamic SQL:
 *
 * <pre>
 * <code>public interface UserMapper {
 *   &#064;Select({ "&lt;script&gt;", "select * from users", "where name = #{name}",
 *       "&lt;if test=\"age != null\"&gt; age = #{age} &lt;/if&gt;", "&lt;/script&gt;" })
 *   User select(@NotNull String name, @Nullable Integer age);
 * }
 * </code>
 * </pre>
 *
 * </li>
 * </ul>
 *
 * @author Clinton Begin
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html">How to use Dynamic SQL</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Select.List.class)
public @interface Select {
  /**
   * Returns an SQL for retrieving record(s).
   *
   * @return an SQL for retrieving record(s)
   */
  String[] value();

  /**
   * @return A database id that correspond this statement
   *
   * @since 3.5.5
   */
  String databaseId() default "";

  /**
   * Returns whether this select affects DB data.<br>
   * e.g. RETURNING of PostgreSQL or OUTPUT of MS SQL Server.
   *
   * @return {@code true} if this select affects DB data; {@code false} if otherwise
   *
   * @since 3.5.12
   */
  boolean affectData() default false;

  /**
   * The container annotation for {@link Select}.
   *
   * @author Kazuki Shimizu
   *
   * @since 3.5.5
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @interface List {
    Select[] value();
  }

}
