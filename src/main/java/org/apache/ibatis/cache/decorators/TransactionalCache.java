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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * The 2nd level cache transactional buffer.
 *
 * This class holds all cache entries that are to be added to the 2nd level cache during a Session.
 * Entries are sent to the cache when commit is called or discarded if the Session is rolled back. 
 * Blocking cache support has been added. Therefore any get() that returns a cache miss 
 * will be followed by a put() so any lock associated with the key can be released. 
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
// TODO: 17/4/27 by zmyer
public class TransactionalCache implements Cache {

    private static final Log log = LogFactory.getLog(TransactionalCache.class);
    //緩存代理
    private Cache delegate;
    //是否提交之后清理
    private boolean clearOnCommit;
    //对象提交映射表
    private Map<Object, Object> entriesToAddOnCommit;
    //缓存失效对象集合
    private Set<Object> entriesMissedInCache;

    // TODO: 17/4/27 by zmyer
    public TransactionalCache(Cache delegate) {
        this.delegate = delegate;
        this.clearOnCommit = false;
        this.entriesToAddOnCommit = new HashMap<Object, Object>();
        this.entriesMissedInCache = new HashSet<Object>();
    }

    // TODO: 17/4/27 by zmyer
    @Override
    public String getId() {
        return delegate.getId();
    }

    // TODO: 17/4/27 by zmyer
    @Override
    public int getSize() {
        return delegate.getSize();
    }

    // TODO: 17/4/27 by zmyer
    @Override
    public Object getObject(Object key) {
        // issue #116
        Object object = delegate.getObject(key);
        if (object == null) {
            //如果缓存失效,记录下该key
            entriesMissedInCache.add(key);
        }
        // issue #146
        if (clearOnCommit) {
            return null;
        } else {
            return object;
        }
    }

    // TODO: 17/4/27 by zmyer
    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    // TODO: 17/4/27 by zmyer
    @Override
    public void putObject(Object key, Object object) {
        entriesToAddOnCommit.put(key, object);
    }

    // TODO: 17/4/27 by zmyer
    @Override
    public Object removeObject(Object key) {
        return null;
    }

    // TODO: 17/4/27 by zmyer
    @Override
    public void clear() {
        clearOnCommit = true;
        entriesToAddOnCommit.clear();
    }

    // TODO: 17/4/27 by zmyer
    public void commit() {
        if (clearOnCommit) {
            delegate.clear();
        }
        //刷新挂起的实体集合
        flushPendingEntries();
        //重置
        reset();
    }

    // TODO: 17/4/27 by zmyer
    public void rollback() {
        unlockMissedEntries();
        reset();
    }

    // TODO: 17/4/27 by zmyer
    private void reset() {
        clearOnCommit = false;
        entriesToAddOnCommit.clear();
        entriesMissedInCache.clear();
    }

    // TODO: 17/4/27 by zmyer
    private void flushPendingEntries() {
        for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
            delegate.putObject(entry.getKey(), entry.getValue());
        }
        for (Object entry : entriesMissedInCache) {
            if (!entriesToAddOnCommit.containsKey(entry)) {
                delegate.putObject(entry, null);
            }
        }
    }

    // TODO: 17/4/27 by zmyer
    private void unlockMissedEntries() {
        for (Object entry : entriesMissedInCache) {
            try {
                //从缓存中删除实体
                delegate.removeObject(entry);
            } catch (Exception e) {
                log.warn("Unexpected exception while notifiying a rollback to the cache adapter."
                    + "Consider upgrading your cache adapter to the latest version.  Cause: " + e);
            }
        }
    }
}
