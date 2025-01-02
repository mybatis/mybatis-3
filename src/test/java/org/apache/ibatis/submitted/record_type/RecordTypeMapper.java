/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.submitted.record_type;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface RecordTypeMapper {

  @Select("select id, val, url from prop where id = #{id}")
  Property selectPropertyAutomapping(int id);

  @Results(id = "propertyRM")
  @Arg(column = "id", javaType = int.class)
  @Arg(column = "val", javaType = String.class)
  @Arg(column = "url", javaType = String.class)
  @Select("select val, id, url from prop where id = #{id}")
  Property selectProperty(int id);

  @Insert("insert into prop (id, val, url) values (#{id}, #{value}, #{URL})")
  int insertProperty(Property property);

  @Arg(id = true, column = "id", javaType = Integer.class)
  @Arg(javaType = Property.class, resultMap = "propertyRM", columnPrefix = "p_")
  // @formatter:off
  @Select({
      "select i.id, p.id p_id, p.val p_val, p.url p_url",
        "from item i left join prop p on p.id = i.prop_id",
       "where i.id = #{id}"})
  // @formatter:on
  Item selectItem(Integer id);

}
