package org.apache.ibatis.annotations;

import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.impl.PerpetualCache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CacheNamespace {
  public abstract Class<? extends org.apache.ibatis.cache.Cache> implementation() default PerpetualCache.class;

  public abstract Class<? extends org.apache.ibatis.cache.Cache> eviction() default LruCache.class;

  public abstract long flushInterval() default 3600000;

  public abstract int size() default 1000;

  public abstract boolean readWrite() default true;
}
