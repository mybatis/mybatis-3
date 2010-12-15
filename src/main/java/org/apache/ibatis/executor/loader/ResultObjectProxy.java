package org.apache.ibatis.executor.loader;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.objectweb.asm.Type;

public class ResultObjectProxy {

  private static final Log log = LogFactory.getLog(ResultObjectProxy.class);

  private static final Set<String> objectMethods = new HashSet<String>(Arrays.asList(new String[] { "equals", "clone", "hashCode", "toString" }));
  private static final TypeHandlerRegistry registry = new TypeHandlerRegistry();
  private static final String FINALIZE_METHOD = "finalize";
  private static final String WRITE_REPLACE_METHOD = "writeReplace";

  public static Object createProxy(Object target, ResultLoaderMap lazyLoader, boolean aggressive,
      ObjectFactory objectFactory, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    return EnhancedResultObjectProxyImpl.createProxy(target, lazyLoader, aggressive, objectFactory,
        constructorArgTypes, constructorArgs);
  }

  private static class EnhancedResultObjectProxyImpl implements MethodInterceptor {

    private Class type;
    private ResultLoaderMap lazyLoader;
    private boolean aggressive;
    private ObjectFactory objectFactory;
    private List<Class> constructorArgTypes;
    private List<Object> constructorArgs;

    private EnhancedResultObjectProxyImpl(Class type, ResultLoaderMap lazyLoader, boolean aggressive,
        ObjectFactory objectFactory, List<Class> constructorArgTypes, List<Object> constructorArgs) {
      this.type = type;
      this.lazyLoader = lazyLoader;
      this.aggressive = aggressive;
      this.objectFactory = objectFactory;
      this.constructorArgTypes = constructorArgTypes;
      this.constructorArgs = constructorArgs;
    }

    public static Object createProxy(Object target, ResultLoaderMap lazyLoader, boolean aggressive,
        ObjectFactory objectFactory, List<Class> constructorArgTypes, List<Object> constructorArgs) {
      final Class type = target.getClass();
      if (registry.hasTypeHandler(type)) {
        return target;
      } else {
        EnhancedResultObjectProxyImpl proxy = new EnhancedResultObjectProxyImpl(type, lazyLoader, aggressive,
            objectFactory, constructorArgTypes, constructorArgs);
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(proxy);
        enhancer.setSuperclass(type);

        try {
          type.getDeclaredMethod(WRITE_REPLACE_METHOD);
          // ObjectOutputStream will call writeReplace of objects returned by writeReplace
          log.warn(WRITE_REPLACE_METHOD + " method was found on bean " + type + ", make sure it returns this");
        } catch (NoSuchMethodException e) {
          InterfaceMaker writeReplaceInterface = new InterfaceMaker();
          Signature signature = new Signature(WRITE_REPLACE_METHOD, Type.getType(Object.class), new Type[] {});
          writeReplaceInterface.add(signature, new Type[] { Type.getType(ObjectStreamException.class) });
          enhancer.setInterfaces(new Class[] { writeReplaceInterface.create() });
        } catch (SecurityException e) {
          // nothing to do here
        }

        Object enhanced = null;
        if (constructorArgTypes.isEmpty()) {
          enhanced = enhancer.create();
        } else {
          Class[] typesArray = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
          Object[] valuesArray = constructorArgs.toArray(new Object[constructorArgs.size()]);
          enhanced = enhancer.create(typesArray, valuesArray);
        }
        PropertyCopier.copyBeanProperties(type, target, enhanced);
        return enhanced;
      }
    }

    public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      final String methodName = method.getName();
      try {
        synchronized (lazyLoader) {
          if (WRITE_REPLACE_METHOD.equals(methodName)) {
            Object original = null;
            if (constructorArgTypes.isEmpty()) {
              original = objectFactory.create(type);
            } else {
              original = objectFactory.create(type, constructorArgTypes, constructorArgs);
            }
            PropertyCopier.copyBeanProperties(type, enhanced, original);
            if (lazyLoader.size() > 0) {
              return new SerialStateHolder(original, lazyLoader.getPropertyNames(), objectFactory,
                  constructorArgTypes, constructorArgs);
            } else {
              return original;
            }
          } else {
            if (lazyLoader.size() > 0 && !FINALIZE_METHOD.equals(methodName)) {
              if (aggressive || objectMethods.contains(methodName)) {
                lazyLoader.loadAll();
              } else if (PropertyNamer.isProperty(methodName)) {
                final String property = PropertyNamer.methodToProperty(methodName);
                if (lazyLoader.hasLoader(property)) {
                  lazyLoader.load(property);
                }
              }
            }
            return methodProxy.invokeSuper(enhanced, args);
          }
        }
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }
  }

}
