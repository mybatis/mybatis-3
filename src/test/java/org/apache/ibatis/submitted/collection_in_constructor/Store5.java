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

public class Store5 {

  private final Integer id;
  private final List<Clerk> clerks;
  private final List<Clerk> managers;

  public Store5(Integer id, List<Clerk> clerks, List<Clerk> managers) {
    super();
    this.id = id;
    this.clerks = clerks;
    this.managers = managers;
  }

  public Integer getId() {
    return id;
  }

  public List<Clerk> getClerks() {
    return clerks;
  }

  public List<Clerk> getManagers() {
    return managers;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clerks, id, managers);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Store5)) {
      return false;
    }
    Store5 other = (Store5) obj;
    return Objects.equals(clerks, other.clerks) && Objects.equals(id, other.id)
        && Objects.equals(managers, other.managers);
  }

  @Override
  public String toString() {
    return "Store5 [id=" + id + ", clerks=" + clerks + ", managers=" + managers + "]";
  }
}
