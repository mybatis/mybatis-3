/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

/**
 * This package contains an implementation of a bounded
 * {@link java.util.concurrent.ConcurrentMap} data structure.
 * <p>
 * This package is intended only for use internally by Jackson libraries and has
 * missing features compared to the full <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 * http://code.google.com/p/concurrentlinkedhashmap/</a> implementation.
 * <p>
 * The {@link org.apache.ibatis.reflection.type.jackson.databind.util.internal.PrivateMaxEntriesMap}
 * class supplies an efficient, scalable, thread-safe, bounded map. As with the
 * <tt>Java Collections Framework</tt> the "Concurrent" prefix is used to
 * indicate that the map is not governed by a single exclusion lock.
 *
 * @see <a href="http://code.google.com/p/concurrentlinkedhashmap/">
 *      http://code.google.com/p/concurrentlinkedhashmap/</a>
 */
package org.apache.ibatis.reflection.type.jackson.databind.util.internal;
