/**
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.cache.decorators;


import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheDecorator;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * @author Clinton Begin
 */
public class LoggingCache extends CacheDecorator {

  private Log log;  
  protected volatile int requests = 0;
  protected volatile int hits = 0;

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

  @Override
  public int hashCode() {
    return getDelegate().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return getDelegate().equals(obj);
  }

  private double getHitRatio() {
    return (double) hits / (double) requests;
  }

  public int getRequests() {
    return requests;
  }

  public int getHits() {
    return hits;
  }
}
