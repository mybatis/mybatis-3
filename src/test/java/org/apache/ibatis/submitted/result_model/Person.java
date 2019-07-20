/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.submitted.result_model;

import org.apache.ibatis.annotations.ResultColumn;
import org.apache.ibatis.annotations.ResultModel;
import java.util.ArrayList;
import java.util.List;

public class Person {
  @ResultColumn
  private Integer id;
  @ResultColumn
  private String name;
  @ResultModel(id = "person_to_item", type = Item.class, columnPrefix = "item_")
  @ResultColumn
  private List<Item> items = new ArrayList<>();

  public String toString() {
    return new StringBuilder()
      .append("Person(")
      .append(id)
      .append(", ")
      .append(name)
      .append(", ")
      .append(items)
      .append(" )")
      .toString();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Item> getItems() {
    return items;
  }

  public boolean owns(String name) {
    for (Item item : getItems()) {
      if (item.getName().equals(name))
        return true;
    }
    return false;
  }
}
