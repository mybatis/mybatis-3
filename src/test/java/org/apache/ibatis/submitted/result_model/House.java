/**
 * Copyright 2009-2015 the original author or authors.
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

public class House {
  @ResultColumn
  private Integer id;
  @ResultColumn
  private String name;
  @ResultColumn
  private Integer owner;
  @ResultModel(type = Person.class, columnPrefix = "person_")
  @ResultColumn
  private Person person;

  public String toString() {
    return new StringBuilder()
      .append("Item(")
      .append(id)
      .append(", ")
      .append(name)
      .append(", ")
      .append(owner)
      .append(", ")
      .append(person)
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

  public Integer getOwnerId() {
    return owner;
  }

  public void setOwnerId(Integer ownerId) {
    this.owner = ownerId;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }
}
