/*
 *    Copyright 2009-2016 the original author or authors.
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
package org.apache.ibatis.submitted.empty_row;

import java.util.Map;

import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

public interface Mapper {

  @Select("select null from (values(0))")
  String getString();

  @ResultMap("parentRM")
  @Select("select col1, col2 from parent where id = #{id}")
  Parent getBean(Integer id);

  @Select("select col1, col2 from parent where id = #{id}")
  Map<String, String> getMap(Integer id);

  @ResultMap("associationRM")
  @Select({ "select p.id, c.name child_name from parent p",
      "left join child c on c.parent_id = p.id where p.id = #{id}" })
  Parent getAssociation(Integer id);

  @ResultMap("associationWithNotNullColumnRM")
  @Select({ "select p.id, c.id child_id, c.name child_name from parent p",
      "left join child c on c.parent_id = p.id where p.id = #{id}" })
  Parent getAssociationWithNotNullColumn(Integer id);

  @ResultMap("nestedAssociationRM")
  @Select("select 1 id, null child_name, null grandchild_name from (values(0))")
  Parent getNestedAssociation();

  @ResultMap("collectionRM")
  @Select({ "select p.id, c.name child_name from parent p",
      "left join child c on c.parent_id = p.id where p.id = #{id}" })
  Parent getCollection(Integer id);

  @ResultMap("twoCollectionsRM")
  @Select({ "select p.id, c.name child_name, e.name pet_name from parent p",
      "left join child c on c.parent_id = p.id",
      "left join pet e on e.parent_id = p.id", "where p.id = #{id}" })
  Parent getTwoCollections(Integer id);
}
