/*
 * Copyright 2013 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.executor.loader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.reflection.factory.ObjectFactory;

public abstract class AbstractSerialStateHolder implements Externalizable {

  private static final long serialVersionUID = 20140307;
  private static final ThreadLocal<ObjectOutputStream> stream = new ThreadLocal<ObjectOutputStream>();
  private static final ThreadLocal<Map<Object, Integer>> handlesWrite = new ThreadLocal<Map<Object, Integer>>() {

    @Override
    protected Map<Object, Integer> initialValue() {
      return new IdentityHashMap<Object, Integer>();
    }

  };
  private static final ThreadLocal<Map<Integer, Object>> handlesRead = new ThreadLocal<Map<Integer, Object>>() {

    @Override
    protected Map<Integer, Object> initialValue() {
      return new IdentityHashMap<Integer, Object>();
    }

  };
  private Object enhanced;
  private byte[] userBeanBytes = new byte[0];
  private Object userBean;
  private Map<String, ResultLoaderMap.LoadPair> unloadedProperties;
  private ObjectFactory objectFactory;
  private Class<?>[] constructorArgTypes;
  private Object[] constructorArgs;

  public AbstractSerialStateHolder() {
  }

  public AbstractSerialStateHolder(
          final Object enhanced,
          final Object userBean,
          final Map<String, ResultLoaderMap.LoadPair> unloadedProperties,
          final ObjectFactory objectFactory,
          List<Class<?>> constructorArgTypes,
          List<Object> constructorArgs) {
    this.enhanced = enhanced;
    this.userBean = userBean;
    this.unloadedProperties = new HashMap<String, ResultLoaderMap.LoadPair>(unloadedProperties);
    this.objectFactory = objectFactory;
    this.constructorArgTypes = constructorArgTypes.toArray(new Class<?>[constructorArgTypes.size()]);
    this.constructorArgs = constructorArgs.toArray(new Object[constructorArgs.size()]);
  }

  private Map<Field, Object> findFields(final Object source) {
    final Map<Field, Object> map = new IdentityHashMap<Field, Object>();
    for (Class<?> c = source.getClass(); c != null; c = c.getSuperclass()) {
      for (Field f : c.getDeclaredFields()) {
        if (Modifier.isStatic(f.getModifiers())) {
          continue;
        }
        if (Modifier.isTransient(f.getModifiers())) {
          continue;
        }

        f.setAccessible(true);
        try {
          map.put(f, f.get(source));
        } catch (final IllegalAccessException ex) {
          // no-op
        }
      }
    }

    return map;
  }

  @Override
  public final void writeExternal(final ObjectOutput out) throws IOException {
    boolean firstRound = false;
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream os = stream.get();
    Map<Object, Integer> hand = handlesWrite.get();
    if (os == null) {
      os = new ObjectOutputStream(baos);
      firstRound = true;
      stream.set(os);
    }

    final Integer handle = hand.get(enhanced);
    hand.put(enhanced, handle == null ? hand.size() : handle);

    os.writeObject(userBean.getClass());
    os.writeObject(hand.get(enhanced));

    if (handle == null) {
      os.writeObject(this.unloadedProperties);
      os.writeObject(this.constructorArgTypes);
      os.writeObject(this.constructorArgs);
      os.writeObject(this.objectFactory);

      final Map<Field, Object> fields = findFields(userBean);
      os.writeObject(fields.size());
      for (final Map.Entry<Field, Object> e : fields.entrySet()) {
        os.writeObject(e.getKey().getDeclaringClass());
        os.writeObject(e.getKey().getName());
        os.writeObject(e.getValue());
      }
    }

    final byte[] bytes = baos.toByteArray();
    out.writeObject(bytes);

    if (firstRound) {
      stream.remove();
      handlesWrite.remove();
    }
  }

  @Override
  public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    final Object data = in.readObject();
    if (data.getClass().isArray()) {
      this.userBeanBytes = (byte[]) data;
    } else {
      if (data instanceof Class<?> == false) {
        throw new IOException("Invalid stream. Unexpected " + data);
      }
      this.userBean = this.readUserBean((Class<?>) data, in);
    }
  }

  @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch", "ThrowableInitCause"})
  private Object readUserBean(final Class<?> type, final ObjectInput in) throws IOException, ClassNotFoundException {
    final Integer handle = (Integer) in.readObject();
    final Map<Integer, Object> hand = handlesRead.get();
    if (hand.containsKey(handle)) {
      return hand.get(handle);
    }

    @SuppressWarnings("unchecked")
    final Map<String, ResultLoaderMap.LoadPair> desUnloaded = (Map<String, ResultLoaderMap.LoadPair>) in.readObject();
    final Class<?>[] desCtorTypes = (Class<?>[]) in.readObject();
    final Object[] desCtorArgs = (Object[]) in.readObject();
    final ObjectFactory desObjectFactory = (ObjectFactory) in.readObject();

    final Object bean = desObjectFactory.create(type, Arrays.asList(desCtorTypes), Arrays.asList(desCtorArgs));
    final Object ubean = desUnloaded.isEmpty() ? bean : newDeserializationProxy(bean, desCtorTypes, desCtorArgs, desObjectFactory, desUnloaded);
    hand.put(handle, ubean);

    final Integer fieldsCount = (Integer) in.readObject();
    for (int i = 0; i < fieldsCount; ++i) {
      final Class<?> c = (Class<?>) in.readObject();
      final String n = (String) in.readObject();
      try {
        final Field f = c.getDeclaredField(n);

        Object v = in.readObject();
        if (v instanceof AbstractSerialStateHolder) {
          v = newCyclicReferenceMarker(f.getType(), desCtorTypes, desCtorArgs, (AbstractSerialStateHolder) v);
          assert f.getType().isInstance(v) : "Marker not instance of " + f.getType();
          assert v instanceof CyclicReferenceMarker : "Marker not instanceof " + CyclicReferenceMarker.class;
        }

        f.setAccessible(true);
        f.set(bean, v);
      } catch (final Exception ex) {
        throw (IOException) new IOException(ex.getLocalizedMessage()).initCause(ex);
      }
    }

    return ubean;
  }

  protected final Object readResolve() throws ObjectStreamException {
    /* Second run */
    if (this.userBean != null && this.userBeanBytes.length == 0) {
      return this.userBean;
    }

    /* First run */
    try {
      final ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(this.userBeanBytes));
      final Class<?> userBeanType = (Class<?>) in.readObject();

      this.userBean = this.readUserBean(userBeanType, in);
      this.replaceCyclicReferenceMarkers(this.userBean, new HashSet<Object>());

      return this.userBean;
    } catch (final IOException ex) {
      throw (ObjectStreamException) new StreamCorruptedException().initCause(ex);
    } catch (final ClassNotFoundException ex) {
      throw (ObjectStreamException) new InvalidClassException(ex.getLocalizedMessage()).initCause(ex);
    } finally {
      handlesRead.remove();
    }
  }

  private void replaceCyclicReferenceMarkers(final Object o, final Set<Object> set) {
    for (final Map.Entry<Field, Object> e : findFields(o).entrySet()) {
      final Object value = e.getValue();
      if (value == null) {
        continue;
      }

      if (value instanceof CyclicReferenceMarker) {
        final Field f = e.getKey();
        f.setAccessible(true);

        final AbstractSerialStateHolder assh = ((CyclicReferenceMarker) e.getValue()).getSerialStateHolder();
        try {
          f.set(o, assh.userBean);
        } catch (final Exception ex) {
          throw new IllegalStateException(ex);
        }
      } else if (value instanceof Collection<?>) {
        for (Object ce : ((Collection<?>) value)) {
          this.replaceCyclicReferenceMarkers(ce, set);
        }
      } else if (value instanceof Map<?, ?>) {
        for (Map.Entry<?, ?> ce : ((Map<?, ?>) value).entrySet()) {
          this.replaceCyclicReferenceMarkers(ce.getKey(), set);
          this.replaceCyclicReferenceMarkers(ce.getValue(), set);
        }
      } else {
        if (set.add(value)) {
          this.replaceCyclicReferenceMarkers(value, set);
        }
      }
    }
  }

  protected abstract Object newDeserializationProxy(Object target, Class<?>[] ctorTypes, Object[] ctorArgs,
          ObjectFactory factory, Map<String, ResultLoaderMap.LoadPair> unloaded);

  protected abstract Object newCyclicReferenceMarker(Class<?> target, Class<?>[] ctorTypes, Object[] ctorArgs,
          AbstractSerialStateHolder ssh);

  protected static interface CyclicReferenceMarker {

    AbstractSerialStateHolder getSerialStateHolder();
  }
}
