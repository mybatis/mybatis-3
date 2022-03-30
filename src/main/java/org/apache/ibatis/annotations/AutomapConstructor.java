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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The marker annotation that indicate a constructor for automatic mapping.
 *
 * <p>
 * <b>How to use:</b>
 *
 * <pre>
 * public class User {
 *
 *   private int id;
 *   private String name;
 *
 *   public User(int id) {
 *     this.id = id;
 *   }
 *
 *   &#064;AutomapConstructor
 *   public User(int id, String name) {
 *     this.id = id;
 *     this.name = name;
 *   }
 *   // ...
 * }
 * </pre>
 *
 * @author Tim Chen
 * @since 3.4.3
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR })
public @interface AutomapConstructor {
}
