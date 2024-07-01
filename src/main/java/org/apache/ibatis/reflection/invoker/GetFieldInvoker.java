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

import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.Field;

/**
 * 实现 Invoker 接口，获得 Field 调用者。
 * @author Clinton Begin
 */
public class GetFieldInvoker implements Invoker {
  /**
   * Field 对象
   */
  private final Field field;

  public GetFieldInvoker(Field field) {
    this.field = field;
  }

  /**
   * 获取 Field 属性
   * @param target 目标
   * @param args   参数
   * @return
   * @throws IllegalAccessException
   */
  @Override
  public Object invoke(Object target, Object[] args) throws IllegalAccessException {
    try {
      // 获取 Field 属性
      return field.get(target);
    } catch (IllegalAccessException e) {
      // 判断是否可以修改可访问性
      if (Reflector.canControlMemberAccessible()) {
        // 取消Java语言访问检查并获取 Field 属性
        field.setAccessible(true);
        return field.get(target);
      } else {
        throw e;
      }
    }
  }

  /**
   * @return 属性类型
   */
  @Override
  public Class<?> getType() {
    return field.getType();
  }
}
