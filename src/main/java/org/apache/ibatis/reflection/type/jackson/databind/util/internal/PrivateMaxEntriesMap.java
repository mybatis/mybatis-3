/*
 * Copyright 2010 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.reflection.type.jackson.databind.util.internal;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A hash table supporting full concurrency of retrievals, adjustable expected
 * concurrency for updates, and a maximum capacity to bound the map by. This
 * implementation differs from {@link ConcurrentHashMap} in that it maintains a
 * page replacement algorithm that is used to evict an entry when the map has
 * exceeded its capacity. Unlike the <tt>Java Collections Framework</tt>, this
 * map does not have a publicly visible constructor and instances are created
 * through a {@link Builder}.
 * <p>
 * An entry is evicted from the map when the entry size exceeds
 * its <tt>maximum capacity</tt> threshold.
 * <p>
 * An {@code EvictionListener} may be supplied for notification when an entry
 * is evicted from the map. This listener is invoked on a caller's thread and
 * will not block other threads from operating on the map. An implementation
 * should be aware that the caller's thread will not expect long execution
 * times or failures as a side effect of the listener being notified. Execution
 * safety and a fast turn around time can be achieved by performing the
 * operation asynchronously, such as by submitting a task to an
 * {@link java.util.concurrent.ExecutorService}.
 * <p>
 * The <tt>concurrency level</tt> determines the number of threads that can
 * concurrently modify the table. Using a significantly higher or lower value
 * than needed can waste space or lead to thread contention, but an estimate
 * within an order of magnitude of the ideal value does not usually have a
 * noticeable impact. Because placement in hash tables is essentially random,
 * the actual concurrency will vary.
 * <p>
 * This class and its views and iterators implement all of the
 * <em>optional</em> methods of the {@link Map} and {@link Iterator}
 * interfaces.
 * <p>
 * Like {@link java.util.Hashtable} but unlike {@link HashMap}, this class
 * does <em>not</em> allow <tt>null</tt> to be used as a key or value. Unlike
 * {@link java.util.LinkedHashMap}, this class does <em>not</em> provide
 * predictable iteration order. A snapshot of the keys and entries may be
 * obtained in ascending and descending order of retention.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 *      http://code.google.com/p/concurrentlinkedhashmap/</a>
 */
