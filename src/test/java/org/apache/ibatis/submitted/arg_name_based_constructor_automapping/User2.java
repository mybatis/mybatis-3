/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.submitted.arg_name_based_constructor_automapping;

import org.apache.ibatis.annotations.Param;

public class User2 {

  private Integer userId;
  private String name;

  public User2(Integer userId, @Param("userName") String name) {
    super();
    this.userId = userId;
    this.name = name;
  }

  public Integer getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }
}
