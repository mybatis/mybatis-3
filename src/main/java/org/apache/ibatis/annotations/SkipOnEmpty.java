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

import org.apache.ibatis.binding.MapperMethod.MethodSignature;
import org.apache.ibatis.cursor.Cursor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If the annotated param is empty(refer to {@linkplain MethodSignature#isEmpty(Object) MethodSignature.isEmpty}),
 * the hole sql will not be executed, also no interaction with the database,
 * returns an equivalent object directly, such as empty list, empty map, empty array, 0(effected rows), null...
 * (it is related to the return type and the command type(select or update or...) of the mapper method) <br/>
 *
 * Note: this feature does not support the return type {@linkplain Cursor},
 * supporting Cursor makes the logic confusing, so don't support it.
 *
 * @author trytocatch
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SkipOnEmpty {
  /**
   * the attribute to determine if it is empty, its usage is the same as in mapper,
   * except without specifying the annotated param itself<br/>
   * the default value "" means the annotated param itself
   * @return the attribute to determine if it is empty
   */
  String value() default "";
}
