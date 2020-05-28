/**
 *    Copyright 2009-2019 the original author or authors.
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
package org.apache.ibatis.submitted.complex_column;

import org.apache.ibatis.annotations.*;

public interface PersonMapper {

    Person getWithoutComplex(Long id);
    Person getWithComplex(Long id);
    Person getParentWithComplex(Person person);

    @Select({
      "SELECT id, firstName, lastName, parent_id, parent_firstName, parent_lastName",
      "FROM Person",
      "WHERE id = #{id,jdbcType=INTEGER}"
    })
    @ResultMap("personMapComplex")
    Person getWithComplex2(Long id);

    @Select({
        "SELECT id, firstName, lastName, parent_id, parent_firstName, parent_lastName",
        "FROM Person",
        "WHERE id = #{id,jdbcType=INTEGER}"
      })
    @ResultMap("org.apache.ibatis.submitted.complex_column.PersonMapper.personMapComplex")
    Person getWithComplex3(Long id);


    @Select({
            "SELECT id, firstName, lastName, parent_id, parent_firstName, parent_lastName",
            "FROM Person",
            "WHERE id = #{id,jdbcType=INTEGER}"
    })
    @Results({
            @Result(id=true, column = "id", property = "id"),
            @Result(property = "parent", column="{firstName=parent_firstName,lastName=parent_lastName}", one=@One(select="getParentWithParamAttributes"))

    })
    Person getComplexWithParamAttributes(Long id);

    @Select("SELECT id, firstName, lastName, parent_id, parent_firstName, parent_lastName" +
            " FROM Person" +
            " WHERE firstName = #{firstName,jdbcType=VARCHAR}" +
            " AND lastName = #{lastName,jdbcType=VARCHAR}" +
            " LIMIT 1")
    Person getParentWithParamAttributes(@Param("firstName") String firstName, @Param("lastName") String lastname);
}
