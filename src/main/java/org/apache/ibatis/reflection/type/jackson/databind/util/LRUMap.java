package org.apache.ibatis.reflection.type.jackson.databind.util;

import org.apache.ibatis.reflection.type.jackson.databind.util.internal.PrivateMaxEntriesMap;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Helper for simple bounded maps used for reusing lookup values.
 *<p>
 * Note that serialization behavior is such that contents are NOT serialized,
 * on assumption that all use cases are for caching where persistence
 * does not make sense. The only thing serialized is the cache size of Map.
 *<p>
 * NOTE: since Jackson 2.14, the implementation evicts the least recently used
 * entry when max size is reached.
 *<p>
 * Since Jackson 2.12, there has been pluggable {@link LookupCache} interface which
 * allows users, frameworks, provide their own cache implementations.
 */
public class LRUMap<K,V>
    implements LookupCache<K,V>, // since 2.12
        java.io.Serializable
{
    private static final long serialVersionUID = 2L;

    protected final int _initialEntries;
    protected final int _maxEntries;
    protected final transient PrivateMaxEntriesMap<K,V> _map;

    public LRUMap(int initialEntries, int maxEntries)
    {
        _initialEntries = initialEntries;
        _maxEntries = maxEntries;
        // We'll use concurrency level of 4, seems reasonable
        _map = new PrivateMaxEntriesMap.Builder<K, V>()
                .initialCapacity(initialEntries)
                .maximumCapacity(maxEntries)
                .concurrencyLevel(4)
                .build();
    }

    @Override
    public V put(K key, V value) {
        return _map.put(key, value);
    }

    /**
     * @since 2.5
     */
    @Override
    public V putIfAbsent(K key, V value) {
        return _map.putIfAbsent(key, value);
    }

    // NOTE: key is of type Object only to retain binary backwards-compatibility
    @Override
    public V get(Object key) { return _map.get(key); }

    @Override
    public void clear() { _map.clear(); }

    @Override
    public int size() { return _map.size(); }

    /*
    /**********************************************************************
    /* Extended API (2.14)
    /**********************************************************************
     */

    public void contents(BiConsumer<K,V> consumer) {
        for (Map.Entry<K,V> entry : _map.entrySet()) {
            consumer.accept(entry.getKey(), entry.getValue());
        }
    }

    /*
    /**********************************************************************
    /* Serializable overrides
    /**********************************************************************
     */

    protected Object readResolve() {
        return new LRUMap<K,V>(_initialEntries, _maxEntries);
    }
}
