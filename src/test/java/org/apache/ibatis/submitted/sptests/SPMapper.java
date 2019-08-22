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
package org.apache.ibatis.submitted.sptests;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.StatementType;

public interface SPMapper {
  // XML based
  Object adderAsSelect(Parameter parameter);

  void adderAsUpdate(Parameter parameter);

  void adderWithParameterMap(Map<String, Object> parameter);

  Name getName(Integer id);

  List<Name> getNames(Map<String, Object> parms);

  List<Name> getNamesWithArray(Map<String, Object> parms);

  List<List<?>> getNamesAndItems();

  List<Name> getNamesAndItemsLinked();

  List<Name> getNamesAndItemsLinkedById(int id);

  Object echoDate(Map<String, Object> parameter);  // issue #145

  // annotated
  @Select({ "{call sptest.adder(", "#{addend1,jdbcType=INTEGER,mode=IN},", "#{addend2,jdbcType=INTEGER,mode=IN},", "#{sum,jdbcType=INTEGER,mode=OUT})}" })
  @Options(statementType = StatementType.CALLABLE)
  Object adderAsSelectAnnotated(Parameter parameter);

  @Update({ "{call sptest.adder(", "#{addend1,jdbcType=INTEGER,mode=IN},", "#{addend2,jdbcType=INTEGER,mode=IN},", "#{sum,jdbcType=INTEGER,mode=OUT})}" })
  @Options(statementType = StatementType.CALLABLE)
  void adderAsUpdateAnnotated(Parameter parameter);

  @Select("{call sptest.getname(#{id,jdbcType=INTEGER,mode=IN})}")
  @Results({ @Result(column = "ID", property = "id"), @Result(column = "FIRST_NAME", property = "firstName"), @Result(column = "LAST_NAME", property = "lastName") })
  @Options(statementType = StatementType.CALLABLE)
  Name getNameAnnotated(Integer id);

  @Select("{call sptest.getname(#{id,jdbcType=INTEGER,mode=IN})}")
  @ResultMap("nameResult")
  @Options(statementType = StatementType.CALLABLE)
  Name getNameAnnotatedWithXMLResultMap(Integer id);

  @Select({ "{call sptest.getnames(", "#{lowestId,jdbcType=INTEGER,mode=IN},", "#{totalRows,jdbcType=INTEGER,mode=OUT})}" })
  @Results({ @Result(column = "ID", property = "id"), @Result(column = "FIRST_NAME", property = "firstName"), @Result(column = "LAST_NAME", property = "lastName") })
  @Options(statementType = StatementType.CALLABLE)
  List<Name> getNamesAnnotated(Map<String, Object> parms);

  @Select({ "{call sptest.getnames(", "#{lowestId,jdbcType=INTEGER,mode=IN},", "#{totalRows,jdbcType=INTEGER,mode=OUT})}" })
  @ResultMap("nameResult")
  @Options(statementType = StatementType.CALLABLE)
  List<Name> getNamesAnnotatedWithXMLResultMap(Map<String, Object> parms);

  @Select({ "{call sptest.getnamesLowHigh(", "#{lowestId,jdbcType=INTEGER,mode=IN},", "#{highestId,jdbcType=INTEGER,mode=IN})}" })
  @ResultMap("nameResult")
  @Options(statementType = StatementType.CALLABLE)
  List<Name> getNamesAnnotatedLowHighWithXMLResultMap(@Param("lowestId") int lowestId, @Param("highestId") int highestId);

  @Select({ "{call sptest.arraytest(", "#{ids,mode=IN,jdbcType=ARRAY},", "#{requestedRows,jdbcType=INTEGER,mode=OUT},", "#{returnedIds,mode=OUT,jdbcType=ARRAY})}" })
  @Results({ @Result(column = "ID", property = "id"), @Result(column = "FIRST_NAME", property = "firstName"), @Result(column = "LAST_NAME", property = "lastName") })
  @Options(statementType = StatementType.CALLABLE)
  List<Name> getNamesWithArrayAnnotated(Map<String, Object> parms);

  @Select({ "{call sptest.arraytest(", "#{ids,mode=IN,jdbcType=ARRAY},", "#{requestedRows,jdbcType=INTEGER,mode=OUT},", "#{returnedIds,mode=OUT,jdbcType=ARRAY})}" })
  @ResultMap("nameResult")
  @Options(statementType = StatementType.CALLABLE)
  List<Name> getNamesWithArrayAnnotatedWithXMLResultMap(Map<String, Object> parms);

  @Select("{call sptest.getnamesanditems()}")
  @ResultMap("nameResult,itemResult")
  @Options(statementType = StatementType.CALLABLE)
  List<List<?>> getNamesAndItemsAnnotatedWithXMLResultMap();

  @Select("{call sptest.getnamesanditems()}")
  @ResultMap({"nameResult","itemResult"})
  @Options(statementType = StatementType.CALLABLE)
  List<List<?>> getNamesAndItemsAnnotatedWithXMLResultMapArray();

  List<Book> getBookAndGenre();
}
