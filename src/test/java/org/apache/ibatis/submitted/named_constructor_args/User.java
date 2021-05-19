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
package org.apache.ibatis.submitted.named_constructor_args;

import org.apache.ibatis.annotations.Param;

public class User {

  private Integer id;
  private String name;
  private Long team;

  public User(@Param("id") String id) {
    super();
    this.id = Integer.valueOf(id);
  }

  public User(Integer userId, @Param("name") String userName) {
    super();
    this.id = userId;
    this.name = userName;
  }

  public User(@Param("id") int id, @Param("name") String name, @Param("team") String team) {
    super();
    // NOP constructor to make sure MyBatis performs strict type matching.
  }

  public User(@Param("id") Integer id, @Param("name") String name, @Param("team") String team) {
    super();
    this.id = id;
    this.name = name;
    this.team = Long.valueOf(team);
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getTeam() {
    return team;
  }
}
