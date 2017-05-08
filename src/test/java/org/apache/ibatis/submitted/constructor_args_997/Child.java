/*
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.submitted.constructor_args_997;

public class Child {
  private final String id;
  private final String name;
  private final Parent parent;

  public Child(String id, String name, Parent parent) {
    System.out.println("CHILD: id = [" + id + "], name = [" + name + "], parent = [" + parent + "]");
    this.id = id;
    this.name = name;
    this.parent = parent;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Parent getParent() {
    return parent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Child child = (Child) o;

    if (id != null ? !id.equals(child.id) : child.id != null) return false;
    if (name != null ? !name.equals(child.name) : child.name != null) return false;
    return parent != null ? parent.equals(child.parent) : child.parent == null;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (parent != null ? parent.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Child{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", parent=" + parent +
      '}';
  }
}
