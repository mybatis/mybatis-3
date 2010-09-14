package org.apache.ibatis.executor.loader;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;

public class ResultObjectProxy {

  private static final Set<String> objectMethods = new HashSet<String>(Arrays.asList(new String[]{"equals","clone","hashCode","toString"}));
  private static final TypeHandlerRegistry registry = new TypeHandlerRegistry();
  private static final String FINALIZE_METHOD = "finalize";

  public static Object createProxy(Object target, ResultLoaderMap lazyLoader, boolean aggressive) {
    return EnhancedResultObjectProxyImpl.createProxy(target, lazyLoader, aggressive);
  }

  private static class EnhancedResultObjectProxyImpl implements MethodInterceptor, Serializable {

    private ResultLoaderMap lazyLoader;
    private boolean aggressive;

    private EnhancedResultObjectProxyImpl(ResultLoaderMap lazyLoader, boolean aggressive) {
      this.lazyLoader = lazyLoader;
      this.aggressive = aggressive;
    }

    public static Object createProxy(Object target, ResultLoaderMap lazyLoader, boolean aggressive) {
      final Class type = target.getClass();
      if (registry.hasTypeHandler(type)) {
        return target;
      } else {
        final Object enhanced = Enhancer.create(type, new EnhancedResultObjectProxyImpl(lazyLoader, aggressive));
        copyInitialState(type, target, enhanced);
        return enhanced;
      }
    }

    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      try {
        final String methodName = method.getName();
        synchronized (lazyLoader) {
          if (lazyLoader.size() > 0) {
            if (!FINALIZE_METHOD.equals(methodName)) {
              if (aggressive || objectMethods.contains(methodName)) {
                lazyLoader.loadAll();
              } else if (PropertyNamer.isProperty(methodName)) {
                final String property = PropertyNamer.methodToProperty(methodName);
                if (lazyLoader.hasLoader(property)) {
                  lazyLoader.load(property);
                }
              }
            }
          }
        }
        return methodProxy.invokeSuper(o, args);
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }

    private static void copyInitialState(Class type, Object target, Object enhanced) {
      Class parent = type;
      while (parent != null) {
        final Field[] fields = parent.getDeclaredFields();
        for(Field field : fields) {
          try {
            field.setAccessible(true);
            field.set(enhanced,field.get(target));
          } catch (Exception e) {
            // Nothing useful to do, will only fail on final fields, which will be ignored.
          }
        }
        parent = parent.getSuperclass();
      }
    }

  }

}
