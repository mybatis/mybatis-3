/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.reflection.property;

import java.lang.reflect.Field;

import org.apache.ibatis.reflection.Reflector;

/**
 * @author Clinton Begin
 */
public final class PropertyCopier {

  private PropertyCopier() {
    // Prevent Instantiation of Static Class
  }

  /**
   * 对象属性复制
   */
  public static void copyBeanProperties(Class<?> type, Object sourceBean, Object destinationBean) {
    Class<?> parent = type;
    while (parent != null) {
      final Field[] fields = parent.getDeclaredFields();
      for (Field field : fields) {
        try {
          try {
            field.set(destinationBean, field.get(sourceBean));
          } catch (IllegalAccessException e) {
            if (Reflector.canControlMemberAccessible()) {
              field.setAccessible(true);
              field.set(destinationBean, field.get(sourceBean));
            } else {
              throw e;
            }
          }
        } catch (Exception e) {
          // Nothing useful to do, will only fail on final fields, which will be ignored.
        }
      }
      // 把父类的属性也会复制
      parent = parent.getSuperclass();
    }
  }

  public static class Hobo{
    String name;
  }
  public static class HoboSon extends Hobo{
    String sonName;

    public HoboSon(String sonName, String name) {
      this.sonName = sonName;
      this.name = name;
    }

    public HoboSon() {
    }

    @Override
    public String toString() {
      return "HoboSon{" +
        "name='" + name + '\'' +
        ", sonName='" + sonName + '\'' +
        '}';
    }
  }

  public static void main(String[] args) {
    HoboSon hoboSon = new HoboSon("hobo", "cool");
    HoboSon hoboSon2 = new HoboSon();

    PropertyCopier.copyBeanProperties(HoboSon.class, hoboSon, hoboSon2);

    System.out.println(hoboSon2);
  }

}
