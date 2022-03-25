/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.executor.loader;

import org.apache.ibatis.io.SerialFilterChecker;
import org.apache.ibatis.reflection.factory.ObjectFactory;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eduardo Macarron
 * @author Franta Mejta
 */
public abstract class AbstractSerialStateHolder implements Externalizable {

  private static final long serialVersionUID = 8940388717901644661L;
  private static final ThreadLocal<ObjectOutputStream> stream = new ThreadLocal<>();
  private byte[] userBeanBytes = new byte[0];
  private Object userBean;
  private Map<String, ResultLoaderMap.LoadPair> unloadedProperties;
  private ObjectFactory objectFactory;
  private Class<?>[] constructorArgTypes;
  private Object[] constructorArgs;

  public AbstractSerialStateHolder() {
  }

  public AbstractSerialStateHolder(
          final Object userBean,
          final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
          final ObjectFactory objectFactory,
          List<Class<?>> constructorArgTypes,
          List<Object> constructorArgs) {
    this.userBean = userBean;
    this.unloadedProperties = new HashMap<>(unloadedProperties);
    this.objectFactory = objectFactory;
    this.constructorArgTypes = constructorArgTypes.toArray(new Class<?>[0]);
    this.constructorArgs = constructorArgs.toArray(new Object[0]);
  }

  @Override
  public final void writeExternal(final ObjectOutput out) throws IOException {
    boolean firstRound = false;
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream objectOutputStream = stream.get();
    if (objectOutputStream == null) {
      objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      firstRound = true;
      stream.set(objectOutputStream);
    }

    objectOutputStream.writeObject(this.userBean);
    objectOutputStream.writeObject(this.unloadedProperties);
    objectOutputStream.writeObject(this.objectFactory);
    objectOutputStream.writeObject(this.constructorArgTypes);
    objectOutputStream.writeObject(this.constructorArgs);

    final byte[] bytes = byteArrayOutputStream.toByteArray();
    out.writeObject(bytes);

    if (firstRound) {
      stream.remove();
    }
  }

  @Override
  public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    final Object data = in.readObject();
    if (data.getClass().isArray()) {
      this.userBeanBytes = (byte[]) data;
    } else {
      this.userBean = data;
    }
  }

  @SuppressWarnings("unchecked")
  protected final Object readResolve() throws ObjectStreamException {
    /* Second run */
    if (this.userBean != null && this.userBeanBytes.length == 0) {
      return this.userBean;
    }

    SerialFilterChecker.check();

    /* First run */
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(this.userBeanBytes))) {
      this.userBean = in.readObject();
      this.unloadedProperties = (Map<String, ResultLoaderMap.LoadPair>) in.readObject();
      this.objectFactory = (ObjectFactory) in.readObject();
      this.constructorArgTypes = (Class<?>[]) in.readObject();
      this.constructorArgs = (Object[]) in.readObject();
    } catch (final IOException ex) {
      throw (ObjectStreamException) new StreamCorruptedException().initCause(ex);
    } catch (final ClassNotFoundException ex) {
      throw (ObjectStreamException) new InvalidClassException(ex.getLocalizedMessage()).initCause(ex);
    }

    final Map<String, ResultLoaderMap.LoadPair> arrayProps = new HashMap<>(this.unloadedProperties);
    final List<Class<?>> arrayTypes = Arrays.asList(this.constructorArgTypes);
    final List<Object> arrayValues = Arrays.asList(this.constructorArgs);

    return this.createDeserializationProxy(userBean, arrayProps, objectFactory, arrayTypes, arrayValues);
  }

  protected abstract Object createDeserializationProxy(Object target, Map<String, ResultLoaderMap.LoadPair> unloadedProperties, ObjectFactory objectFactory,
          List<Class<?>> constructorArgTypes, List<Object> constructorArgs);
}
