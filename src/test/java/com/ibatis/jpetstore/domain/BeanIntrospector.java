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
package com.ibatis.jpetstore.domain;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/*
 * Abstract class used to help development of Probe implementations
 */
public class BeanIntrospector {

  protected Object getIndexedProperty(Object object, String indexedName) {

    Object value = null;

    try {
      String name = indexedName.substring(0, indexedName.indexOf('['));
      int i = Integer.parseInt(indexedName.substring(indexedName.indexOf('[') + 1, indexedName.indexOf(']')));
      Object list = null;
      if ("".equals(name)) {
        list = object;
      } else {
        list = getProperty(object, name);
      }

      if (list instanceof List) {
        value = ((List) list).get(i);
      } else if (list instanceof Object[]) {
        value = ((Object[]) list)[i];
      } else if (list instanceof char[]) {
        value = new Character(((char[]) list)[i]);
      } else if (list instanceof boolean[]) {
        value = new Boolean(((boolean[]) list)[i]);
      } else if (list instanceof byte[]) {
        value = new Byte(((byte[]) list)[i]);
      } else if (list instanceof double[]) {
        value = new Double(((double[]) list)[i]);
      } else if (list instanceof float[]) {
        value = new Float(((float[]) list)[i]);
      } else if (list instanceof int[]) {
        value = new Integer(((int[]) list)[i]);
      } else if (list instanceof long[]) {
        value = new Long(((long[]) list)[i]);
      } else if (list instanceof short[]) {
        value = new Short(((short[]) list)[i]);
      } else {
        throw new RuntimeException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error getting ordinal list from JavaBean. Cause " + e, e);
    }

    return value;
  }

  protected Class getIndexedType(Object object, String indexedName) {

    Class value = null;

    try {
      String name = indexedName.substring(0, indexedName.indexOf('['));
      int i = Integer.parseInt(indexedName.substring(indexedName.indexOf('[') + 1, indexedName.indexOf(']')));
      Object list = null;
      if (!"".equals(name)) {
        list = getProperty(object, name);
      } else {
        list = object;
      }

      if (list instanceof List) {
        value = ((List) list).get(i).getClass();
      } else if (list instanceof Object[]) {
        value = ((Object[]) list)[i].getClass();
      } else if (list instanceof char[]) {
        value = Character.class;
      } else if (list instanceof boolean[]) {
        value = Boolean.class;
      } else if (list instanceof byte[]) {
        value = Byte.class;
      } else if (list instanceof double[]) {
        value = Double.class;
      } else if (list instanceof float[]) {
        value = Float.class;
      } else if (list instanceof int[]) {
        value = Integer.class;
      } else if (list instanceof long[]) {
        value = Long.class;
      } else if (list instanceof short[]) {
        value = Short.class;
      } else {
        throw new RuntimeException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error getting ordinal list from JavaBean. Cause " + e, e);
    }

    return value;
  }

  protected void setIndexedProperty(Object object, String indexedName, Object value) {

    try {
      String name = indexedName.substring(0, indexedName.indexOf('['));
      int i = Integer.parseInt(indexedName.substring(indexedName.indexOf('[') + 1, indexedName.indexOf(']')));
      Object list = getProperty(object, name);
      if (list instanceof List) {
        ((List) list).set(i, value);
      } else if (list instanceof Object[]) {
        ((Object[]) list)[i] = value;
      } else if (list instanceof char[]) {
        ((char[]) list)[i] = ((Character) value).charValue();
      } else if (list instanceof boolean[]) {
        ((boolean[]) list)[i] = ((Boolean) value).booleanValue();
      } else if (list instanceof byte[]) {
        ((byte[]) list)[i] = ((Byte) value).byteValue();
      } else if (list instanceof double[]) {
        ((double[]) list)[i] = ((Double) value).doubleValue();
      } else if (list instanceof float[]) {
        ((float[]) list)[i] = ((Float) value).floatValue();
      } else if (list instanceof int[]) {
        ((int[]) list)[i] = ((Integer) value).intValue();
      } else if (list instanceof long[]) {
        ((long[]) list)[i] = ((Long) value).longValue();
      } else if (list instanceof short[]) {
        ((short[]) list)[i] = ((Short) value).shortValue();
      } else {
        throw new RuntimeException("The '" + name + "' property of the " + object.getClass().getName() + " class is not a List or Array.");
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error getting ordinal value from JavaBean. Cause " + e, e);
    }
  }

  private static final Object[] NO_ARGUMENTS = new Object[0];

  /*
   * Returns an array of the readable properties exposed by a bean
   *
   * @param object The bean
   * @return The properties
   */
  public String[] getReadablePropertyNames(Object object) {
    return ClassIntrospector.getInstance(object.getClass()).getReadablePropertyNames();
  }

  /*
   * Returns an array of the writeable properties exposed by a bean
   *
   * @param object The bean
   * @return The properties
   */
  public String[] getWriteablePropertyNames(Object object) {
    return ClassIntrospector.getInstance(object.getClass()).getWriteablePropertyNames();
  }

  /*
   * Returns the class that the setter expects to receive as a parameter when
   * setting a property value.
   *
   * @param object The bean to check
   * @param name   The name of the property
   * @return The type of the property
   */
  public Class getPropertyTypeForSetter(Object object, String name) {
    Class type = object.getClass();

    if (object instanceof Class) {
      type = getClassPropertyTypeForSetter((Class) object, name);
    } else if (object instanceof Map) {
      Map map = (Map) object;
      Object value = map.get(name);
      if (value == null) {
        type = Object.class;
      } else {
        type = value.getClass();
      }
    } else {
      if (name.indexOf('.') > -1) {
        StringTokenizer parser = new StringTokenizer(name, ".");
        while (parser.hasMoreTokens()) {
          name = parser.nextToken();
          type = ClassIntrospector.getInstance(type).getSetterType(name);
        }
      } else {
        type = ClassIntrospector.getInstance(type).getSetterType(name);
      }
    }

    return type;
  }

  /*
   * Returns the class that the getter will return when reading a property value.
   *
   * @param object The bean to check
   * @param name   The name of the property
   * @return The type of the property
   */
  public Class getPropertyTypeForGetter(Object object, String name) {
    Class type = object.getClass();

    if (object instanceof Class) {
      type = getClassPropertyTypeForGetter((Class) object, name);
    } else if (object instanceof Map) {
      Map map = (Map) object;
      Object value = map.get(name);
      if (value == null) {
        type = Object.class;
      } else {
        type = value.getClass();
      }
    } else {
      if (name.indexOf('.') > -1) {
        StringTokenizer parser = new StringTokenizer(name, ".");
        while (parser.hasMoreTokens()) {
          name = parser.nextToken();
          type = ClassIntrospector.getInstance(type).getGetterType(name);
        }
      } else {
        type = ClassIntrospector.getInstance(type).getGetterType(name);
      }
    }

    return type;
  }

  /*
   * Returns the class that the getter will return when reading a property value.
   *
   * @param type The class to check
   * @param name The name of the property
   * @return The type of the property
   */
  private Class getClassPropertyTypeForGetter(Class type, String name) {

    if (name.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(name, ".");
      while (parser.hasMoreTokens()) {
        name = parser.nextToken();
        type = ClassIntrospector.getInstance(type).getGetterType(name);
      }
    } else {
      type = ClassIntrospector.getInstance(type).getGetterType(name);
    }

    return type;
  }

  /*
   * Returns the class that the setter expects to receive as a parameter when
   * setting a property value.
   *
   * @param type The class to check
   * @param name The name of the property
   * @return The type of the property
   */
  private Class getClassPropertyTypeForSetter(Class type, String name) {

    if (name.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(name, ".");
      while (parser.hasMoreTokens()) {
        name = parser.nextToken();
        type = ClassIntrospector.getInstance(type).getSetterType(name);
      }
    } else {
      type = ClassIntrospector.getInstance(type).getSetterType(name);
    }

    return type;
  }

  /*
   * Gets an Object property from a bean
   *
   * @param object The bean
   * @param name   The property name
   * @return The property value (as an Object)
   */
  public Object getObject(Object object, String name) {
    if (name.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(name, ".");
      Object value = object;
      while (parser.hasMoreTokens()) {
        value = getProperty(value, parser.nextToken());

        if (value == null) {
          break;
        }

      }
      return value;
    } else {
      return getProperty(object, name);
    }
  }

  /*
   * Sets the value of a bean property to an Object
   *
   * @param object The bean to change
   * @param name   The name of the property to set
   * @param value  The new value to set
   */
  public void setObject(Object object, String name, Object value) {
    if (name.indexOf('.') > -1) {
      StringTokenizer parser = new StringTokenizer(name, ".");
      String property = parser.nextToken();
      Object child = object;
      while (parser.hasMoreTokens()) {
        Class type = getPropertyTypeForSetter(child, property);
        Object parent = child;
        child = getProperty(parent, property);
        if (child == null) {
          if (value == null) {
            return; // don't instantiate child path if value is null
          } else {
            try {
              child = type.newInstance();
              setObject(parent, property, child);
            } catch (Exception e) {
              throw new RuntimeException("Cannot set value of property '" + name + "' because '" + property + "' is null and cannot be instantiated on instance of " + type.getName() + ". Cause:" + e.toString(), e);
            }
          }
        }
        property = parser.nextToken();
      }
      setProperty(child, property, value);
    } else {
      setProperty(object, name, value);
    }
  }


  /*
   * Checks to see if a bean has a writable property be a given name
   *
   * @param object       The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is writable
   */
  public boolean hasWritableProperty(Object object, String propertyName) {
    boolean hasProperty = false;
    if (object instanceof Map) {
      hasProperty = true;//((Map) object).containsKey(propertyName);
    } else {
      if (propertyName.indexOf('.') > -1) {
        StringTokenizer parser = new StringTokenizer(propertyName, ".");
        Class type = object.getClass();
        while (parser.hasMoreTokens()) {
          propertyName = parser.nextToken();
          type = ClassIntrospector.getInstance(type).getGetterType(propertyName);
          hasProperty = ClassIntrospector.getInstance(type).hasWritableProperty(propertyName);
        }
      } else {
        hasProperty = ClassIntrospector.getInstance(object.getClass()).hasWritableProperty(propertyName);
      }
    }
    return hasProperty;
  }

  /*
   * Checks to see if a bean has a readable property be a given name
   *
   * @param object       The bean to check
   * @param propertyName The property to check for
   * @return True if the property exists and is readable
   */
  public boolean hasReadableProperty(Object object, String propertyName) {
    boolean hasProperty = false;
    if (object instanceof Map) {
      hasProperty = true;//((Map) object).containsKey(propertyName);
    } else {
      if (propertyName.indexOf('.') > -1) {
        StringTokenizer parser = new StringTokenizer(propertyName, ".");
        Class type = object.getClass();
        while (parser.hasMoreTokens()) {
          propertyName = parser.nextToken();
          type = ClassIntrospector.getInstance(type).getGetterType(propertyName);
          hasProperty = ClassIntrospector.getInstance(type).hasReadableProperty(propertyName);
        }
      } else {
        hasProperty = ClassIntrospector.getInstance(object.getClass()).hasReadableProperty(propertyName);
      }
    }
    return hasProperty;
  }

  protected Object getProperty(Object object, String name) {
    ClassIntrospector classCache = ClassIntrospector.getInstance(object.getClass());
    try {
      Object value = null;
      if (name.indexOf('[') > -1) {
        value = getIndexedProperty(object, name);
      } else {
        if (object instanceof Map) {
          value = ((Map) object).get(name);
        } else {
          Method method = classCache.getGetter(name);
          if (method == null) {
            throw new NoSuchMethodException("No GET method for property " + name + " on instance of " + object.getClass().getName());
          }
          try {
            value = method.invoke(object, NO_ARGUMENTS);
          } catch (Throwable t) {
            throw ClassIntrospector.unwrapThrowable(t);
          }
        }
      }
      return value;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable t) {
      if (object == null) {
        throw new RuntimeException("Could not get property '" + name + "' from null reference.  Cause: " + t.toString(), t);
      } else {
        throw new RuntimeException("Could not get property '" + name + "' from " + object.getClass().getName() + ".  Cause: " + t.toString(), t);
      }
    }
  }

  protected void setProperty(Object object, String name, Object value) {
    ClassIntrospector classCache = ClassIntrospector.getInstance(object.getClass());
    try {
      if (name.indexOf('[') > -1) {
        setIndexedProperty(object, name, value);
      } else {
        if (object instanceof Map) {
          ((Map) object).put(name, value);
        } else {
          Method method = classCache.getSetter(name);
          if (method == null) {
            throw new NoSuchMethodException("No SET method for property " + name + " on instance of " + object.getClass().getName());
          }
          Object[] params = new Object[1];
          params[0] = value;
          try {
            method.invoke(object, params);
          } catch (Throwable t) {
            throw ClassIntrospector.unwrapThrowable(t);
          }
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable t) {
      if (object == null) {
        throw new RuntimeException("Could not set property '" + name + "' for null reference.  Cause: " + t.toString(), t);
      } else {
        throw new RuntimeException("Could not set property '" + name + "' for " + object.getClass().getName() + ".  Cause: " + t.toString(), t);
      }
    }
  }


}
