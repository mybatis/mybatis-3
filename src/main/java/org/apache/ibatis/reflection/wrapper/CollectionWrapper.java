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
package org.apache.ibatis.reflection.wrapper;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 */
public class CollectionWrapper implements ObjectWrapper {

  private Collection<Object> object;

  public CollectionWrapper(MetaObject metaObject, Collection<Object> object) {
    this.object = object;
  }

  public Object get(PropertyTokenizer prop) {
    throw new UnsupportedOperationException();
  }

  public void set(PropertyTokenizer prop, Object value) {
    throw new UnsupportedOperationException();
  }

  public String findProperty(String name, boolean useCamelCaseMapping) {
    throw new UnsupportedOperationException();
  }

  public String[] getGetterNames() {
    throw new UnsupportedOperationException();
  }

  public String[] getSetterNames() {
    throw new UnsupportedOperationException();
  }

  public Class<?> getSetterType(String name) {
    throw new UnsupportedOperationException();
  }

  public Class<?> getGetterType(String name) {
    throw new UnsupportedOperationException();
  }

  public boolean hasSetter(String name) {
    throw new UnsupportedOperationException();
  }

  public boolean hasGetter(String name) {
    throw new UnsupportedOperationException();
  }

  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    throw new UnsupportedOperationException();
  }

  public boolean isCollection() {
    return true;
  }

  public void add(Object element) {
    object.add(element);
  }

  public <E> void addAll(List<E> element) {
    object.addAll(element);
  }

}
