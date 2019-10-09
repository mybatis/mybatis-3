/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.cache;

/**
 * @author Clinton Begin
 * @deprecated Since 3.5.3, This class never used and will be removed future version.
 */
@Deprecated
public final class NullCacheKey extends CacheKey {

  private static final long serialVersionUID = 3704229911977019465L;

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
