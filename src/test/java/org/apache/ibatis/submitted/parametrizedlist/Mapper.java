/*
 *    Copyright 2009-2021 the original author or authors.
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
package org.apache.ibatis.submitted.parametrizedlist;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

public interface Mapper {

  @Select("select id, name from users")
  List<User<String>> getAListOfUsers();

  @Select("select id, name from users")
  @MapKey("id")
  Map<Integer, User<String>> getAMapOfUsers();

  @Select("select id, name from users where id=1")
  Map<String, Object> getUserAsAMap();

  @Select("select id, name from users")
  List<Map<String, Object>> getAListOfMaps();

}
