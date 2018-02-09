/**
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.binding;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.binding.handler.MapperHandler;
import org.apache.ibatis.binding.handler.MapperHandlerContext;
import org.apache.ibatis.binding.handler.impl.DefaultMapperHandlerContext;
import org.apache.ibatis.lang.UsesJava7;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -6424540398559729838L;
    private final SqlSession sqlSession;
    private final Class<T> mapperInterface;
    private final Map<Method, MapperMethod> methodCache;
    /**
     * @since 3.4.6
     */
    private final Map<Method, MapperHandlerContext> mapperHandlerContextCache;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache) {
        this(sqlSession, mapperInterface, methodCache, null);
    }

    /**
     * @since 3.4.6
     */
    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> methodCache, Map<Method, MapperHandlerContext> mapperHandlerContextCache) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
        this.mapperHandlerContextCache = mapperHandlerContextCache;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            } else if (isDefaultMethod(method)) {
                return invokeDefaultMethod(proxy, method, args);
            }
        } catch (Throwable t) {
            throw ExceptionUtil.unwrapThrowable(t);
        }

        if (null != mapperHandlerContextCache && sqlSession.getConfiguration().isEnableMapperHandler()) {
            cachedMapperMethod(method);// for pass the test class org.apache.ibatis.binding.BindingTest
            final MapperHandlerContext mapperHandlerContext = cachedMapperHandlerContext(method);
            MapperHandler mapperHandler = this.getMapperHandler(mapperHandlerContext, method);
            if (null != mapperHandler) {
                return mapperHandler.execute(sqlSession, args, mapperHandlerContext);
            }
        }
        final MapperMethod mapperMethod = cachedMapperMethod(method);
        return mapperMethod.execute(sqlSession, args);
    }

    private MapperHandler getMapperHandler(MapperHandlerContext mapperHandlerContext, Method method) {
        List<MapperHandler> mapperHandlers = sqlSession.getConfiguration().getMapperHandlers();
        if (null != mapperHandlers) {
            for (MapperHandler mapperHandler : mapperHandlers) {
                if (mapperHandler.supports(mapperHandlerContext)) {
                    return mapperHandler;
                }
            }
        }
        return null;
    }

    private MapperHandlerContext cachedMapperHandlerContext(Method method) {
        MapperHandlerContext mhc = mapperHandlerContextCache.get(method);
        if (mhc == null) {
            mhc = new DefaultMapperHandlerContext(mapperInterface, method, sqlSession.getConfiguration());
            mapperHandlerContextCache.put(method, mhc);
        }
        return mhc;
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
