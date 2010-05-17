package org.apache.ibatis.builder;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import java.util.List;
import java.util.Properties;

public class ExampleObjectFactory extends DefaultObjectFactory {

  public Object create(Class type) {
    return super.create(type);
  }

  public Object create(Class type, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    return super.create(type, constructorArgTypes, constructorArgs);
  }

  public void setProperties(Properties properties) {
    super.setProperties(properties);
  }

}
