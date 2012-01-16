/*
 *    Copyright 2009-2012 The MyBatis Team
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
package org.apache.ibatis.binding;

import java.util.HashMap;

public class MapperParamMap<V> extends HashMap<String, V> {

  private static final long serialVersionUID = -2212268410512043556L;

  @Override
  public V get(Object key) {
    if (!super.containsKey(key)) {
      throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + this.keySet());
    }
    return super.get(key);
  }

}
