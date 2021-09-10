/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.binding;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.ibatis.lang.UsesJava7;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 * 接口Mapper内的方法能重载(overLoad)吗？(重要)？
 * 原因：在投鞭断流时，Mybatis使用package+Mapper+method全限名作为key，去xml内寻找唯一sql来执行的。类似：key=x.y.UserMapper.getUserById，那么，重载方法时将导致矛盾。对于Mapper接口，Mybatis禁止方法重载(overLoad)。
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

  private static final long serialVersionUID = -6424540398559729838L;
  private final SqlSession sqlSession;
  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethod> methodCache;

  public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
    this.sqlSession = sqlSession;
    this.mapperInterface = mapperInterface;
    this.methodCache = methodCache;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
//      我来说正确答案吧。我们知道这个MapperProxy就是一个InvocationHandler（他的作用是jdk创建动态代理时用的，不清楚动态代理，自己补习一下），
//      也就是我们会根据当前的接口创建一个这个接口的动态代理对象，使用动态代理对象再利用反射调用实际对象的目标方法。
//      然而动态代理对象里面的方法都是Interface规定的。但是动态代理对象也能调用比如toString(),hashCode()等这些方法呀，这些方法是所有类从Object继承过来的。
//
//      所以这个判断的根本作用就是，如果利用动态代理对象调用的是toString，hashCode,getClass等这些从Object类继承过来的方法，
//      就直接反射调用。如果调用的是接口规定的方法。我们就用MapperMethod来执行。

//      结论：
//
//      1）、method.getDeclaringClass用来判断当前这个方法是哪个类的方法。
//
//      2）、接口创建出的代理对象不仅有实现接口的方法，也有从Object继承过来的方法
//
//      3）、实现的接口的方法method.getDeclaringClass是接口类型，比如com.atguigu.dao.EmpDao
//
//      从Object类继承过来的方法类型是java.lang.Object类型
//
//      4）、如果是Object类继承来的方法，直接反射调用
//
//      如果是实现的接口规定的方法，利用Mybatis的MapperMethod调用
      if (Object.class.equals(method.getDeclaringClass())) {
        //// 诸如hashCode()、toString()、equals()等方法，将target指向当前对象thisreturn method.invoke(this, args);
        //target，在执行Object.java内的方法时，target被指向了this，target已经变成了傀儡、象征、占位符。在投鞭断流式的拦截时，已经没有了target。
        return method.invoke(this, args);
      } else if (isDefaultMethod(method)) {
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      throw ExceptionUtil.unwrapThrowable(t);
    }
    final MapperMethod mapperMethod = cachedMapperMethod(method);
    return mapperMethod.execute(sqlSession, args);
  }

  private MapperMethod cachedMapperMethod(Method method) {
    MapperMethod mapperMethod = methodCache.get(method);
    if (mapperMethod == null) {
      mapperMethod = new MapperMethod(mapperInterface, method, sqlSession.getConfiguration());
      methodCache.put(method, mapperMethod);
    }
    return mapperMethod;
  }

  @UsesJava7
  private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
      throws Throwable {
    final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
        .getDeclaredConstructor(Class.class, int.class);
    if (!constructor.isAccessible()) {
      constructor.setAccessible(true);
    }
    final Class<?> declaringClass = method.getDeclaringClass();
    return constructor
        .newInstance(declaringClass,
            MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
        .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
  }

  /**
   * Backport of java.lang.reflect.Method#isDefault()
   */
  private boolean isDefaultMethod(Method method) {
    return (method.getModifiers()
        & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC
        && method.getDeclaringClass().isInterface();
  }
}
