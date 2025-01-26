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

public class Store10 {

  private final Integer id;
  private final String name;
  private final List<Clerk> clerks;
  private final List<String> strings;

  public Store10(Integer id, String name, List<Clerk> clerks, List<String> strings) {
    this.id = id;
    this.name = name;
    this.clerks = clerks;
    this.strings = strings;
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

  public List<String> getStrings() {
    return strings;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;
    Store10 store10 = (Store10) o;
    return Objects.equals(id, store10.id) && Objects.equals(name, store10.name)
        && Objects.equals(clerks, store10.clerks) && Objects.equals(strings, store10.strings);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, clerks, strings);
  }

  @Override
  public String toString() {
    return "Store10{" + "id=" + id + ", name='" + name + '\'' + ", clerks=" + clerks + ", strings=" + strings + '}';
  }
}
