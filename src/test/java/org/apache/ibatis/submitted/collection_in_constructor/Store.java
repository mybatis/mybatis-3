/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.submitted.collection_in_constructor;

import java.util.List;
import java.util.Objects;

public class Store {

  private final Integer id;
  private final String name;
  private final List<Aisle> aisles;

  public Store(Integer id, String name, List<Aisle> aisles) {
    super();
    this.id = id;
    this.name = name;
    this.aisles = aisles;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<Aisle> getAisles() {
    return aisles;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, aisles, name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Store)) {
      return false;
    }
    Store other = (Store) obj;
    return Objects.equals(id, other.id) && Objects.equals(aisles, other.aisles) && Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return "Store [id=" + id + ", name=" + name + ", aisles=" + aisles + "]";
  }
}