//@ThreadSafe
public final class PrivateMaxEntriesMap<K, V> extends AbstractMap<K, V>
        implements ConcurrentMap<K, V>, Serializable {

    /*
     * This class performs a best-effort bounding of a ConcurrentHashMap using a
     * page-replacement algorithm to determine which entries to evict when the
     * capacity is exceeded.
     *
     * The page replacement algorithm's data structures are kept eventually
     * consistent with the map. An update to the map and recording of reads may
     * not be immediately reflected on the algorithm's data structures. These
     * structures are guarded by a lock and operations are applied in batches to
     * avoid lock contention. The penalty of applying the batches is spread across
     * threads so that the amortized cost is slightly higher than performing just
     * the ConcurrentHashMap operation.
     *
     * A memento of the reads and writes that were performed on the map are
     * recorded in buffers. These buffers are drained at the first opportunity
     * after a write or when the read buffer exceeds a threshold size. The reads
     * are recorded in a lossy buffer, allowing the reordering operations to be
     * discarded if the draining process cannot keep up. Due to the concurrent
     * nature of the read and write operations a strict policy ordering is not
     * possible, but is observably strict when single threaded.
     *
     * Due to a lack of a strict ordering guarantee, a task can be executed
     * out-of-order, such as a removal followed by its addition. The state of the
     * entry is encoded within the value's weight.
     *
     * Alive: The entry is in both the hash-table and the page replacement policy.
     * This is represented by a positive weight.
     *
     * Retired: The entry is not in the hash-table and is pending removal from the
     * page replacement policy. This is represented by a negative weight.
     *
     * Dead: The entry is not in the hash-table and is not in the page replacement
     * policy. This is represented by a weight of zero.
     *
     * The Least Recently Used page replacement algorithm was chosen due to its
     * simplicity, high hit rate, and ability to be implemented with O(1) time
     * complexity.
     */

    /** The number of CPUs */
    static final int NCPU = Runtime.getRuntime().availableProcessors();

    /** The maximum capacity of the map. */
    static final long MAXIMUM_CAPACITY = Long.MAX_VALUE - Integer.MAX_VALUE;

    /**
     * The number of read buffers to use.
     * The max of 4 was introduced due to https://github.com/FasterXML/jackson-databind/issues/3665.
     */
    static final int NUMBER_OF_READ_BUFFERS = Math.min(4, ceilingNextPowerOfTwo(NCPU));

    /** Mask value for indexing into the read buffers. */
    static final int READ_BUFFERS_MASK = NUMBER_OF_READ_BUFFERS - 1;

    /**
     * The number of pending read operations before attempting to drain.
     * The threshold of 4 was introduced due to https://github.com/FasterXML/jackson-databind/issues/3665.
     */
    static final int READ_BUFFER_THRESHOLD = 4;

    /** The maximum number of read operations to perform per amortized drain. */
    static final int READ_BUFFER_DRAIN_THRESHOLD = 2 * READ_BUFFER_THRESHOLD;

    /** The maximum number of pending reads per buffer. */
    static final int READ_BUFFER_SIZE = 2 * READ_BUFFER_DRAIN_THRESHOLD;

    /** Mask value for indexing into the read buffer. */
    static final int READ_BUFFER_INDEX_MASK = READ_BUFFER_SIZE - 1;

    /** The maximum number of write operations to perform per amortized drain. */
    static final int WRITE_BUFFER_DRAIN_THRESHOLD = 16;

    static int ceilingNextPowerOfTwo(int x) {
        // From Hacker's Delight, Chapter 3, Harry S. Warren Jr.
        return 1 << (Integer.SIZE - Integer.numberOfLeadingZeros(x - 1));
    }

    // The backing data store holding the key-value associations
    final ConcurrentMap<K, Node<K, V>> data;
    final int concurrencyLevel;

    // These fields provide support to bound the map by a maximum capacity
    //@GuardedBy("evictionLock")
    final long[] readBufferReadCount;
    //@GuardedBy("evictionLock")
    final LinkedDeque<Node<K, V>> evictionDeque;

    //@GuardedBy("evictionLock") // must write under lock
    final AtomicLong weightedSize;
    //@GuardedBy("evictionLock") // must write under lock
    final AtomicLong capacity;

    final Lock evictionLock;
    final Queue<Runnable> writeBuffer;
    final AtomicLongArray readBufferWriteCount;
    final AtomicLongArray readBufferDrainAtWriteCount;
    final AtomicReferenceArray<Node<K, V>> readBuffers;

    final AtomicReference<DrainStatus> drainStatus;

    transient Set<K> keySet;
    transient Collection<V> values;
    transient Set<Entry<K, V>> entrySet;

    private static int readBufferIndex(int bufferIndex, int entryIndex) {
        return READ_BUFFER_SIZE * bufferIndex + entryIndex;
    }

    /**
     * Creates an instance based on the builder's configuration.
     */
    @SuppressWarnings({"unchecked", "cast"})
    PrivateMaxEntriesMap(Builder<K, V> builder) {
        // The data store and its maximum capacity
        concurrencyLevel = builder.concurrencyLevel;
        capacity = new AtomicLong(Math.min(builder.capacity, MAXIMUM_CAPACITY));
        data = new ConcurrentHashMap<K, Node<K, V>>(builder.initialCapacity, 0.75f, concurrencyLevel);

        // The eviction support
        evictionLock = new ReentrantLock();
        weightedSize = new AtomicLong();
        evictionDeque = new LinkedDeque<Node<K, V>>();
        writeBuffer = new ConcurrentLinkedQueue<Runnable>();
        drainStatus = new AtomicReference<DrainStatus>(DrainStatus.IDLE);

        readBufferReadCount = new long[NUMBER_OF_READ_BUFFERS];
        readBufferWriteCount = new AtomicLongArray(NUMBER_OF_READ_BUFFERS);
        readBufferDrainAtWriteCount = new AtomicLongArray(NUMBER_OF_READ_BUFFERS);
        readBuffers = new AtomicReferenceArray<>(NUMBER_OF_READ_BUFFERS * READ_BUFFER_SIZE);
    }

    /** Ensures that the object is not null. */
    static void checkNotNull(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    /** Ensures that the argument expression is true. */
    static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /** Ensures that the state expression is true. */
    static void checkState(boolean expression) {
        if (!expression) {
            throw new IllegalStateException();
        }
    }

    /* ---------------- Eviction Support -------------- */

    /**
     * Retrieves the maximum capacity of the map.
     *
     * @return the maximum capacity
     */
    public long capacity() {
        return capacity.get();
    }

    /**
     * Sets the maximum capacity of the map and eagerly evicts entries
     * until it shrinks to the appropriate size.
     *
     * @param capacity the maximum capacity of the map
     * @throws IllegalArgumentException if the capacity is negative
     */
    public void setCapacity(long capacity) {
        checkArgument(capacity >= 0);
        evictionLock.lock();
        try {
            this.capacity.lazySet(Math.min(capacity, MAXIMUM_CAPACITY));
            drainBuffers();
            evict();
        } finally {
            evictionLock.unlock();
        }
    }

    /** Determines whether the map has exceeded its capacity. */
    //@GuardedBy("evictionLock")
    boolean hasOverflowed() {
        return weightedSize.get() > capacity.get();
    }

    /**
     * Evicts entries from the map while it exceeds the capacity and appends
     * evicted entries to the notification queue for processing.
     */
    //@GuardedBy("evictionLock")
    void evict() {
        // Attempts to evict entries from the map if it exceeds the maximum
        // capacity. If the eviction fails due to a concurrent removal of the
        // victim, that removal may cancel out the addition that triggered this
        // eviction. The victim is eagerly unlinked before the removal task so
        // that if an eviction is still required then a new victim will be chosen
        // for removal.
        while (hasOverflowed()) {
            final Node<K, V> node = evictionDeque.poll();

            // If weighted values are used, then the pending operations will adjust
            // the size to reflect the correct weight
            if (node == null) {
                return;
            }

            data.remove(node.key, node);

            makeDead(node);
        }
    }

    /**
     * Performs the post-processing work required after a read.
     *
     * @param node the entry in the page replacement policy
     */
    void afterRead(Node<K, V> node) {
        final int bufferIndex = readBufferIndex();
        final long writeCount = recordRead(bufferIndex, node);
        drainOnReadIfNeeded(bufferIndex, writeCount);
    }

    /** Returns the index to the read buffer to record into. */
    static int readBufferIndex() {
        // A buffer is chosen by the thread's id so that tasks are distributed in a
        // pseudo evenly manner. This helps avoid hot entries causing contention
        // due to other threads trying to append to the same buffer.
        return ((int) Thread.currentThread().getId()) & READ_BUFFERS_MASK;
    }

    /**
     * Records a read in the buffer and return its write count.
     *
     * @param bufferIndex the index to the chosen read buffer
     * @param node the entry in the page replacement policy
     * @return the number of writes on the chosen read buffer
     */
    long recordRead(int bufferIndex, Node<K, V> node) {
        // The location in the buffer is chosen in a racy fashion as the increment
        // is not atomic with the insertion. This means that concurrent reads can
        // overlap and overwrite one another, resulting in a lossy buffer.
        final long writeCount = readBufferWriteCount.get(bufferIndex);
        readBufferWriteCount.lazySet(bufferIndex, writeCount + 1);

        final int index = (int) (writeCount & READ_BUFFER_INDEX_MASK);
        readBuffers.lazySet(readBufferIndex(bufferIndex, index), node);

        return writeCount;
    }

    /**
     * Attempts to drain the buffers if it is determined to be needed when
     * post-processing a read.
     *
     * @param bufferIndex the index to the chosen read buffer
     * @param writeCount the number of writes on the chosen read buffer
     */
    void drainOnReadIfNeeded(int bufferIndex, long writeCount) {
        final long pending = (writeCount - readBufferDrainAtWriteCount.get(bufferIndex));
        final boolean delayable = (pending < READ_BUFFER_THRESHOLD);
        final DrainStatus status = drainStatus.get();
        if (status.shouldDrainBuffers(delayable)) {
            tryToDrainBuffers();
        }
    }

    /**
     * Performs the post-processing work required after a write.
     *
     * @param task the pending operation to be applied
     */
    void afterWrite(Runnable task) {
        writeBuffer.add(task);
        drainStatus.lazySet(DrainStatus.REQUIRED);
        tryToDrainBuffers();
    }

    /**
     * Attempts to acquire the eviction lock and apply the pending operations, up
     * to the amortized threshold, to the page replacement policy.
     */
    void tryToDrainBuffers() {
        if (evictionLock.tryLock()) {
            try {
                drainStatus.lazySet(DrainStatus.PROCESSING);
                drainBuffers();
            } finally {
                drainStatus.compareAndSet(DrainStatus.PROCESSING, DrainStatus.IDLE);
                evictionLock.unlock();
            }
        }
    }

    /** Drains the read and write buffers up to an amortized threshold. */
    //@GuardedBy("evictionLock")
    void drainBuffers() {
        drainReadBuffers();
        drainWriteBuffer();
    }

    /** Drains the read buffers, each up to an amortized threshold. */
    //@GuardedBy("evictionLock")
    void drainReadBuffers() {
        final int start = (int) Thread.currentThread().getId();
        final int end = start + NUMBER_OF_READ_BUFFERS;
        for (int i = start; i < end; i++) {
            drainReadBuffer(i & READ_BUFFERS_MASK);
        }
    }

    /** Drains the read buffer up to an amortized threshold. */
    //@GuardedBy("evictionLock")
    void drainReadBuffer(int bufferIndex) {
        final long writeCount = readBufferWriteCount.get(bufferIndex);
        for (int i = 0; i < READ_BUFFER_DRAIN_THRESHOLD; i++) {
            final int index = (int) (readBufferReadCount[bufferIndex] & READ_BUFFER_INDEX_MASK);
            final int arrayIndex = readBufferIndex(bufferIndex, index);
            final Node<K, V> node = readBuffers.get(arrayIndex);
            if (node == null) {
                break;
            }

            readBuffers.lazySet(arrayIndex, null);
            applyRead(node);
            readBufferReadCount[bufferIndex]++;
        }
        readBufferDrainAtWriteCount.lazySet(bufferIndex, writeCount);
    }

    /** Updates the node's location in the page replacement policy. */
    //@GuardedBy("evictionLock")
    void applyRead(Node<K, V> node) {
        // An entry may be scheduled for reordering despite having been removed.
        // This can occur when the entry was concurrently read while a writer was
        // removing it. If the entry is no longer linked then it does not need to
        // be processed.
        if (evictionDeque.contains(node)) {
            evictionDeque.moveToBack(node);
        }
    }

    /** Drains the read buffer up to an amortized threshold. */
    //@GuardedBy("evictionLock")
    void drainWriteBuffer() {
        for (int i = 0; i < WRITE_BUFFER_DRAIN_THRESHOLD; i++) {
            final Runnable task = writeBuffer.poll();
            if (task == null) {
                break;
            }
            task.run();
        }
    }

    /**
     * Attempts to transition the node from the <tt>alive</tt> state to the
     * <tt>retired</tt> state.
     *
     * @param node the entry in the page replacement policy
     * @param expect the expected weighted value
     * @return if successful
     */
    boolean tryToRetire(Node<K, V> node, WeightedValue<V> expect) {
        if (expect.isAlive()) {
            final WeightedValue<V> retired = new WeightedValue<V>(expect.value, -expect.weight);
            return node.compareAndSet(expect, retired);
        }
        return false;
    }

    /**
     * Atomically transitions the node from the <tt>alive</tt> state to the
     * <tt>retired</tt> state, if a valid transition.
     *
     * @param node the entry in the page replacement policy
     */
    void makeRetired(Node<K, V> node) {
        for (;;) {
            final WeightedValue<V> current = node.get();
            if (!current.isAlive()) {
                return;
            }
            final WeightedValue<V> retired = new WeightedValue<V>(current.value, -current.weight);
            if (node.compareAndSet(current, retired)) {
                return;
            }
        }
    }

    /**
     * Atomically transitions the node to the <tt>dead</tt> state and decrements
     * the <tt>weightedSize</tt>.
     *
     * @param node the entry in the page replacement policy
     */
    //@GuardedBy("evictionLock")
    void makeDead(Node<K, V> node) {
        for (;;) {
            WeightedValue<V> current = node.get();
            WeightedValue<V> dead = new WeightedValue<V>(current.value, 0);
            if (node.compareAndSet(current, dead)) {
                weightedSize.lazySet(weightedSize.get() - Math.abs(current.weight));
                return;
            }
        }
    }

    /** Adds the node to the page replacement policy. */
    final class AddTask implements Runnable {
        final Node<K, V> node;
        final int weight;

        AddTask(Node<K, V> node, int weight) {
            this.weight = weight;
            this.node = node;
        }

        @Override
        //@GuardedBy("evictionLock")
        public void run() {
            weightedSize.lazySet(weightedSize.get() + weight);

            // ignore out-of-order write operations
            if (node.get().isAlive()) {
                evictionDeque.add(node);
                evict();
            }
        }
    }

    /** Removes a node from the page replacement policy. */
    final class RemovalTask implements Runnable {
        final Node<K, V> node;

        RemovalTask(Node<K, V> node) {
            this.node = node;
        }

        @Override
        //@GuardedBy("evictionLock")
        public void run() {
            // add may not have been processed yet
            evictionDeque.remove(node);
            makeDead(node);
        }
    }

    /** Updates the weighted size and evicts an entry on overflow. */
    final class UpdateTask implements Runnable {
        final int weightDifference;
        final Node<K, V> node;

        UpdateTask(Node<K, V> node, int weightDifference) {
            this.weightDifference = weightDifference;
            this.node = node;
        }

        @Override
        //@GuardedBy("evictionLock")
        public void run() {
            weightedSize.lazySet(weightedSize.get() + weightDifference);
            applyRead(node);
            evict();
        }
    }

    /* ---------------- Concurrent Map Support -------------- */

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void clear() {
        evictionLock.lock();
        try {
            // Discard all entries
            Node<K, V> node;
            while ((node = evictionDeque.poll()) != null) {
                data.remove(node.key, node);
                makeDead(node);
            }

            // Discard all pending reads
            for (int i = 0; i < readBuffers.length(); i++) {
                readBuffers.lazySet(i, null);
            }

            // Apply all pending writes
            Runnable task;
            while ((task = writeBuffer.poll()) != null) {
                task.run();
            }
        } finally {
            evictionLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        checkNotNull(value);

        for (Node<K, V> node : data.values()) {
            if (node.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        final Node<K, V> node = data.get(key);
        if (node == null) {
            return null;
        }
        afterRead(node);
        return node.getValue();
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, false);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return put(key, value, true);
    }

    /**
     * Adds a node to the list and the data store. If an existing node is found,
     * then its value is updated if allowed.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param onlyIfAbsent a write is performed only if the key is not already
     *     associated with a value
     * @return the prior value in the data store or null if no mapping was found
     */
    V put(K key, V value, boolean onlyIfAbsent) {
        checkNotNull(key);
        checkNotNull(value);

        final int weight = 1;
        final WeightedValue<V> weightedValue = new WeightedValue<V>(value, weight);
        final Node<K, V> node = new Node<K, V>(key, weightedValue);

        for (;;) {
            final Node<K, V> prior = data.putIfAbsent(node.key, node);
            if (prior == null) {
                afterWrite(new AddTask(node, weight));
                return null;
            } else if (onlyIfAbsent) {
                afterRead(prior);
                return prior.getValue();
            }
            for (;;) {
                final WeightedValue<V> oldWeightedValue = prior.get();
                if (!oldWeightedValue.isAlive()) {
                    break;
                }

                if (prior.compareAndSet(oldWeightedValue, weightedValue)) {
                    final int weightedDifference = weight - oldWeightedValue.weight;
                    if (weightedDifference == 0) {
                        afterRead(prior);
                    } else {
                        afterWrite(new UpdateTask(prior, weightedDifference));
                    }
                    return oldWeightedValue.value;
                }
            }
        }
    }

    @Override
    public V remove(Object key) {
        final Node<K, V> node = data.remove(key);
        if (node == null) {
            return null;
        }

        makeRetired(node);
        afterWrite(new RemovalTask(node));
        return node.getValue();
    }

    @Override
    public boolean remove(Object key, Object value) {
        final Node<K, V> node = data.get(key);
        if ((node == null) || (value == null)) {
            return false;
        }

        WeightedValue<V> weightedValue = node.get();
        for (;;) {
            if (weightedValue.contains(value)) {
                if (tryToRetire(node, weightedValue)) {
                    if (data.remove(key, node)) {
                        afterWrite(new RemovalTask(node));
                        return true;
                    }
                } else {
                    weightedValue = node.get();
                    if (weightedValue.isAlive()) {
                        // retry as an intermediate update may have replaced the value with
                        // an equal instance that has a different reference identity
                        continue;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public V replace(K key, V value) {
        checkNotNull(key);
        checkNotNull(value);

        final int weight = 1;
        final WeightedValue<V> weightedValue = new WeightedValue<V>(value, weight);

        final Node<K, V> node = data.get(key);
        if (node == null) {
            return null;
        }
        for (;;) {
            final WeightedValue<V> oldWeightedValue = node.get();
            if (!oldWeightedValue.isAlive()) {
                return null;
            }
            if (node.compareAndSet(oldWeightedValue, weightedValue)) {
                final int weightedDifference = weight - oldWeightedValue.weight;
                if (weightedDifference == 0) {
                    afterRead(node);
                } else {
                    afterWrite(new UpdateTask(node, weightedDifference));
                }
                return oldWeightedValue.value;
            }
        }
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        checkNotNull(key);
        checkNotNull(oldValue);
        checkNotNull(newValue);

        final int weight = 1;
        final WeightedValue<V> newWeightedValue = new WeightedValue<V>(newValue, weight);

        final Node<K, V> node = data.get(key);
        if (node == null) {
            return false;
        }
        for (;;) {
            final WeightedValue<V> weightedValue = node.get();
            if (!weightedValue.isAlive() || !weightedValue.contains(oldValue)) {
                return false;
            }
            if (node.compareAndSet(weightedValue, newWeightedValue)) {
                final int weightedDifference = weight - weightedValue.weight;
                if (weightedDifference == 0) {
                    afterRead(node);
                } else {
                    afterWrite(new UpdateTask(node, weightedDifference));
                }
                return true;
            }
        }
    }

    @Override
    public Set<K> keySet() {
        final Set<K> ks = keySet;
        return (ks == null) ? (keySet = new KeySet()) : ks;
    }

    @Override
    public Collection<V> values() {
        final Collection<V> vs = values;
        return (vs == null) ? (values = new Values()) : vs;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> es = entrySet;
        return (es == null) ? (entrySet = new EntrySet()) : es;
    }

    /** The draining status of the buffers. */
    enum DrainStatus {

        /** A drain is not taking place. */
        IDLE {
            @Override boolean shouldDrainBuffers(boolean delayable) {
                return !delayable;
            }
        },

        /** A drain is required due to a pending write modification. */
        REQUIRED {
            @Override boolean shouldDrainBuffers(boolean delayable) {
                return true;
            }
        },

        /** A drain is in progress. */
        PROCESSING {
            @Override boolean shouldDrainBuffers(boolean delayable) {
                return false;
            }
        };

        /**
         * Determines whether the buffers should be drained.
         *
         * @param delayable if a drain should be delayed until required
         * @return if a drain should be attempted
         */
        abstract boolean shouldDrainBuffers(boolean delayable);
    }

    /** A value, its weight, and the entry's status. */
    //@Immutable
    static final class WeightedValue<V> {
        final int weight;
        final V value;

        WeightedValue(V value, int weight) {
            this.weight = weight;
            this.value = value;
        }

        boolean contains(Object o) {
            return (o == value) || value.equals(o);
        }

        /**
         * If the entry is available in the hash-table and page replacement policy.
         */
        boolean isAlive() {
            return weight > 0;
        }
    }

    /**
     * A node contains the key, the weighted value, and the linkage pointers on
     * the page-replacement algorithm's data structures.
     */
    @SuppressWarnings("serial")
    static final class Node<K, V> extends AtomicReference<WeightedValue<V>>
            implements Linked<Node<K, V>> {
        final K key;
        //@GuardedBy("evictionLock")
        Node<K, V> prev;
        //@GuardedBy("evictionLock")
        Node<K, V> next;

        /** Creates a new, unlinked node. */
        Node(K key, WeightedValue<V> weightedValue) {
            super(weightedValue);
            this.key = key;
        }

        @Override
        //@GuardedBy("evictionLock")
        public Node<K, V> getPrevious() {
            return prev;
        }

        @Override
        //@GuardedBy("evictionLock")
        public void setPrevious(Node<K, V> prev) {
            this.prev = prev;
        }

        @Override
        //@GuardedBy("evictionLock")
        public Node<K, V> getNext() {
            return next;
        }

        @Override
        //@GuardedBy("evictionLock")
        public void setNext(Node<K, V> next) {
            this.next = next;
        }

        /** Retrieves the value held by the current <tt>WeightedValue</tt>. */
        V getValue() {
            return get().value;
        }
    }

    /** An adapter to safely externalize the keys. */
    final class KeySet extends AbstractSet<K> {
        final PrivateMaxEntriesMap<K, V> map = PrivateMaxEntriesMap.this;

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public boolean contains(Object obj) {
            return containsKey(obj);
        }

        @Override
        public boolean remove(Object obj) {
            return (map.remove(obj) != null);
        }

        @Override
        public Object[] toArray() {
            return map.data.keySet().toArray();
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return map.data.keySet().toArray(array);
        }
    }

    /** An adapter to safely externalize the key iterator. */
    final class KeyIterator implements Iterator<K> {
        final Iterator<K> iterator = data.keySet().iterator();
        K current;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public K next() {
            current = iterator.next();
            return current;
        }

        @Override
        public void remove() {
            checkState(current != null);
            PrivateMaxEntriesMap.this.remove(current);
            current = null;
        }
    }

    /** An adapter to safely externalize the values. */
    final class Values extends AbstractCollection<V> {

        @Override
        public int size() {
            return PrivateMaxEntriesMap.this.size();
        }

        @Override
        public void clear() {
            PrivateMaxEntriesMap.this.clear();
        }

        @Override
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }
    }

    /** An adapter to safely externalize the value iterator. */
    final class ValueIterator implements Iterator<V> {
        final Iterator<Node<K, V>> iterator = data.values().iterator();
        Node<K, V> current;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public V next() {
            current = iterator.next();
            return current.getValue();
        }

        @Override
        public void remove() {
            checkState(current != null);
            PrivateMaxEntriesMap.this.remove(current.key);
            current = null;
        }
    }

    /** An adapter to safely externalize the entries. */
    final class EntrySet extends AbstractSet<Entry<K, V>> {
        final PrivateMaxEntriesMap<K, V> map = PrivateMaxEntriesMap.this;

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public boolean contains(Object obj) {
            if (!(obj instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) obj;
            Node<K, V> node = map.data.get(entry.getKey());
            return (node != null) && (node.getValue().equals(entry.getValue()));
        }

        @Override
        public boolean add(Entry<K, V> entry) {
            throw new UnsupportedOperationException("ConcurrentLinkedHashMap does not allow add to be called on entrySet()");
        }

        @Override
        public boolean remove(Object obj) {
            if (!(obj instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) obj;
            return map.remove(entry.getKey(), entry.getValue());
        }
    }

    /** An adapter to safely externalize the entry iterator. */
    final class EntryIterator implements Iterator<Entry<K, V>> {
        final Iterator<Node<K, V>> iterator = data.values().iterator();
        Node<K, V> current;

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            current = iterator.next();
            return new WriteThroughEntry(current);
        }

        @Override
        public void remove() {
            checkState(current != null);
            PrivateMaxEntriesMap.this.remove(current.key);
            current = null;
        }
    }

    /** An entry that allows updates to write through to the map. */
    final class WriteThroughEntry extends SimpleEntry<K, V> {
        static final long serialVersionUID = 1;

        WriteThroughEntry(Node<K, V> node) {
            super(node.key, node.getValue());
        }

        @Override
        public V setValue(V value) {
            put(getKey(), value);
            return super.setValue(value);
        }

        Object writeReplace() {
            return new SimpleEntry<K, V>(this);
        }
    }

    /* ---------------- Serialization Support -------------- */

    static final long serialVersionUID = 1;

    Object writeReplace() {
        return new SerializationProxy<K, V>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    /**
     * A proxy that is serialized instead of the map. The page-replacement
     * algorithm's data structures are not serialized so the deserialized
     * instance contains only the entries. This is acceptable as caches hold
     * transient data that is recomputable and serialization would tend to be
     * used as a fast warm-up process.
     */
    static final class SerializationProxy<K, V> implements Serializable {
        final int concurrencyLevel;
        final Map<K, V> data;
        final long capacity;

        SerializationProxy(PrivateMaxEntriesMap<K, V> map) {
            concurrencyLevel = map.concurrencyLevel;
            data = new HashMap<K, V>(map);
            capacity = map.capacity.get();
        }

        Object readResolve() {
            PrivateMaxEntriesMap<K, V> map = new Builder<K, V>()
                    .maximumCapacity(capacity)
                    .build();
            map.putAll(data);
            return map;
        }

        static final long serialVersionUID = 1;
    }

    /* ---------------- Builder -------------- */

    /**
     * A builder that creates {@link PrivateMaxEntriesMap} instances. It
     * provides a flexible approach for constructing customized instances with
     * a named parameter syntax. It can be used in the following manner:
     * <pre>{@code
     * ConcurrentMap<Vertex, Set<Edge>> graph = new Builder<Vertex, Set<Edge>>()
     *     .maximumCapacity(5000)
     *     .build();
     * }</pre>
     */
    public static final class Builder<K, V> {
        static final int DEFAULT_CONCURRENCY_LEVEL = 16;
        static final int DEFAULT_INITIAL_CAPACITY = 16;

        int concurrencyLevel;
        int initialCapacity;
        long capacity;

        public Builder() {
            capacity = -1;
            initialCapacity = DEFAULT_INITIAL_CAPACITY;
            concurrencyLevel = DEFAULT_CONCURRENCY_LEVEL;
        }

        /**
         * Specifies the initial capacity of the hash table (default <tt>16</tt>).
         * This is the number of key-value pairs that the hash table can hold
         * before a resize operation is required.
         *
         * @param initialCapacity the initial capacity used to size the hash table
         *     to accommodate this many entries.
         * @throws IllegalArgumentException if the initialCapacity is negative
         */
        public Builder<K, V> initialCapacity(int initialCapacity) {
            checkArgument(initialCapacity >= 0);
            this.initialCapacity = initialCapacity;
            return this;
        }

        /**
         * Specifies the maximum capacity to coerce the map to and may
         * exceed it temporarily.
         *
         * @param capacity the threshold to bound the map by
         * @throws IllegalArgumentException if the maximumCapacity is negative
         */
        public Builder<K, V> maximumCapacity(long capacity) {
            checkArgument(capacity >= 0);
            this.capacity = capacity;
            return this;
        }

        /**
         * Specifies the estimated number of concurrently updating threads. The
         * implementation performs internal sizing to try to accommodate this many
         * threads (default <tt>16</tt>).
         *
         * @param concurrencyLevel the estimated number of concurrently updating
         *     threads
         * @throws IllegalArgumentException if the concurrencyLevel is less than or
         *     equal to zero
         */
        public Builder<K, V> concurrencyLevel(int concurrencyLevel) {
            checkArgument(concurrencyLevel > 0);
            this.concurrencyLevel = concurrencyLevel;
            return this;
        }

        /**
         * Creates a new {@link PrivateMaxEntriesMap} instance.
         *
         * @throws IllegalStateException if the maximum capacity was
         *     not set
         */
        public PrivateMaxEntriesMap<K, V> build() {
            checkState(capacity >= 0);
            return new PrivateMaxEntriesMap<K, V>(this);
        }
    }
}
