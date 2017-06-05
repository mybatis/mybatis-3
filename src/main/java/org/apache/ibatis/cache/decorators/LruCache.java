/**
 * Copyright 2009-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.ibatis.cache.Cache;

/**
 * Lru (least recently used) cache decorator
 *
 * @author Clinton Begin
 */
// TODO: 17/4/18 by zmyer
public class LruCache implements Cache {
    //缓存委托对象
    private final Cache delegate;
    //键值映射表
    private Map<Object, Object> keyMap;
    //最老的key
    private Object eldestKey;

    // TODO: 17/4/26 by zmyer
    public LruCache(Cache delegate) {
        this.delegate = delegate;
        setSize(1024);
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public String getId() {
        return delegate.getId();
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public int getSize() {
        return delegate.getSize();
    }

    // TODO: 17/4/26 by zmyer
    public void setSize(final int size) {
        keyMap = new LinkedHashMap<Object, Object>(size, .75F, true) {
            private static final long serialVersionUID = 4267176411845948333L;

            // TODO: 17/4/26 by zmyer
            @Override
            protected boolean removeEldestEntry(Map.Entry<Object, Object> eldest) {
                boolean tooBig = size() > size;
                if (tooBig) {
                    eldestKey = eldest.getKey();
                }
                return tooBig;
            }
        };
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public void putObject(Object key, Object value) {
        delegate.putObject(key, value);
        cycleKeyList(key);
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public Object getObject(Object key) {
        keyMap.get(key); //touch
        return delegate.getObject(key);
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public Object removeObject(Object key) {
        return delegate.removeObject(key);
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public void clear() {
        delegate.clear();
        keyMap.clear();
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    // TODO: 17/4/26 by zmyer
    private void cycleKeyList(Object key) {
        keyMap.put(key, key);
        if (eldestKey != null) {
            delegate.removeObject(eldestKey);
            eldestKey = null;
        }
    }

}
