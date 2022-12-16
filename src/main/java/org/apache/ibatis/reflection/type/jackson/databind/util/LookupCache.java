package org.apache.ibatis.reflection.type.jackson.databind.util;

/**
 * An interface describing the required API for the Jackson-databind Type cache.
 *<p>
 * Note that while interface itself does not specify synchronization requirements for
 * implementations, specific use cases do. Typically implementations are
 * expected to be thread-safe, that is, to handle synchronization.
 *
 * @since 2.12 (for forwards-compatiblity with 3.0)
 */
public interface LookupCache <K,V>
{
    /**
     * @return Number of entries currently in cache: may be approximate, only
     *    to be used for diagnostics, metrics reporting
     */
    int size();

    /**
     * NOTE: key is of type Object only to retain binary backwards-compatibility
     *
     * @param key
     * @return value associated with key (can return null)
     */
    V get(Object key);

    V put(K key, V value);

    V putIfAbsent(K key, V value);

    /**
     * Method for removing all contents this cache has.
     */
    void clear();
}
