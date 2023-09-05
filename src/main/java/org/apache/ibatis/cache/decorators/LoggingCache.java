/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.impl.DelegateCache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * @author Clinton Begin
 */
public class LoggingCache extends DelegateCache {

  private final Log log;
  protected int requests;
  protected int hits;

  public LoggingCache(Cache delegate) {
    super(delegate);
    this.log = LogFactory.getLog(getId());
  }

  @Override
  public Object getObject(Object key) {
    requests++;
    final Object value = super.getObject(key);
    if (value != null) {
      hits++;
    }
    if (log.isDebugEnabled()) {
      log.debug("Cache Hit Ratio [" + getId() + "]: " + getHitRatio());
    }
    return value;
  }

  private double getHitRatio() {
    return (double) hits / (double) requests;
  }

}
