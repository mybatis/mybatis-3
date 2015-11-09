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
package org.apache.ibatis.annotations;

import org.apache.ibatis.binding.CustomMethodStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The maker annotation that invoke a custom method.
 * <p>
 * <h2>How to define :</h2>
 * Define this annotation as method level annotation.<br>
 * Note that this annotation can not mix with another annotations({@link Select}, {@link Insert}, {@link Update}, {@link Delete}, {@link Flush}, etc..}).
 * <p>
 * e.g.)
 * <pre class="code">
 *   package com.example.domain.mapper;
 *
 *   public interface PersonMapper {
 *     &#064;CustomMethod
 *     Page&lt;Person&gt; findPage(RowBounds rowBounds);
 *   }
 * </pre>
 * <p>
 * <h2>How to implement a custom method :</h2>
 * Implements a custom method at default implementation class("FQCN of Mapper interface" + "Impl") or
 * user defined implementation class which specify using annotation attributes ({@link #type} and {@link #method}).<br>
 * Note that first argument needs to define {@link org.apache.ibatis.session.SqlSession}, and subsequent arguments needs to define same signatures with mapper interface.
 * <strong>This behavior can customize using {@link CustomMethodStrategy}.</strong>
 * <p>
 * e.g.)
 * <pre class="code">
 *   package com.example.domain.mapper;
 *
 *   public class PersonMapperImpl {
 *     public Page&lt;Person&gt; findPage(SqlSession sqlSession, RowBounds rowBounds) {
 *       // ...
 *     }
 *   }
 * </pre>
 * @author Kazuki Shimizu
 * @since 3.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CustomMethod {

  /**
   * Sets class which implements custom method.
   *
   * Default is "FQCN of Mapper interface" + "Impl",
   * <strong>it can customize using {@link CustomMethodStrategy}.</strong>
   * @return Class that implements custom method
   */
  Class<?> type() default void.class;

  /**
   * Sets implementation method name of custom method.
   *
   * Default is same method name with method which annotated.
   * @return Method name of custom method
   */
  String method() default "";

}
