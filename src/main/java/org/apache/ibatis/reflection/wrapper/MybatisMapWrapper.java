/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author sunchaofei
 */
public class MybatisMapWrapper extends MapWrapper {
  public MybatisMapWrapper(MetaObject metaObject, MybatisMap<String, Object> map) {
    super(metaObject, map);
  }

  @Override
  public Object get(PropertyTokenizer prop) {
    return map.get(prop.getIndexedName());
  }

  @Override
  public void set(PropertyTokenizer prop, Object value) {
    map.put(prop.getIndexedName(), value);
  }

  @Override
  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (!prop.hasNext()) {
      return map.containsKey(prop.getIndexedName());
    }
    if (map.containsKey(prop.getIndexedName())) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return true;
      } else {
        return metaValue.hasGetter(prop.getChildren());
      }
    } else {
      return false;
    }
  }
}
