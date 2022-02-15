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
package org.apache.ibatis.submitted.skip_on_empty;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SkipOnEmpty;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public interface Mapper {

  List<User> getUsersListFromList(@SkipOnEmpty List<Integer> ids);

  ArrayList<User> getUsersArrayListFromArray(@SkipOnEmpty @Param("ids") Integer[] ids);

  LinkedList<User> getUsersLinkedListFromRequest(@SkipOnEmpty("idSet") Request request);

  User[] getUsersArrayFromNamedRequest(@SkipOnEmpty("idSet") @Param("request") Request request);

  User getFirstUserFromList(@SkipOnEmpty("ref.idSet") Request.RequestHolder holder);

  @MapKey("code")
  Map<String, User> getCodeMapFromIdsAndCodes(@SkipOnEmpty @Param("ids") List<Integer> ids,
                                              @SkipOnEmpty @Param("codes") List<String> codes );

  Cursor<User> getUserCursorFromList(@SkipOnEmpty List<Integer> ids);

  int batchInsert(@SkipOnEmpty List<User> users);

  int batchUpdate(@SkipOnEmpty @Param("ids") List<Integer> ids, @Param("newName") String newName);

  int batchDelete(@SkipOnEmpty List<Integer> ids);

  void getUsersListFromListWithHandler(@SkipOnEmpty @Param("ids") List<Integer> ids,
                                       ResultHandler<User> resultHandler);

}
