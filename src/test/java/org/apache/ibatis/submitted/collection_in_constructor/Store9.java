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

public class Store9 {

  private final Integer id;
  private final String name;
  private final List<Clerk> clerks;

  public Store9(Integer id, String name, List<Clerk> clerks) {
    this.id = id;
    this.name = name;
    this.clerks = clerks;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<Clerk> getClerks() {
    return clerks;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Store9)) {
      return false;
    }
    Store9 other = (Store9) obj;
    return Objects.equals(id, other.id);
  }

  @Override
  public String toString() {
    return "Store9 [id=" + id + ", name=" + name + ", clerks=" + clerks + "]";
  }
}
