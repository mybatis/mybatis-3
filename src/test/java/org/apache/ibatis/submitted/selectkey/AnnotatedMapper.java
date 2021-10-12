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
package org.apache.ibatis.submitted.selectkey;

import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

public interface AnnotatedMapper {

  @Insert("insert into table2 (name) values(#{name})")
  @SelectKey(statement = "call identity()", keyProperty = "nameId", before = false, resultType = int.class)
  int insertTable2(Name name);

  @Insert("insert into table2 (name) values(#{name})")
  @Options(useGeneratedKeys = true, keyProperty = "nameId,generatedName", keyColumn = "ID,NAME_FRED")
  int insertTable2WithGeneratedKey(Name name);

  int insertTable2WithGeneratedKeyXml(Name name);

  @Insert("insert into table2 (name) values(#{name})")
  @SelectKey(statement = "select id, name_fred from table2 where id = identity()", keyProperty = "nameId,generatedName", keyColumn = "ID,NAME_FRED", before = false, resultType = Map.class)
  int insertTable2WithSelectKeyWithKeyMap(Name name);

  int insertTable2WithSelectKeyWithKeyMapXml(Name name);

  @Insert("insert into table2 (name) values(#{name})")
  @SelectKey(statement = "select id as nameId, name_fred as generatedName from table2 where id = identity()", keyProperty = "nameId,generatedName", before = false, resultType = Name.class)
  int insertTable2WithSelectKeyWithKeyObject(Name name);

  int insertTable2WithSelectKeyWithKeyObjectXml(Name name);

  @Insert("insert into table3 (id, name) values(#{nameId}, #{name})")
  @SelectKey(statement = "call next value for TestSequence", keyProperty = "nameId", before = true, resultType = int.class)
  int insertTable3(Name name);

  @InsertProvider(type = SqlProvider.class, method = "insertTable3_2")
  @SelectKey(statement = "call next value for TestSequence", keyProperty = "nameId", before = true, resultType = int.class)
  int insertTable3_2(Name name);

  @Update("update table2 set name = #{name} where id = #{nameId}")
  @Options(useGeneratedKeys = true, keyProperty = "generatedName")
  int updateTable2WithGeneratedKey(Name name);

  int updateTable2WithGeneratedKeyXml(Name name);

  @Update("update table2 set name = #{name} where id = #{nameId}")
  @SelectKey(statement = "select name_fred from table2 where id = #{nameId}", keyProperty = "generatedName", keyColumn = "NAME_FRED", before = false, resultType = String.class)
  int updateTable2WithSelectKeyWithKeyMap(Name name);

  int updateTable2WithSelectKeyWithKeyMapXml(Name name);

  @Update("update table2 set name = #{name} where id = #{nameId}")
  @SelectKey(statement = "select name_fred as generatedName from table2 where id = #{nameId}", keyProperty = "generatedName", before = false, resultType = Name.class)
  int updateTable2WithSelectKeyWithKeyObject(Name name);

  int updateTable2WithSelectKeyWithKeyObjectXml(Name name);
}
