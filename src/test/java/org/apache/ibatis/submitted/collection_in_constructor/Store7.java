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

public class Store7 {

  private final Integer id;
  private final List<String> aisleNames;
  private final List<String> clerkNames;

  public Store7(Integer id, List<String> aisleNames, List<String> clerkNames) {
    super();
    this.id = id;
    this.aisleNames = aisleNames;
    this.clerkNames = clerkNames;
  }

  public Integer getId() {
    return id;
  }

  public List<String> getAisleNames() {
    return aisleNames;
  }

  public List<String> getClerkNames() {
    return clerkNames;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clerkNames, id, aisleNames);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Store7)) {
      return false;
    }
    Store7 other = (Store7) obj;
    return Objects.equals(clerkNames, other.clerkNames) && Objects.equals(id, other.id)
        && Objects.equals(aisleNames, other.aisleNames);
  }

  @Override
  public String toString() {
    return "Store7 [id=" + id + ", aisleNames=" + aisleNames + ", clerkNames=" + clerkNames + "]";
  }
}
