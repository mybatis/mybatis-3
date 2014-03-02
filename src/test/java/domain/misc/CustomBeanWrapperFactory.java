/*
 *    Copyright 2009-2012 the original author or authors.
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
package domain.misc;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

import domain.blog.Author;

public class CustomBeanWrapperFactory implements ObjectWrapperFactory {
  public boolean hasWrapperFor(Object object) {
    if (object instanceof Author) {
      return true;
    } else {
      return false;
    }
  }
  
  public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
    return new CustomBeanWrapper(metaObject, object);
  }
}
