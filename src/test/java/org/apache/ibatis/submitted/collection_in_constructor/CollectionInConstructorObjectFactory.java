/*
 *    Copyright 2009-2024 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.apache.ibatis.submitted.collection_in_constructor;

import java.util.List;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

public class CollectionInConstructorObjectFactory extends DefaultObjectFactory {

  private static final long serialVersionUID = -5912469844471984785L;

  @SuppressWarnings("unchecked")
  @Override
  public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    if (type == Store4.class) {
      return (T) Store4.builder().id((Integer) constructorArgs.get(0)).isles((List<Aisle>) constructorArgs.get(1))
          .build();
    }
    return super.create(type, constructorArgTypes, constructorArgs);
  }

}
