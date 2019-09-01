/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author andyslin
 */
public class GenericRegistry {

  /**
   * 接口实现类注册器
   */
  public interface InstanceRegistry<I> {

    /**
     * 注册实现类的实例
     *
     * @param implInstance
     */
    void register(I implInstance);

    /**
     * 获取实现类的实例
     *
     * @return
     */
    I get();
  }

  /**
   * 命名对象注册器接口
   *
   * @param <N>
   */
  public interface NamedRegistry<N extends Named> {

    /**
     * 注册命名对象
     *
     * @param nameds
     */
    void register(N... nameds);

    /**
     * 注册命名对象
     *
     * @param nameds
     */
    default void register(Collection<N> nameds) {
      if (null != nameds && !nameds.isEmpty()) {
        nameds.stream().filter(Objects::nonNull).forEach(this::register);
      }
    }

    /**
     * 获取命名对象
     *
     * @param name
     * @return
     */
    N get(String name);
  }

  /**
   * 排序对象注册器接口
   *
   * @param <O>
   */
  public interface OrderedRegistry<O extends Ordered> {

    /**
     * 注册排序对象
     *
     * @param ordereds
     */
    void register(O... ordereds);

    /**
     * 注册排序对象
     *
     * @param ordereds
     */
    default void register(Collection<O> ordereds) {
      if (null != ordereds && !ordereds.isEmpty()) {
        ordereds.stream().filter(Objects::nonNull).forEach(this::register);
      }
    }

    /**
     * 获取所有已注册的排序对象
     *
     * @return
     */
    List<O> get();

    /**
     * 获取第一个已注册的对象
     *
     * @return
     */
    default O getFirst() {
      List<O> list = get();
      return (null == list || list.isEmpty()) ? null : list.get(0);
    }
  }

  /**
   * 接口实现类缓存
   */
  private static final Map<Class<?>, Object> instances = new HashMap<>();

  /**
   * 命名对象缓存
   */
  private static final Map<Class<? extends Named>, Map<String, Named>> named = new HashMap<>();

  /**
   * 排序对象缓存
   */
  private static final Map<Class<?>, List<?>> ordered = new HashMap<>();

  private static final Comparator<Ordered> ORDERED_COMPARATOR = (o1, o2) -> o1.getOrder() - o2.getOrder();

  /**
   * 注册接口的实现类实例
   *
   * @param interfaceClass
   * @param implInstance
   * @param <I>
   */
  public static <I> void registerInstance(Class<I> interfaceClass, I implInstance) {
    synchronized (instances) {
      instances.put(interfaceClass, implInstance);
    }
  }

  /**
   * 获取接口的实现类实例
   *
   * @param interfaceClass
   * @param <I>
   * @return
   */
  public static <I> I getInstance(Class<I> interfaceClass) {
    Object o = instances.get(interfaceClass);
    return interfaceClass.cast(o);
  }

  /**
   * 获取接口实例注册器
   *
   * @param interfaceClass
   * @param <I>
   * @return
   */
  public static <I> InstanceRegistry<I> getInstanceRegistry(Class<I> interfaceClass) {
    return new InstanceRegistry<I>() {
      @Override
      public void register(I implInstance) {
        registerInstance(interfaceClass, implInstance);
      }

      @Override
      public I get() {
        return getInstance(interfaceClass);
      }
    };
  }

  /**
   * 注册命名对象
   *
   * @param cls
   * @param nameds
   * @param <N>
   */
  public static <N extends Named> void registerNameds(Class<N> cls, N... nameds) {
    Map<String, Named> cache = getNamedMap(cls);
    synchronized (cache) {
      Arrays.stream(nameds).filter(Objects::nonNull).forEach(n -> cache.put(n.getName(), n));
    }
  }

  /**
   * 获取命名对象
   *
   * @param cls
   * @param name
   * @param <N>
   * @return
   */
  public static <N extends Named> N getNamed(Class<N> cls, String name) {
    Map<String, Named> cache = getNamedMap(cls);
    Named named = cache.get(name);
    return cls.cast(named);
  }

  /**
   * 获取命名对象注册器
   *
   * @param cls
   * @param <N>
   * @return
   */
  public static <N extends Named> NamedRegistry<N> getNamedRegistry(Class<N> cls) {
    return new NamedRegistry<N>() {
      @Override
      public void register(N... nameds) {
        registerNameds(cls, nameds);
      }

      @Override
      public N get(String name) {
        return getNamed(cls, name);
      }
    };
  }

  private static <N extends Named> Map<String, Named> getNamedMap(Class<N> cls) {
    Map<String, Named> cache = named.get(cls);
    if (null == cache) {
      synchronized (named) {
        cache = named.get(cls);
        if (null == cache) {
          cache = new HashMap<>();
          named.put(cls, cache);
        }
      }
    }
    return cache;
  }

  /**
   * 注册排序对象
   *
   * @param cls
   * @param ordereds
   * @param <O>
   */
  public static <O extends Ordered> void registerOrdereds(Class<O> cls, O... ordereds) {
    List<O> list = getOrdered(cls);
    synchronized (list) {
      Arrays.stream(ordereds).filter(Objects::nonNull).forEach(list::add);
      list.sort(ORDERED_COMPARATOR);
    }
  }

  /**
   * 获取排序对象列表
   *
   * @param cls
   * @param <O>
   * @return
   */
  public static <O extends Ordered> List<O> getOrdered(Class<O> cls) {
    List<O> list = (List<O>) ordered.get(cls);
    if (null == list) {
      synchronized (ordered) {
        list = (List<O>) ordered.get(cls);
        if (null == list) {
          list = new ArrayList<>();
          ordered.put(cls, list);
        }
      }
    }
    return list;
  }

  /**
   * 获取排序对象注册器
   *
   * @param cls
   * @param <O>
   * @return
   */
  public static <O extends Ordered> OrderedRegistry<O> getOrderedRegistry(Class<O> cls) {
    return new OrderedRegistry<O>() {
      @Override
      public void register(O... ordereds) {
        registerOrdereds(cls, ordereds);
      }

      @Override
      public List<O> get() {
        return getOrdered(cls);
      }
    };
  }
}
