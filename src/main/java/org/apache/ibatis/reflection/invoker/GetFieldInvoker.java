/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.reflection.invoker;

import java.lang.reflect.Field;

import org.apache.ibatis.reflection.Reflector;

/**
 * @author Clinton Begin
 */
public class GetFieldInvoker implements Invoker {
  // 属性
  private final Field field;

  public GetFieldInvoker(Field field) {
    this.field = field;
  }
  /**
   * @Author marvin
   * @Description 获取属性值
   * @Date 17:54 2023/9/12
   * @param target
   * @param args
   * @return java.lang.Object
   **/
  @Override
  public Object invoke(Object target, Object[] args) throws IllegalAccessException {
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      if (Reflector.canControlMemberAccessible()) {
        field.setAccessible(true);
        return field.get(target);
      } else {
        throw e;
      }
    }
  }
  /**
   * @Author marvin
   * @Description 获取属性类型
   * @Date 17:54 2023/9/12
   * @return java.lang.Class<?>
   **/
  @Override
  public Class<?> getType() {
    return field.getType();
  }
}
