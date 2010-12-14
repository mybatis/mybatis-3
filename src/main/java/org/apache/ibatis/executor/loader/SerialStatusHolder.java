package org.apache.ibatis.executor.loader;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.ibatis.reflection.factory.ObjectFactory;

public class SerialStatusHolder implements Serializable {
  
  private static final long serialVersionUID = 9018585337519878124L;
  private Object userBean;
  private String[] unloadedProperties;
  private ObjectFactory objectFactory;

  public SerialStatusHolder(final Object userBean, final String[] unloadedProperties, final ObjectFactory objectFactory) {
    this.userBean = userBean;
    this.unloadedProperties = unloadedProperties;
    this.objectFactory = objectFactory;
  }
  
  protected Object readResolve() throws ObjectStreamException {
    return DeserializedObjectProxy.createProxy(userBean, unloadedProperties, objectFactory);
  }

}
