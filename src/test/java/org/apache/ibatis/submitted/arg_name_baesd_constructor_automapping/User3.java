/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.arg_name_baesd_constructor_automapping;

import org.apache.ibatis.annotations.AutomapConstructor;

public class User3 {

  private Integer id;
  private String name;
  private Long team;

  @AutomapConstructor
  public User3(Integer id, String name, Long team) {
    super();
    this.id = id;
    this.name = name;
    this.team = team;
  }

  @AutomapConstructor
  public User3(Integer id, String name) {
    super();
    this.id = id;
    this.name = name;
    this.team = 100L;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setTeam(Long team) {
    this.team = team;
  }

  public Long getTeam() {
    return team;
  }
}
