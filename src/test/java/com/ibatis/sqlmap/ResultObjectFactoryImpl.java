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
package com.ibatis.sqlmap;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import com.testdomain.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/*
 * @author Jeff Butler
 */
public class ResultObjectFactoryImpl implements ObjectFactory {

  /*
   *
   */
  public ResultObjectFactoryImpl() {
    super();
  }

  /* (non-Javadoc)
   * @see com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory#createInstance(java.lang.String, java.lang.Class)
   */
  @SuppressWarnings( "unchecked" ) // types are always checked
  public <T> T create(Class<T> clazz) {

    T obj = null;

    if (clazz.equals(IItem.class)) {
      obj = (T) new IItemImpl();
    } else if (clazz.equals((ISupplier.class))) {
      obj = (T) new ISupplierImpl();
    } else if (clazz.equals((ISupplierKey.class))) {
      obj = (T) new ISupplierKeyImpl();
    } else if (clazz.equals(List.class)) {
      obj = (T) new ArrayList<Object>();
    }

    return obj;
  }

  public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
    return create(type);
  }

  public void setProperties(Properties properties) {
  }

  public <T> boolean isCollection(Class<T> type) {
    return Collection.class.isAssignableFrom(type);
  }

  @SuppressWarnings("unchecked")
  public void add(Object collection, Object element) {
    ((Collection<Object>) collection).add(element);
  }

  @SuppressWarnings("unchecked")
  public <E> void addAll(Object collection, List<E> elements) {
    ((Collection<Object>) collection).addAll(elements);
  }

  @SuppressWarnings("unchecked")
  public <T> T[] createArray(Class<T> type, int size) {
    return (T[]) Array.newInstance(type, size);
  }
  
}
