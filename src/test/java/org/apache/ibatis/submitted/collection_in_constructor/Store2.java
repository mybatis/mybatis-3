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

public class Store2 {

  private final Integer id;
  private final List<Clerk> clerks;
  private final List<Isle> isles;

  public Store2(Integer id, List<Clerk> clerks, List<Isle> isles) {
    super();
    this.id = id;
    this.clerks = clerks;
    this.isles = isles;
  }

  public Integer getId() {
    return id;
  }

  public List<Clerk> getClerks() {
    return clerks;
  }

  public List<Isle> getIsles() {
    return isles;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clerks, id, isles);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Store2)) {
      return false;
    }
    Store2 other = (Store2) obj;
    return Objects.equals(clerks, other.clerks) && Objects.equals(id, other.id) && Objects.equals(isles, other.isles);
  }

  @Override
  public String toString() {
    return "Store2 [id=" + id + ", clerks=" + clerks + ", isles=" + isles + "]";
  }
}
