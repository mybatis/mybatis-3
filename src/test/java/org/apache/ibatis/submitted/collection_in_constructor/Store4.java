/*
 *    Copyright 2009-2024 the original author or authors.
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

public class Store4 {

  private final Integer id;
  private final List<Aisle> aisles;

  // Using different arg order than the <constructor> definition
  // to ensure the builder is used, see CollectionInConstructorObjectFactory.create
  Store4(List<Aisle> aisles, Integer id) {
    super();
    this.aisles = aisles;
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public List<Aisle> getAisles() {
    return aisles;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, aisles);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Store4)) {
      return false;
    }
    Store4 other = (Store4) obj;
    return Objects.equals(id, other.id) && Objects.equals(aisles, other.aisles);
  }

  @Override
  public String toString() {
    return "Store4 [id=" + id + ", aisles=" + aisles + "]";
  }

  public static Store4Builder builder() {
    return new Store4Builder();
  }

  public static class Store4Builder {
    private Integer id;
    private List<Aisle> isles;

    public Store4Builder id(Integer id) {
      this.id = id;
      return this;
    }

    public Store4Builder isles(List<Aisle> isles) {
      this.isles = isles;
      return this;
    }

    public Store4 build() {
      return new Store4(isles, id);
    }
  }
}
