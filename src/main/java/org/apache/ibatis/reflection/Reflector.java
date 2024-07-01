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

import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.invoker.SetFieldInvoker;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * This class represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 *
 * @author Clinton Begin
 */
public class Reflector {

  /**
   * 对应的类
   */
  private final Class<?> type;
  /**
   * 可读属性集合
   */
  private final String[] readablePropertyNames;
  /**
   * 可写属性集合
   */
  private final String[] writeablePropertyNames;
  /**
   * 属性对应的 setting 方法的映射
   * key 为属性名称
   * value 为 Invoker 对象
   */
  private final Map<String, Invoker> setMethods = new HashMap<>();
  /**
   * 属性对应的 getting 方法的映射
   * key 为属性名称
   * value 为 Invoker 对象
   */
  private final Map<String, Invoker> getMethods = new HashMap<>();
  /**
   * 属性对应的 setting 方法的方法参数类型的映射。{@link #setMethods}
   *
   * key 为属性名称
   * value 为方法参数类型
   */
  private final Map<String, Class<?>> setTypes = new HashMap<>();
  /**
   * 属性对应的 getting 方法的返回值类型的映射。{@link #getMethods}
   *
   * key 为属性名称
   * value 为返回值的类型
   */
  private final Map<String, Class<?>> getTypes = new HashMap<>();
  /**
   * 默认构造方法
   */
  private Constructor<?> defaultConstructor;

  /**
   * 不区分大小写的属性集合
   */
  private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

  public Reflector(Class<?> clazz) {
    // 设置对应的类
    type = clazz;
    // <1> 初始化 DeclaredConstructors
    addDefaultConstructor(clazz);
    // <2> 初始化 getMethods 和 getTypes，通过遍历 getting 方法
    addGetMethods(clazz);
    // <3> 初始化 setMethods 和 setTypes，通过遍历 setting 方法
    addSetMethods(clazz);
    // <4> 初始化 getMethods 和 getTypes，setMethods 和 setTypes，通过遍历 fields 属性
    addFields(clazz);
    // <5> 初始化可读属性、可写属性、不区分大小写的属性集合
    readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
    writeablePropertyNames = setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
    for (String propName : readablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
    for (String propName : writeablePropertyNames) {
      caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
    }
  }

  /**
   * 初始化默认的构造方法
   * @param clazz 类
   */
  private void addDefaultConstructor(Class<?> clazz) {
    // 获取所有的构造方法
    Constructor<?>[] consts = clazz.getDeclaredConstructors();
    for (Constructor<?> constructor : consts) {
      // 拿到无参构造方法
      if (constructor.getParameterTypes().length == 0) {
          this.defaultConstructor = constructor;
      }
    }
  }

  private void addGetMethods(Class<?> cls) {
    // <1> 属性与其 getting 方法的映射
    Map<String, List<Method>> conflictingGetters = new HashMap<>();
    // <2> 获取所有的方法
    Method[] methods = getClassMethods(cls);
    // <3> 遍历所有的方法
    for (Method method : methods) {
      // <3.1> 有参数的方法，说明不是 getting 方法，忽略
      if (method.getParameterTypes().length > 0) {
        continue;
      }
      // <3.2> 获取方法名称，以 get 或 is 开头的是 getting 方法
      String name = method.getName();
      if ((name.startsWith("get") && name.length() > 3)
          || (name.startsWith("is") && name.length() > 2)) {
        // <3.3> 获得属性
        name = PropertyNamer.methodToProperty(name);
        // <3.4> 添加到 conflictingGetters 中
        addMethodConflict(conflictingGetters, name, method);
      }
    }
    // <4> 解决 getting 冲突方法
    resolveGetterConflicts(conflictingGetters);
  }

  private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
    // <4.1> 遍历每个属性，查找其最匹配的方法。因为子类可以覆写父类的方法，所以一个属性可能对应多个 getting 方法
    for (Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
      Method winner = null;
      // <4.2> 属性名称
      String propName = entry.getKey();
      // <4.3> 遍历方法
      for (Method candidate : entry.getValue()) {
        // winner 为空，说明 candidate 是最匹配的方法
        if (winner == null) {
          winner = candidate;
          continue;
        }
        // winner 不为空，说明当前属性的 getting 方法有多个
        // 获取 winner 的返回类型和 candidateType 的返回类型
        Class<?> winnerType = winner.getReturnType();
        Class<?> candidateType = candidate.getReturnType();
        if (candidateType.equals(winnerType)) { // 返回类型相同
          if (!boolean.class.equals(candidateType)) {
            throw new ReflectionException(
                "Illegal overloaded getter method with ambiguous type for property "
                    + propName + " in class " + winner.getDeclaringClass()
                    + ". This breaks the JavaBeans specification and can cause unpredictable results.");
          } else if (candidate.getName().startsWith("is")) { // 选择 boolean 类型的 is 方法
            winner = candidate;
          }
        } else if (candidateType.isAssignableFrom(winnerType)) {  // 不符合选择子类
          // OK getter type is descendant
        } else if (winnerType.isAssignableFrom(candidateType)) {  // 符合选择子类
          winner = candidate;
        } else { // 返回类型冲突
          throw new ReflectionException(
              "Illegal overloaded getter method with ambiguous type for property "
                  + propName + " in class " + winner.getDeclaringClass()
                  + ". This breaks the JavaBeans specification and can cause unpredictable results.");
        }
      }
      // <4.4> 添加到 getMethods 和 getTypes 中
      addGetMethod(propName, winner);
    }
  }

