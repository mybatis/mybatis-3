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
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.mapping.StatementType;

/**
 * The annotation that specify an SQL for retrieving a key value.
 *
 * <p>
 * <b>How to use:</b>
 *
 * <pre>
 * public interface UserMapper {
 *   &#064;SelectKey(statement = "SELECT identity('users')", keyProperty = "id", before = true, resultType = int.class)
 *   &#064;Insert("INSERT INTO users (id, name) VALUES(#{id}, #{name})")
 *   boolean insert(User user);
 * }
 * </pre>
 *
 * @author Clinton Begin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(SelectKey.List.class)
public @interface SelectKey {
  /**
   * Returns an SQL for retrieving a key value.
   *
   * @return an SQL for retrieving a key value
   */
  String[] statement();

  /**
   * Returns property names that holds a key value.
   * <p>
   * If you specify multiple property, please separate using comma(',').
   * </p>
   *
   * @return property names that separate with comma(',')
   */
  String keyProperty();

  /**
   * Returns column names that retrieves a key value.
   * <p>
   * If you specify multiple column, please separate using comma(',').
   * </p>
   *
   * @return column names that separate with comma(',')
   */
  String keyColumn() default "";

  /**
   * Returns whether retrieves a key value before executing insert/update statement.
   *
   * @return {@code true} if execute before; {@code false} if otherwise
   */
  boolean before();

  /**
   * Returns the key value type.
   *
   * @return the key value type
   */
  Class<?> resultType();

  /**
   * Returns the statement type to use.
   *
   * @return the statement type
   */
  StatementType statementType() default StatementType.PREPARED;

  /**
   * @return A database id that correspond this select key
   * @since 3.5.5
   */
  String databaseId() default "";

  /**
   * The container annotation for {@link SelectKey}.
   * @author Kazuki Shimizu
   * @since 3.5.5
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @interface List {
    SelectKey[] value();
  }

}
