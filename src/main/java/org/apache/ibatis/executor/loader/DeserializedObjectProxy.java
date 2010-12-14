package org.apache.ibatis.executor.loader;

import java.io.ObjectStreamException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InterfaceMaker;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.objectweb.asm.Type;

public class DeserializedObjectProxy {

  private static final Log log = LogFactory.getLog(DeserializedObjectProxy.class);

  private static final String FINALIZE_METHOD = "finalize";
  private static final String WRITE_REPLACE_METHOD = "writeReplace";

  public static Object createProxy(Object target, String[] unloadedProperties, ObjectFactory objectFactory) {
    return DeserializationProxyImpl.createProxy(target, unloadedProperties, objectFactory);
  }

  private static class DeserializationProxyImpl implements MethodInterceptor {

    private Class type;
    private Set<String> unloadedProperties;
    private ObjectFactory objectFactory;

    private DeserializationProxyImpl(Class type, String[] unloadedProperties, ObjectFactory objectFactory) {
      this.type = type;
      this.unloadedProperties = new HashSet<String>();
      for (String s : unloadedProperties) {
        this.unloadedProperties.add(s);
      }
      this.objectFactory = objectFactory;
    }

    public static Object createProxy(Object target, String[] unloadedProperties, ObjectFactory objectFactory) {
      final Class type = target.getClass();
      DeserializationProxyImpl proxy = new DeserializationProxyImpl(type, unloadedProperties, objectFactory);
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

      final Object enhanced = enhancer.create();
      PropertyCopier.copyBeanProperties(type, target, enhanced);
      return enhanced;
    }
    
    public Object intercept(Object enhanced, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      final String methodName = method.getName();
      try {
        if (WRITE_REPLACE_METHOD.equals(methodName)) {
          Object original = objectFactory.create(type);
          PropertyCopier.copyBeanProperties(type, enhanced, original);
          return new SerialStatusHolder(original, unloadedProperties.toArray(new String[unloadedProperties.size()]), objectFactory);
        } else {
          if (!FINALIZE_METHOD.equals(methodName) && PropertyNamer.isProperty(methodName)) {
            final String property = PropertyNamer.methodToProperty(methodName);
            if (unloadedProperties.contains(property.toUpperCase(Locale.ENGLISH))) {
              throw new ExecutorException("An attempt has been made to read a not loaded lazy property '" 
                  + property + "' of a disconnected object");
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
