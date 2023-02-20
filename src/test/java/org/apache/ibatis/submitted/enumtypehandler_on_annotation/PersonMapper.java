/*
 *    Copyright 2009-2023 the original author or authors.
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
package org.apache.ibatis.submitted.enumtypehandler_on_annotation;

import org.apache.ibatis.annotations.Arg;
import org.apache.ibatis.annotations.Case;
import org.apache.ibatis.annotations.ConstructorArgs;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.TypeDiscriminator;
import org.apache.ibatis.submitted.enumtypehandler_on_annotation.Person.PersonType;
import org.apache.ibatis.type.EnumOrdinalTypeHandler;

/**
 * @since #444
 *
 * @author Kazuki Shimizu
 */
public interface PersonMapper {

  // @formatter:off
  @ConstructorArgs({
        @Arg(column = "id", javaType = Integer.class, id = true)
      , @Arg(column = "firstName", javaType = String.class)
      , @Arg(column = "lastName", javaType = String.class)
      // target for test (ordinal number -> Enum constant)
      , @Arg(column = "personType", javaType = PersonType.class, typeHandler = EnumOrdinalTypeHandler.class)
    })
  // @formatter:on
  @Select("SELECT id, firstName, lastName, personType FROM person WHERE id = #{id}")
  Person findOneUsingConstructor(int id);

  // @formatter:off
  @Results({
      // target for test (ordinal number -> Enum constant)
      @Result(property = "personType", column = "personType", typeHandler = EnumOrdinalTypeHandler.class)
    })
  // @formatter:on
  @Select("SELECT id, firstName, lastName, personType FROM person WHERE id = #{id}")
  Person findOneUsingSetter(int id);

  // @formatter:off
  @TypeDiscriminator(
      // target for test (ordinal number -> Enum constant)
      column = "personType", javaType = PersonType.class, typeHandler = EnumOrdinalTypeHandler.class,
      // Switch using enum constant name(PERSON or EMPLOYEE) at cases attribute
      cases = {
            @Case(value = "PERSON", type = Person.class, results = {@Result(property = "personType", column = "personType", typeHandler = EnumOrdinalTypeHandler.class)})
          , @Case(value = "EMPLOYEE", type = Employee.class, results = {@Result(property = "personType", column = "personType", typeHandler = EnumOrdinalTypeHandler.class)})
        })
  // @formatter:on
  @Select("SELECT id, firstName, lastName, personType FROM person WHERE id = #{id}")
  Person findOneUsingTypeDiscriminator(int id);

}
