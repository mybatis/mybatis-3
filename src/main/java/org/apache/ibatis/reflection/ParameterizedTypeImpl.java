/**
 *    Copyright 2009-2016 the original author or authors.
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeImpl implements ParameterizedType {

  private Class<?> rawType;

  private Type ownerType;

  private Type[] actualTypeArguments;

  public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
    super();
    this.rawType = rawType;
    this.ownerType = ownerType;
    this.actualTypeArguments = actualTypeArguments;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments;
  }

  @Override
  public Type getOwnerType() {
    return ownerType;
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

}
