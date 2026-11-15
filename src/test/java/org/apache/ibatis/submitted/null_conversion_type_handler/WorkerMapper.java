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
package org.apache.ibatis.submitted.null_conversion_type_handler;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

public interface WorkerMapper {

  @Insert("""
      insert into worker (id, name, blood_type)
      values (#{id}, #{name}, #{bloodType})
      """)
  void insert(Worker user);

  @Select("select blood_type from worker where id = #{id}")
  Integer selectBloodTypeValue(Integer id);

  @Select("select * from worker where id = #{id}")
  Worker select(Integer id);

}
