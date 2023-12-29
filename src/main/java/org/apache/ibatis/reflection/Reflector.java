/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.ReflectPermission;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.invoker.SetFieldInvoker;
import org.apache.ibatis.reflection.property.PropertyNamer;

/**
 * This class represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 *
 * @author Clinton Begin
 */
public class Reflector {
  // 对应的类
  private final Class<?> type;
  // 可读属性数组
  private final String[] readablePropertyNames;
  // 可写属性数组
  private final String[] writeablePropertyNames;
  // 属性对应的setting方法的映射
  // key 为属性名称
  // value 为Invoker 对象
  private final Map<String, Invoker> setMethods = new HashMap<>();
  // 属性对应getting方法的映射
  // key 为属性名称
  // value 为Invoker 对象
  private final Map<String, Invoker> getMethods = new HashMap<>();
  // 属性对应setting方法的方法参数类型的映射
  // key 为属性名称
  // value 为方法参数类型
  private final Map<String, Class<?>> setTypes = new HashMap<>();

  // 属性对应getting方法的方法参数类型的映射
  // key 为属性名称
  // value 为返回值的类型
  private final Map<String, Class<?>> getTypes = new HashMap<>();

  // 默认构造方法
  private Constructor<?> defaultConstructor;

  // 不区分大小写的属性集合
  private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

  /**
   * @Author marvin
   * @Description 构造函数
   * @Date 17:21 2023/9/8
   * @param clazz
   * @return null
   **/
  public Reflector(Class<?> clazz) {
    // 设置对应的类
    type = clazz;
    // 设置默认构造器
    addDefaultConstructor(clazz);
    // 添加属性的getter方法
    addGetMethods(clazz);
    // 添加属性的setter方法
    addSetMethods(clazz);
    // 添加这个类中的一些没有set get 方法的属性
    addFields(clazz);
    // 获取可读属性的数组
    readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
    // 获取可写属性的数组
    writeablePropertyNames = setMethods.keySet().toArray(new String[setMethods.keySet().size()]);

    // 将可读可写属性的值都归并到不区分大小写的属性集合
    for (String propName : readablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
    for (String propName : writeablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
  }
  /**
   * @Author marvin
   * @Description 添加默认构造函数
   * @Date 17:24 2023/9/8
   * @param clazz
   **/
  private void addDefaultConstructor(Class<?> clazz) {
    // 查找这个类所有声明的构造函数
    Constructor<?>[] consts = clazz.getDeclaredConstructors();
    for (Constructor<?> constructor : consts) {
      // 遍历构造函数，判断构造函数的方法的参数长度为0 的方法
      if (constructor.getParameterTypes().length == 0) {
          // 设置Reflector 这个类的默认构造函数这个属性
          this.defaultConstructor = constructor;
      }
    }
  }
  /**
   * @Author marvin
   * @Description 添加get方法
   * @Date 17:40 2023/9/8
   * @param cls
   **/
  private void addGetMethods(Class<?> cls) {
    // 创建一个Map key 为属性的名字， value 为这个属性对应的getter方法
    Map<String, List<Method>> conflictingGetters = new HashMap<>();
    // 获取这个类以及父类中的所有方法
    Method[] methods = getClassMethods(cls);
    for (Method method : methods) {
      // get方法的参数 == 0 所以大于 0 的方法直接跳过
      if (method.getParameterTypes().length > 0) {
        continue;
      }
      // 获取方法的名字
      String name = method.getName();
      // 判断方法是不是以get 开头并且长度大于3 或者is 开头长度大于2
      if ((name.startsWith("get") && name.length() > 3)
          || (name.startsWith("is") && name.length() > 2)) {
        // 从方法名转成 属性名
        name = PropertyNamer.methodToProperty(name);
        // 将这个方法添加到 conflictingGetters这个map 中
        addMethodConflict(conflictingGetters, name, method);
      }
    }
    // 解决getter方法冲突
    resolveGetterConflicts(conflictingGetters);
  }
  /**
   * @Author marvin
   * @Description 处理gettter 方法冲突的方法
   * @Date 16:47 2023/9/8
   * @param conflictingGetters
   **/
  private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
    for (Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
      Method winner = null;
      // 获取属性名
      String propName = entry.getKey();
      for (Method candidate : entry.getValue()) {
        if (winner == null) {
          winner = candidate;
          continue;
        }
        // 获取winner方法的返回值
        Class<?> winnerType = winner.getReturnType();
        // 获取candidate方法的返回值
        Class<?> candidateType = candidate.getReturnType();
        // 如果这两个方法的返回值一样
        if (candidateType.equals(winnerType)) {
          // 候选方法的返回值不是boolean 类型
          if (!boolean.class.equals(candidateType)) {
            throw new ReflectionException(
                "Illegal overloaded getter method with ambiguous type for property "
                    + propName + " in class " + winner.getDeclaringClass()
                    + ". This breaks the JavaBeans specification and can cause unpredictable results.");
          } else if (candidate.getName().startsWith("is")) {
            winner = candidate;
          }
          // 判断 candidate 的类型是否继承自winner
        } else if (candidateType.isAssignableFrom(winnerType)) {
          // OK getter type is descendant
        } else if (winnerType.isAssignableFrom(candidateType)) {
          // 如果相反，反过来就可以了
          winner = candidate;
        } else {
          throw new ReflectionException(
              "Illegal overloaded getter method with ambiguous type for property "
                  + propName + " in class " + winner.getDeclaringClass()
                  + ". This breaks the JavaBeans specification and can cause unpredictable results.");
        }
      }
      // 处理完之后一个属性值只会留一个方法
      addGetMethod(propName, winner);
    }
  }
  /**
   * @Author marvin
   * @Description  将 conflictingGetters 解决完冲突之后，放入getMethods和getTypes
   * @Date 16:56 2023/9/8
   * @param name
   * @param method
   **/
  private void addGetMethod(String name, Method method) {
    if (isValidPropertyName(name)) {
      getMethods.put(name, new MethodInvoker(method));
      Type returnType = TypeParameterResolver.resolveReturnType(method, type);
      getTypes.put(name, typeToClass(returnType));
    }
  }
  /**
   * @Author marvin
   * @Description 和get 方法相似处理
   * @Date 17:03 2023/9/8
   * @param cls
   **/
  private void addSetMethods(Class<?> cls) {
    Map<String, List<Method>> conflictingSetters = new HashMap<>();
    Method[] methods = getClassMethods(cls);
    for (Method method : methods) {
      String name = method.getName();
      if (name.startsWith("set") && name.length() > 3) {
        if (method.getParameterTypes().length == 1) {
          name = PropertyNamer.methodToProperty(name);
          addMethodConflict(conflictingSetters, name, method);
        }
      }
    }
    resolveSetterConflicts(conflictingSetters);
  }
  /**
   * @Author marvin
   * @Description 往 conflictingMethods 添加方法
   * @Date 16:44 2023/9/8
   * @param conflictingMethods
   * @param name
   * @param method
   **/
  private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
    // 判断这个方法名字是不是存在，如果不存在就创建一个新的arrayList
    List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
    // 把这个方法添加进list
    list.add(method);
  }

