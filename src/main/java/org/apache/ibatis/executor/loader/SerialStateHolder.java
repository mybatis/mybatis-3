package org.apache.ibatis.executor.loader;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.reflection.factory.ObjectFactory;

class SerialStateHolder implements Serializable {
  
  private static final long serialVersionUID = 9018585337519878124L;
  private Object userBean;
  private String[] unloadedProperties;
  private ObjectFactory objectFactory;
  private Class[] constructorArgTypes;
  private Object[] constructorArgs;

  public SerialStateHolder(final Object userBean, final Set<String> unloadedProperties,
      final ObjectFactory objectFactory, List<Class> constructorArgTypes, List<Object> constructorArgs) {
    this.userBean = userBean;
    this.unloadedProperties = unloadedProperties.toArray(new String[unloadedProperties.size()]);
    this.objectFactory = objectFactory;
    this.constructorArgTypes = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
    this.constructorArgs = constructorArgs.toArray(new Object[constructorArgs.size()]);
  }
  
  protected Object readResolve() throws ObjectStreamException {
    Set<String> arrayProps = new HashSet<String>(Arrays.asList(this.unloadedProperties));
    List<Class> arrayTypes = Arrays.asList(this.constructorArgTypes);
    List<Object> arrayValues = Arrays.asList(this.constructorArgs);
    return ResultObjectProxy.createDeserializationProxy(userBean, arrayProps, objectFactory, arrayTypes, arrayValues);
  }

}
