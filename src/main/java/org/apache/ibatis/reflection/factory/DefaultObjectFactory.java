package org.apache.ibatis.reflection.factory;

import org.apache.ibatis.reflection.ReflectionException;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;

public class DefaultObjectFactory implements ObjectFactory, Serializable {

  private static final long serialVersionUID = -8855120656740914948L;

  public Object create(Class type) {
    return create(type, null, null);
  }

  public Object create(Class type, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    Class classToCreate = resolveCollectionInterface(type);
    return instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
  }

  public void setProperties(Properties properties) {
    // no props for default
  }

  private Object instantiateClass(Class type, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    try {
      Constructor constructor;
      if (constructorArgTypes == null || constructorArgs == null) {
        constructor = type.getDeclaredConstructor();
        if (!constructor.isAccessible()) {
          constructor.setAccessible(true);
        }
        return constructor.newInstance();
      } else {
        constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[constructorArgTypes.size()]));
        if (!constructor.isAccessible()) {
          constructor.setAccessible(true);
        }
        return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
      }
    } catch (Exception e) {
      StringBuilder argTypes = new StringBuilder();
      if (constructorArgTypes != null) {
        for (Class argType : constructorArgTypes) {
          argTypes.append(argType.getSimpleName());
          argTypes.append(",");
        }
      }
      StringBuilder argValues = new StringBuilder();
      if (constructorArgs != null) {
        for (Object argValue : constructorArgs) {
          argValues.append(String.valueOf(argValue));
          argValues.append(",");
        }
      }
      throw new ReflectionException("Error instantiating " + type + " with invalid types (" + argTypes + ") or values (" + argValues + "). Cause: " + e, e);
    }
  }


  private Class resolveCollectionInterface(Class type) {
    Class classToCreate;
    if (type == List.class || type == Collection.class) {
      classToCreate = ArrayList.class;
    } else if (type == Map.class) {
      classToCreate = HashMap.class;
    } else if (type == Set.class) {
      classToCreate = HashSet.class;
    } else {
      classToCreate = type;
    }
    return classToCreate;
  }

}