  private void addGetMethod(String name, Method method) {
    // 判断是合理的属性名
    if (isValidPropertyName(name)) {
      // 添加到 getMethods 中
      getMethods.put(name, new MethodInvoker(method));
      // 添加到 getTypes 中
      Type returnType = TypeParameterResolver.resolveReturnType(method, type);
      getTypes.put(name, typeToClass(returnType));
    }
  }

  private void addSetMethods(Class<?> cls) {
    // <1> 属性与方法的映射
    Map<String, List<Method>> conflictingSetters = new HashMap<>();
    // <2> 获取所有的方法
    Method[] methods = getClassMethods(cls);
    // <3> 遍历所有的方法
    for (Method method : methods) {
      // <3.1> 获取方法名称
      String name = method.getName();
      // <3.2> 方法名以 set 开头且有 1 个参数
      if (name.startsWith("set") && name.length() > 3) {
        if (method.getParameterTypes().length == 1) {
          // <3.3> 获得属性
          name = PropertyNamer.methodToProperty(name);
          // <3.4> 添加到属性与方法的映射中
          addMethodConflict(conflictingSetters, name, method);
        }
      }
    }
    // <4> 解决 setting 冲突方法
    resolveSetterConflicts(conflictingSetters);
  }

  private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
    // 如果 name 对应的 value 不存在，则使用获取 remappingFunction 重新计算后的值，并保存为该 key 的 value，否则返回 value。
    List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
    list.add(method);
  }

  private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
    // 遍历每个属性，查找其最匹配的方法。因为子类可以覆写父类的方法，所以一个属性，可能对应多个 setting 方法
    for (String propName : conflictingSetters.keySet()) {
      List<Method> setters = conflictingSetters.get(propName);
      Class<?> getterType = getTypes.get(propName);
      Method match = null;
      ReflectionException exception = null;
      // <1> 遍历属性对应的 setting 方法
      for (Method setter : setters) {
        Class<?> paramType = setter.getParameterTypes()[0];
        // 和 getterType 相同，直接使用
        if (paramType.equals(getterType)) {
          // should be the best match
          match = setter;
          break;
        }
        if (exception == null) {
          try {
            // 选择一个更加匹配的
            match = pickBetterSetter(match, setter, propName);
          } catch (ReflectionException e) {
            // there could still be the 'best match'
            match = null;
            exception = e;
          }
        }
      }
      // <2> 添加到 setMethods 和 setTypes 中
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
    // 符合选择字类
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
      // 添加到 setMethods 中
      setMethods.put(name, new MethodInvoker(method));
      // 添加到 setTypes 中
      Type[] paramTypes = TypeParameterResolver.resolveParamTypes(method, type);
      setTypes.put(name, typeToClass(paramTypes[0]));
    }
  }

  private Class<?> typeToClass(Type src) {
    Class<?> result = null;
    // 普通类型，直接返回
    if (src instanceof Class) {
      result = (Class<?>) src;
    // 泛型类型，使用泛型
    } else if (src instanceof ParameterizedType) {
      result = (Class<?>) ((ParameterizedType) src).getRawType();
    // 泛型数组，获得具体类
    } else if (src instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) src).getGenericComponentType();
      if (componentType instanceof Class) {  // 普通类型
        result = Array.newInstance((Class<?>) componentType, 0).getClass();
      } else {
        Class<?> componentClass = typeToClass(componentType); // 递归此方法，返回类
        result = Array.newInstance(componentClass, 0).getClass();
      }
    }
    // 都不符合，使用 Object 类
    if (result == null) {
      result = Object.class;
    }
    return result;
  }

  private void addFields(Class<?> clazz) {
    // <1> 获取所有的属性
    Field[] fields = clazz.getDeclaredFields();
    // <2> 遍历所有属性
    for (Field field : fields) {
      // <3> 将不在 setMethods 中的属性加入 setMethods 中
      if (!setMethods.containsKey(field.getName())) {
        // issue #379 - removed the check for final because JDK 1.5 allows
        // modification of final fields through reflection (JSR-133). (JGB)
        // pr #16 - final static can only be set by the classloader
        // 判断属性的修饰符是否为 final 和 static，不是则添加到 setMethods 和 setTypes 中
        int modifiers = field.getModifiers();
        if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
          addSetField(field);
        }
      }
      // <4> 将不在 getMethods 中的属性加入 getMethods 中
      if (!getMethods.containsKey(field.getName())) {
        addGetField(field);
      }
    }
    // <5> 通过递归添加父类中的属性
    if (clazz.getSuperclass() != null) {
      addFields(clazz.getSuperclass());
    }
  }

  private void addSetField(Field field) {
    // 判断是合理的属性
    if (isValidPropertyName(field.getName())) {
      // 添加到 setMethods 中
      setMethods.put(field.getName(), new SetFieldInvoker(field));
      // 添加到 setTypes 中
      Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
      setTypes.put(field.getName(), typeToClass(fieldType));
    }
  }

  private void addGetField(Field field) {
    // 判断是合理的属性
    if (isValidPropertyName(field.getName())) {
      // 添加到 getMethods 中
      getMethods.put(field.getName(), new GetFieldInvoker(field));
      // 添加到 getTypes 中
      Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
      getTypes.put(field.getName(), typeToClass(fieldType));
    }
  }

  private boolean isValidPropertyName(String name) {
    return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
  }

  /**
   * This method returns an array containing all methods
   * declared in this class and any superclass.
   * We use this method, instead of the simpler Class.getMethods(),
   * because we want to look for private methods as well.
   *
   * @param cls The class
   * @return An array containing all methods in this class
   */
  private Method[] getClassMethods(Class<?> cls) {
    // 每个 方法签名(方法返回值类型#方法名:方法参数名1,...,方法参数名n) 与 该方法 的映射
    Map<String, Method> uniqueMethods = new HashMap<>();
    // 循环类，类的父类，类的父类的父类，直到 Object 类
    Class<?> currentClass = cls;
    while (currentClass != null && currentClass != Object.class) {
      // <1> 添加方法签名和该方法的映射
      addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

      // we also need to look for interface methods -
      // because the class may be abstract
      // <2> 获取当前类实现的接口
      Class<?>[] interfaces = currentClass.getInterfaces();
      // <2.1> 添加接口的方法签名及方法的映射
      for (Class<?> anInterface : interfaces) {
        addUniqueMethods(uniqueMethods, anInterface.getMethods());
      }
      // <3> 获取父类
      currentClass = currentClass.getSuperclass();
    }

    // <4> 转换成 methods 数组返回
    Collection<Method> methods = uniqueMethods.values();

    return methods.toArray(new Method[methods.size()]);
  }

  /**
   * 添加方法签名和该方法的映射
   * @param uniqueMethods 方法签名和该方法的映射
   * @param methods 所有的方法
   */
  private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
    for (Method currentMethod : methods) {
      // <1.1> 忽略桥接方法
      if (!currentMethod.isBridge()) {
        // <1.2> 获取方法签名(方法返回值类型#方法名:方法参数名1,...,方法参数名n)
        String signature = getSignature(currentMethod);
        // check to see if the method is already known
        // if it is known, then an extended class must have
        // overridden a method
        // <1.3> 添加方法签名和该方法的映射
        if (!uniqueMethods.containsKey(signature)) {
          uniqueMethods.put(signature, currentMethod);
        }
      }
    }
  }

  /**
   * 获取方法签名(方法返回值类型#方法名:方法参数名1,...,方法参数名n)
   * @param method 方法
   * @return 方法签名
   */
  private String getSignature(Method method) {
    StringBuilder sb = new StringBuilder();
    // <1.2.1> 获取方法返回值类型
    Class<?> returnType = method.getReturnType();
    if (returnType != null) {
      sb.append(returnType.getName()).append('#');
    }
    // <1.2.2> 获取方法名
    sb.append(method.getName());
    // <1.2.3> 获取方法参数列表
    Class<?>[] parameters = method.getParameterTypes();
    // <1.2.4> 拼接方法参数列表
    for (int i = 0; i < parameters.length; i++) {
      if (i == 0) {
        sb.append(':');
      } else {
        sb.append(',');
      }
      sb.append(parameters[i].getName());
    }
    // <1.2.5> 返回方法签名
    return sb.toString();
  }

  /**
   * Checks whether can control member accessible.
   * 判断，是否可以修改可访问性
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
