/*
 *    Copyright 2009-2022 the original author or authors.
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

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.UnknownTypeHandler;

/**
 * The annotation that specify a mapping definition for the constructor argument.
 *
 * @see ConstructorArgs
 * @author Clinton Begin
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ConstructorArgs.class)
public @interface Arg {

  /**
   * Returns whether id column or not.
   *
   * @return {@code true} if id column; {@code false} if otherwise
   */
  boolean id() default false;

  /**
   * Return the column name(or column label) to map to this argument.
   *
   * @return the column name(or column label)
   */
  String column() default "";

  /**
   * Return the java type for this argument.
   *
   * @return the java type
   */
  Class<?> javaType() default void.class;

  /**
   * Return the jdbc type for column that map to this argument.
   *
   * @return the jdbc type
   */
  JdbcType jdbcType() default JdbcType.UNDEFINED;

  /**
   * Returns the {@link TypeHandler} type for retrieving a column value from result set.
   *
   * @return the {@link TypeHandler} type
   */
  Class<? extends TypeHandler> typeHandler() default UnknownTypeHandler.class;

  /**
   * Return the statement id for retrieving a object that map to this argument.
   *
   * @return the statement id
   */
  String select() default "";

  /**
   * Returns the result map id for mapping to a object that map to this argument.
   *
   * @return the result map id
   */
  String resultMap() default "";

  /**
   * Returns the parameter name for applying this mapping.
   *
   * @return the parameter name
   * @since 3.4.3
   */
  String name() default "";

  /**
   * Returns the column prefix that use when applying {@link #resultMap()}.
   *
   * @return the column prefix
   * @since 3.5.0
   */
  String columnPrefix() default "";
}
