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
package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * MyBatis uses an ObjectFactory to create all needed new Objects.
 *
 * @author Clinton Begin
 */
public interface ObjectFactory {

  /**
   * 设置配置属性。
   *
   * @param properties 配置属性
   */
  default void setProperties(Properties properties) {
    // NOP
  }

  /**
   * 使用默认构造函数创建一个新对象。
   *
   * @param <T>  泛型
   * @param type 对象类型
   * @return the t
   */
  <T> T create(Class<T> type);

  /**
   * 使用指定的构造函数和参数创建一个新对象。
   *
   * @param <T>                 泛型
   * @param type                对象类型
   * @param constructorArgTypes 构造函数参数类型列表
   * @param constructorArgs     构造函数参数列表
   * @return the t
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

  /**
   * 判断传入的类型是否属于集合类
   *
   * @param <T>  泛型
   * @param type 对象类型
   * @return 类型是否属于集合类
   * @since 3.1.0
   */
  <T> boolean isCollection(Class<T> type);

}