  private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
    for (String propName : conflictingSetters.keySet()) {
      List<Method> setters = conflictingSetters.get(propName);
      Class<?> getterType = getTypes.get(propName);
      Method match = null;
      ReflectionException exception = null;
      for (Method setter : setters) {
        Class<?> paramType = setter.getParameterTypes()[0];
        if (paramType.equals(getterType)) {
          // should be the best match
          match = setter;
          break;
        }
        if (exception == null) {
          try {
            match = pickBetterSetter(match, setter, propName);
          } catch (ReflectionException e) {
            // there could still be the 'best match'
            match = null;
            exception = e;
          }
        }
      }
      if (match == null) {
        throw exception;
      } else {
        addSetMethod(propName, match);
      }
    }
  }

  private Method pickBetterSetter(Method setter1, Method setter2, String property) {
    if (setter1 == null) {
      return setter2;
    }
    Class<?> paramType1 = setter1.getParameterTypes()[0];
    Class<?> paramType2 = setter2.getParameterTypes()[0];
    if (paramType1.isAssignableFrom(paramType2)) {
      return setter2;
    } else if (paramType2.isAssignableFrom(paramType1)) {
      return setter1;
    }
    throw new ReflectionException("Ambiguous setters defined for property '" + property + "' in class '"
        + setter2.getDeclaringClass() + "' with types '" + paramType1.getName() + "' and '"
        + paramType2.getName() + "'.");
  }

  private void addSetMethod(String name, Method method) {
    if (isValidPropertyName(name)) {
      setMethods.put(name, new MethodInvoker(method));
      Type[] paramTypes = TypeParameterResolver.resolveParamTypes(method, type);
      setTypes.put(name, typeToClass(paramTypes[0]));
    }
  }

  private Class<?> typeToClass(Type src) {
    Class<?> result = null;
    if (src instanceof Class) {
      result = (Class<?>) src;
    } else if (src instanceof ParameterizedType) {
      result = (Class<?>) ((ParameterizedType) src).getRawType();
    } else if (src instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) src).getGenericComponentType();
      if (componentType instanceof Class) {
        result = Array.newInstance((Class<?>) componentType, 0).getClass();
      } else {
        Class<?> componentClass = typeToClass(componentType);
        result = Array.newInstance(componentClass, 0).getClass();
      }
    }
    if (result == null) {
      result = Object.class;
    }
    return result;
  }
  /**
   * @Author marvin
   * @Description 添加类中的属性
   * @Date 17:03 2023/9/8
   * @param clazz
   **/
  private void addFields(Class<?> clazz) {
    // 获取类中定义的属性
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      // 如果这个属性值没有set方法
      if (!setMethods.containsKey(field.getName())) {
        // issue #379 - removed the check for final because JDK 1.5 allows
        // modification of final fields through reflection (JSR-133). (JGB)
        // pr #16 - final static can only be set by the classloader
        int modifiers = field.getModifiers();
        // 如果这个变量的修饰符不是final 或者 satic
        if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
          // 将他添加到set方法中
          addSetField(field);
        }
      }
      // 如果这个方法get方法的map中还不包含这个属性的值，就处理下
      if (!getMethods.containsKey(field.getName())) {
        addGetField(field);
      }
    }

    // 处理父类
    if (clazz.getSuperclass() != null) {
      addFields(clazz.getSuperclass());
    }
  }

  private void addSetField(Field field) {
    // 如果是合法的属性名
    if (isValidPropertyName(field.getName())) {
      // 将属性名，属性封装的set方法放入 setMethods
      setMethods.put(field.getName(), new SetFieldInvoker(field));
      Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
      // 将属性的类型，放入 setTypes中
      setTypes.put(field.getName(), typeToClass(fieldType));
    }
  }

  private void addGetField(Field field) {
    if (isValidPropertyName(field.getName())) {
      getMethods.put(field.getName(), new GetFieldInvoker(field));
      Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
      getTypes.put(field.getName(), typeToClass(fieldType));
    }
  }
  /**
   * @Author marvin
   * @Description 判断属性名是否合法
   * 非法的属性名以$ 开头，或者 名字是 serialVersionUID 或者名字是 class
   * @Date 17:08 2023/9/8
   * @param name
   * @return boolean
   **/
  private boolean isValidPropertyName(String name) {
    return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
  }

  /**
   * This method returns an array containing all methods
   * declared in this class and any superclass.
   * We use this method, instead of the simpler Class.getMethods(),
   * because we want to look for private methods as well.
   *
   * 这个方法放回一个包含了所有在这个类中和他的超类中的方法的数组，
   * 我们使用这个方法而不是简单的 Class.getMethods(),因为我们同时想要查询私有方法
   *
   * @param cls The class
   * @return An array containing all methods in this class
   */
  private Method[] getClassMethods(Class<?> cls) {
    Map<String, Method> uniqueMethods = new HashMap<>();
    Class<?> currentClass = cls;
    while (currentClass != null && currentClass != Object.class) {
      // 添加唯一的方法
      addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

      // we also need to look for interface methods -
      // because the class may be abstract

      // 我们也需要查看接口方法，因为这个类可能是抽象类
      // 获取当前类的接口类
      Class<?>[] interfaces = currentClass.getInterfaces();
      for (Class<?> anInterface : interfaces) {
        // 添加接口方法
        addUniqueMethods(uniqueMethods, anInterface.getMethods());
      }

      // 寻找父类
      currentClass = currentClass.getSuperclass();
    }

    Collection<Method> methods = uniqueMethods.values();
    // 以数组的形式返回
    return methods.toArray(new Method[methods.size()]);
  }
  /**
   * @Author marvin
   * @Description 添加唯一方法
   * @Date 16:11 2023/9/8
   * @param uniqueMethods
   * @param methods
   **/
  private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
    for (Method currentMethod : methods) {
      /**
       * @Author marvin
       * @Description 桥方法是编译器自动生成的,开发者不需要关心,只需要知道其目的是保证多态调用的正确性。
       * isBridge() 方法可以用来判断一个方法是否是桥方法
       * @Date 16:17 2023/9/8
       * @param uniqueMethods
       * @param methods
       **/
      if (!currentMethod.isBridge()) {
        // 获取方法签名
        String signature = getSignature(currentMethod);
        // check to see if the method is already known
        // if it is known, then an extended class must have
        // overridden a method
        if (!uniqueMethods.containsKey(signature)) {
          // 放入 uniqueMethods map中
          uniqueMethods.put(signature, currentMethod);
        }
      }
    }
  }
  /**
   * @Author marvin
   * @Description 获取方法的签名
   * @Date 16:18 2023/9/8
   * @param method
   * @return java.lang.String
   * 返回类型#方法名字:参数1，参数2
   **/
  private String getSignature(Method method) {
    StringBuilder sb = new StringBuilder();
    // 获取方法的返回类型
    Class<?> returnType = method.getReturnType();
    // 判断返回类型是否为空
    // 如果返回不为空
    if (returnType != null) {
      // 返回类型的名字 + #
      sb.append(returnType.getName()).append('#');
    }
    // 方法的名字
    sb.append(method.getName());
    // 获取方法的参数
    Class<?>[] parameters = method.getParameterTypes();
    for (int i = 0; i < parameters.length; i++) {
      if (i == 0) {
        sb.append(':');
      } else {
        sb.append(',');
      }
      sb.append(parameters[i].getName());
    }
    return sb.toString();
  }

  /**
   * Checks whether can control member accessible.
   *
   * @return If can control member accessible, it return {@literal true}
   * @since 3.5.0
   */
  public static boolean canControlMemberAccessible() {
    try {
      SecurityManager securityManager = System.getSecurityManager();
      if (null != securityManager) {
        securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
      }
    } catch (SecurityException e) {
      return false;
    }
    return true;
  }

  /**
   * Gets the name of the class the instance provides information for
   *
   * @return The class name
   */
  public Class<?> getType() {
    return type;
  }

  public Constructor<?> getDefaultConstructor() {
    if (defaultConstructor != null) {
      return defaultConstructor;
    } else {
      throw new ReflectionException("There is no default constructor for " + type);
    }
  }

  public boolean hasDefaultConstructor() {
    return defaultConstructor != null;
  }

  public Invoker getSetInvoker(String propertyName) {
    Invoker method = setMethods.get(propertyName);
    if (method == null) {
      throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
    }
    return method;
  }

  public Invoker getGetInvoker(String propertyName) {
    Invoker method = getMethods.get(propertyName);
    if (method == null) {
      throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
    }
    return method;
  }

  /**
   * Gets the type for a property setter
   *
   * @param propertyName - the name of the property
   * @return The Class of the property setter
   */
  public Class<?> getSetterType(String propertyName) {
    Class<?> clazz = setTypes.get(propertyName);
    if (clazz == null) {
      throw new ReflectionException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
    }
    return clazz;
  }

  /**
   * Gets the type for a property getter
   *
   * @param propertyName - the name of the property
   * @return The Class of the property getter
   */
  public Class<?> getGetterType(String propertyName) {
    Class<?> clazz = getTypes.get(propertyName);
    if (clazz == null) {
      throw new ReflectionException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
    }
    return clazz;
  }

  /**
   * Gets an array of the readable properties for an object
   *
   * @return The array
   */
  public String[] getGetablePropertyNames() {
    return readablePropertyNames;
  }

  /**
   * Gets an array of the writable properties for an object
   *
   * @return The array
   */
  public String[] getSetablePropertyNames() {
    return writeablePropertyNames;
  }

  /**
   * Check to see if a class has a writable property by name
   *
   * @param propertyName - the name of the property to check
   * @return True if the object has a writable property by the name
   */
  public boolean hasSetter(String propertyName) {
    return setMethods.keySet().contains(propertyName);
  }

  /**
   * Check to see if a class has a readable property by name
   *
   * @param propertyName - the name of the property to check
   * @return True if the object has a readable property by the name
   */
  public boolean hasGetter(String propertyName) {
    return getMethods.keySet().contains(propertyName);
  }

  public String findPropertyName(String name) {
    return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
  }
}
