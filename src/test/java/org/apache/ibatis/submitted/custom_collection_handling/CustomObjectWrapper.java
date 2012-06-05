package org.apache.ibatis.submitted.custom_collection_handling;

import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

public class CustomObjectWrapper implements org.apache.ibatis.reflection.wrapper.ObjectWrapper {

  private CustomCollection collection;
  
  public CustomObjectWrapper(CustomCollection collection){
    this.collection = collection;
  }
  
  public Object get(PropertyTokenizer prop) {
    // TODO Auto-generated method stub
    return null;
  }

  public void set(PropertyTokenizer prop, Object value) {
    // TODO Auto-generated method stub

  }

  public String findProperty(String name, boolean useCamelCaseMapping) {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getGetterNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public String[] getSetterNames() {
    // TODO Auto-generated method stub
    return null;
  }

  public Class<?> getSetterType(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public Class<?> getGetterType(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasSetter(String name) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasGetter(String name) {
    // TODO Auto-generated method stub
    return false;
  }

  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isCollection() {
    return true;
  }

  public void add(Object element) {
    ((CustomCollection<Object>) collection).add(element);
  }

  public <E> void addAll(List<E> element) {
    ((CustomCollection<Object>) collection).addAll(element);
  }

}
