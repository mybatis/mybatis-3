/**
 * Copyright (c) 2019 ucsmy.com, All rights reserved.
 */
package org.apache.ibatis.submitted.collection_donot_need_oftype_no_reflector_cache;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class BeanUtils {
  public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz){
    Object obj = null;
    try {
      obj = clazz.newInstance();
    if (map != null && !map.isEmpty() && map.size() > 0) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        String propertyName = entry.getKey(); 	// 属性名
        Object value = entry.getValue();		// 属性值
        String setMethodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        Field field = getClassField(clazz, propertyName);	//获取和map的key匹配的属性名称
        if (field == null){
          continue;
        }
        Class<?> fieldTypeClass = field.getType();
        value = convertValType(value, fieldTypeClass);
        try {
          clazz.getMethod(setMethodName, field.getType()).invoke(obj, value);
        } catch (NoSuchMethodException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    }
      return (T) obj;
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static Field getClassField(Class<?> clazz, String fieldName) {
    if (Object.class.getName().equals(clazz.getName())) {
      return null;
    }
    Field[] declaredFields = clazz.getDeclaredFields();
    for (Field field : declaredFields) {
      if (field.getName().equals(fieldName)) {
        return field;
      }
    }
    Class<?> superClass = clazz.getSuperclass();	//如果该类还有父类，将父类对象中的字段也取出
    if (superClass != null) {						//递归获取
      return getClassField(superClass, fieldName);
    }
    return null;
  }

  private static Object convertValType(Object value, Class<?> fieldTypeClass) {
    Object retVal = null;

    if (Long.class.getName().equals(fieldTypeClass.getName())
      || long.class.getName().equals(fieldTypeClass.getName())) {
      retVal = Long.parseLong(value.toString());
    } else if (Integer.class.getName().equals(fieldTypeClass.getName())
      || int.class.getName().equals(fieldTypeClass.getName())) {
      retVal = Integer.parseInt(value.toString());
    } else if (Float.class.getName().equals(fieldTypeClass.getName())
      || float.class.getName().equals(fieldTypeClass.getName())) {
      retVal = Float.parseFloat(value.toString());
    } else if (Double.class.getName().equals(fieldTypeClass.getName())
      || double.class.getName().equals(fieldTypeClass.getName())) {
      retVal = Double.parseDouble(value.toString());
    } else {
      retVal = value;
    }
    return retVal;
  }
}
