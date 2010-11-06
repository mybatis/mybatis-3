package org.apache.ibatis.cache;


public final class NullCacheKey extends CacheKey {

  public NullCacheKey() {
    super();
  }

  @Override
  public void update(Object object) {
    throw new CacheException("Not allowed to update a NullCacheKey instance.");
  }

  @Override
  public void updateAll(Object[] objects) {
    throw new CacheException("Not allowed to update a NullCacheKey instance.");
  }
}
