package org.apache.ibatis.executor.loader;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InterfaceMaker;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.objectweb.asm.Type;

public class ResultObjectProxy {

  private static final Log log = LogFactory.getLog(ResultObjectProxy.class);

  private static final Set<String> objectMethods = new HashSet<String>(Arrays.asList(new String[]{"equals","clone","hashCode","toString"}));
  private static final TypeHandlerRegistry registry = new TypeHandlerRegistry();
  private static final String FINALIZE_METHOD = "finalize";
  private static final String WRITE_REPLACE_METHOD = "writeReplace";

  public static Object createProxy(Object target, ResultLoaderMap lazyLoader, boolean aggressive, ObjectFactory objectFactory) {
    return EnhancedResultObjectProxyImpl.createProxy(target, lazyLoader, aggressive, objectFactory);
  }

  private static class EnhancedResultObjectProxyImpl implements MethodInterceptor, Serializable {

    private static final long serialVersionUID = 3943261828110652464L;
    private Class type;
    private ResultLoaderMap lazyLoader;
    private boolean aggressive;
    private ObjectFactory objectFactory;

    private EnhancedResultObjectProxyImpl(Class type, ResultLoaderMap lazyLoader, boolean aggressive, ObjectFactory objectFactory) {
      this.type = type;
      this.lazyLoader = lazyLoader;
      this.aggressive = aggressive;
      this.objectFactory = objectFactory;
    }

    public static Object createProxy(Object target, ResultLoaderMap lazyLoader, boolean aggressive, ObjectFactory objectFactory) {
      final Class type = target.getClass();
      if (registry.hasTypeHandler(type)) {
        return target;
      } else {
        EnhancedResultObjectProxyImpl proxy = new EnhancedResultObjectProxyImpl(type, lazyLoader, aggressive, objectFactory);
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(proxy);
        enhancer.setSuperclass(type);

        try {
          type.getDeclaredMethod(WRITE_REPLACE_METHOD);
          // ObjectOutputStream will call writeReplace of objects returned by writeReplace
          log.warn(WRITE_REPLACE_METHOD + " method was found on bean " + type + ", make sure it returns this");
        } catch (NoSuchMethodException e) {
          // using asm (org.objectweb.asm.Type), this will not work with cglib-nodep
          InterfaceMaker writeReplaceInterface = new InterfaceMaker();
          Signature signature = new Signature(WRITE_REPLACE_METHOD, Type.getType(Object.class), new Type[] {});
          writeReplaceInterface.add(signature, new Type[] { Type.getType(ObjectStreamException.class) });
          enhancer.setInterfaces(new Class[] { writeReplaceInterface.create() });
        } catch (SecurityException e) {
          // nothing to do here
        }

        final Object enhanced = enhancer.create();
        copyBeanProperties(type, target, enhanced);
        return enhanced;
      }
    }
    
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      final String methodName = method.getName();
      try {
        synchronized (lazyLoader) {
          if (WRITE_REPLACE_METHOD.equals(methodName)) {
            if (lazyLoader.size() > 0) {
              throw new NotSerializableException("Not all lazy properties have been loaded yet");
            }
            Object original = objectFactory.create(type);
            copyBeanProperties(type, o, original);
            return original;
          } else if (!FINALIZE_METHOD.equals(methodName)) {
            if (lazyLoader.size() > 0) {
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

    private static void copyBeanProperties(Class type, Object sourceBean, Object destinationBean) {
      Class parent = type;
      while (parent != null) {
        final Field[] fields = parent.getDeclaredFields();
        for(Field field : fields) {
          try {
            field.setAccessible(true);
            Object val = field.get(sourceBean);
            field.set(destinationBean, val);
          } catch (Exception e) {
            // Nothing useful to do, will only fail on final fields, which will be ignored.
          }
        }
        parent = parent.getSuperclass();
      }
    }
  }

}
