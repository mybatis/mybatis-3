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
package org.apache.ibatis.submitted.foreach;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface Mapper {

  User getUser(User user);

  int countByUserList(List<User> users);

  int countByBestFriend(List<User> users);

  String selectWithNullItemCheck(List<User> users);

  int typoInItemProperty(List<User> users);

  int itemVariableConflict(@Param("id") Integer id, @Param("ids") List<Integer> ids, @Param("ids2") List<Integer> ids2);

  int indexVariableConflict(@Param("idx") Integer id, @Param("idxs") List<Integer> ids, @Param("idxs2") List<Integer> ids2);
}
