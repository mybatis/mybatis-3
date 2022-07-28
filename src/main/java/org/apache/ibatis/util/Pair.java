/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.util;

import java.util.Objects;

/**
 * describe: A pair of key and value
 *
 * @author kfyty725
 * @date 2022/7/28 15:08
 * @email kfyty725@hotmail.com
 */
public class Pair<K, V> {
  /**
   * key
   */
  private final K key;

  /**
   * value
   */
  private final V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  public int getKeyHashCode() {
    return this.getKey() == null ? 0 : this.getKey().hashCode();
  }

  public int getValueHashCode() {
    return this.getValue() == null ? 0 : this.getValue().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Pair)) {
      return false;
    }
    Pair<?, ?> other = (Pair<?, ?>) obj;
    return Objects.equals(this.getKey(), other.getKey()) && Objects.equals(this.getValue(), other.getValue());
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = result * 59 + (this.getKey() == null ? 43 : this.getKey().hashCode());
    result = result * 59 + (this.getValue() == null ? 43 : this.getValue().hashCode());
    return result;
  }

  @Override
  public String toString() {
    return this.getKey() + "=" + this.getValue();
  }
}
