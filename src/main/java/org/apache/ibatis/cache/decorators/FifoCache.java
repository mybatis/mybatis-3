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

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.ibatis.cache.Cache;

/**
 * FIFO (first in, first out) cache decorator
 *
 * @author Clinton Begin
 */
// TODO: 17/4/26 by zmyer
public class FifoCache implements Cache {
    //缓存委托对象
    private final Cache delegate;
    //键值列表
    private Deque<Object> keyList;
    //键值列表长度
    private int size;

    // TODO: 17/4/26 by zmyer
    public FifoCache(Cache delegate) {
        this.delegate = delegate;
        this.keyList = new LinkedList<Object>();
        this.size = 1024;
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
    public void setSize(int size) {
        this.size = size;
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public void putObject(Object key, Object value) {
        cycleKeyList(key);
        delegate.putObject(key, value);
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public Object getObject(Object key) {
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
        keyList.clear();
    }

    // TODO: 17/4/26 by zmyer
    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    // TODO: 17/4/26 by zmyer
    private void cycleKeyList(Object key) {
        keyList.addLast(key);
        if (keyList.size() > size) {
            Object oldestKey = keyList.removeFirst();
            delegate.removeObject(oldestKey);
        }
    }
}
