package org.apache.ibatis.executor.loader;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InterfaceMaker;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.objectweb.asm.Type;

public class DeserializedObjectProxy {

  private static final String FINALIZE_METHOD = "finalize";
  private static final String WRITE_REPLACE_METHOD = "writeReplace";

  public static Object createProxy(Object target, Set<String> unloadedProperties, ObjectFactory objectFactory,
      List<Class> constructorArgTypes, List<Object> constructorArgs) {
    return DeserializationProxyImpl.createProxy(target, unloadedProperties, objectFactory, constructorArgTypes,
        constructorArgs);
  }

  private static class DeserializationProxyImpl implements MethodInterceptor {

    private Class type;
    private Set<String> unloadedProperties;
    private ObjectFactory objectFactory;
    private List<Class> constructorArgTypes;
    private List<Object> constructorArgs;

    private DeserializationProxyImpl(Class type, Set<String> unloadedProperties, ObjectFactory objectFactory,
        List<Class> constructorArgTypes, List<Object> constructorArgs) {
      this.type = type;
      this.unloadedProperties = unloadedProperties;
      this.objectFactory = objectFactory;
      this.constructorArgTypes = constructorArgTypes;
      this.constructorArgs = constructorArgs;
    }

    public static Object createProxy(Object target, Set<String> unloadedProperties, ObjectFactory objectFactory,
        List<Class> constructorArgTypes, List<Object> constructorArgs) {
      final Class type = target.getClass();
      DeserializationProxyImpl proxy = new DeserializationProxyImpl(type, unloadedProperties, objectFactory,
          constructorArgTypes, constructorArgs);
      Enhancer enhancer = new Enhancer();
      enhancer.setCallback(proxy);
      enhancer.setSuperclass(type);

      try {
        type.getDeclaredMethod(WRITE_REPLACE_METHOD);
        // don´t warn if a writeReplace is found, the bean was once serialized so it is ok
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

    public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      final String methodName = method.getName();
      try {
        if (WRITE_REPLACE_METHOD.equals(methodName)) {
          Object original = null;
          if (constructorArgTypes.isEmpty()) {
            original = objectFactory.create(type);
          } else {
            original = objectFactory.create(type, constructorArgTypes, constructorArgs);
          }
          PropertyCopier.copyBeanProperties(type, enhanced, original);
          return new SerialStateHolder(original, unloadedProperties, objectFactory, constructorArgTypes,
              constructorArgs);
        } else {
          if (!FINALIZE_METHOD.equals(methodName) && PropertyNamer.isProperty(methodName)) {
            final String property = PropertyNamer.methodToProperty(methodName);
            if (unloadedProperties.contains(property.toUpperCase(Locale.ENGLISH))) {
              throw new ExecutorException("An attempt has been made to read a not loaded lazy property '" + property
                  + "' of a disconnected object");
            }
          }
          return methodProxy.invokeSuper(enhanced, args);
        }
      } catch (Throwable t) {
        throw ExceptionUtil.unwrapThrowable(t);
      }
    }
  }

}
