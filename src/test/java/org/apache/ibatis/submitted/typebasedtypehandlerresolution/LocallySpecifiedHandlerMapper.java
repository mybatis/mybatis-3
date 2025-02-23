/*
 *    Copyright 2009-2025 the original author or authors.
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
package org.apache.ibatis.submitted.typebasedtypehandlerresolution;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;

public interface LocallySpecifiedHandlerMapper {

  @Result(column = "id", property = "id", id = true)
  @Result(column = "strvalue", property = "strvalue", typeHandler = TypeAwareTypeHandler.class)
  @Result(column = "intvalue", property = "intvalue", typeHandler = TypeAwareTypeHandler.class)
  @Result(column = "datevalue", property = "datevalue", typeHandler = TypeAwareTypeHandler.class)
  @Result(column = "datevalue2", property = "datevalue2")
  @Result(column = "strings", property = "strings", typeHandler = CsvTypeHandler.class)
  @Result(column = "integers", property = "integers", typeHandler = CsvTypeHandler.class)
  @Select("select * from users where id = #{id}")
  User getUser(Integer id);

  @Result(column = "id", property = "id", id = true)
  @Result(column = "datevalue2", property = "datevalue2")
  @Select("select id, datevalue2 from users")
  List<User> getAllUsers();

  @Insert({ "insert into users (id, strvalue, intvalue, datevalue, datevalue2, strings, integers) values (#{id} ",
      ", #{strvalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{intvalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{datevalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{datevalue2}",
      ", #{strings,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.CsvTypeHandler}",
      ", #{integers,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.CsvTypeHandler}", ")" })
  void insertUser(User user);

  @Insert({ "insert into users (id, strvalue, intvalue, datevalue, datevalue2, strings, integers) values (#{user.id} ",
      ", #{user.strvalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{user.intvalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{user.datevalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{user.datevalue2}",
      ", #{user.strings,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.CsvTypeHandler}",
      ", #{user.integers,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.CsvTypeHandler}", ")" })
  void insertUserMultiParam(User user, String foo);

  @Insert({ "insert into users (id, strvalue, intvalue, datevalue, datevalue2, strings, integers) values (#{user.id} ",
      ", #{user.strvalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{user.intvalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{user.datevalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{user.datevalue2}",
      ", #{user.strings,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.CsvTypeHandler}",
      ", #{user.integers,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.CsvTypeHandler}", ")" })
  void insertUserWithParam(@Param("user") User user);

  @Insert({ "insert into users (id, strvalue, intvalue, datevalue, datevalue2, strings, integers) values (#{id} ",
      ", #{strvalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{intvalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{datevalue,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.TypeAwareTypeHandler}",
      ", #{datevalue2}",
      ", #{strings,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.CsvTypeHandler}",
      ", #{integers,typeHandler=org.apache.ibatis.submitted.typebasedtypehandlerresolution.CsvTypeHandler}", ")" })
  void insertUserParamMap(@Param("id") Integer id, @Param("strvalue") FuzzyBean<String> strvalue,
      @Param("intvalue") FuzzyBean<Integer> intvalue, @Param("datevalue") LocalDate datevalue,
      @Param("datevalue2") LocalDate datevalue2, @Param("strings") List<String> strings,
      @Param("integers") List<Integer> integers);

  void insertXmlWithMapperMethod(User user);

  void insertXmlWithMapperMethodMultiParam(User user, String foo);

  void insertXmlWithMapperMethodWithParam(@Param("user") User user);

}
