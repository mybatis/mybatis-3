/*
 *    Copyright 2009-2026 the original author or authors.
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
package org.apache.ibatis.submitted.nested_select;

import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface Mapper {

  @Select("select id, name, fkey from person where id = #{id}")
  @Results({ @Result(property = "id", column = "id", id = true), @Result(property = "name", column = "name"),
      @Result(property = "pets", column = "fkey", one = @One(select = "selectPets")) })
  Person selectPersonWithPets(Integer id);

  @Select("select id, name from pet where fkey = #{fkey}::uuid")
  List<Pet> selectPets(UUID fkey);

}
