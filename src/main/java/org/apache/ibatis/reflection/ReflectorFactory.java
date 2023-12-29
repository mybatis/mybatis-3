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
package org.apache.ibatis.reflection;
/**
 * @Author marvin
 * @Description Reflector工厂接口，用于创建和缓存Reflector对象
 * @Date 17:43 2023/9/12

 **/
public interface ReflectorFactory {
  /**
   * @Author marvin
   * @Description 是否缓存 Reflector对象
   * @Date 17:44 2023/9/12
   * @return boolean
   **/
  boolean isClassCacheEnabled();
  /**
   * @Author marvin
   * @Description 设置是否缓存 Reflector 对象
   * @Date 17:45 2023/9/12
   * @param classCacheEnabled
   **/
  void setClassCacheEnabled(boolean classCacheEnabled);
  /**
   * @Author marvin
   * @Description 获取 Reflector 对象
   * @Date 17:45 2023/9/12
   * @param type
   * @return org.apache.ibatis.reflection.Reflector
   **/
  Reflector findForClass(Class<?> type);
}