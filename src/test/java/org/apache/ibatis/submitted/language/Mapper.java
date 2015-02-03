/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.submitted.language;

import java.util.List;

import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;

public interface Mapper {

  @Lang(RawLanguageDriver.class)
  @Select("SELECT firstName, lastName FROM names WHERE lastName LIKE #{name}")
  List<Name> selectRawWithMapper(Parameter p);

  @Lang(XMLLanguageDriver.class)
  @Select("<script>SELECT firstName <if test=\"includeLastName != null\">, lastName</if> FROM names WHERE lastName LIKE #{name}</script>")
  List<Name> selectXmlWithMapper(Parameter p);

  @Select("SELECT firstName #if($_parameter.includeLastName), lastName#end FROM names WHERE lastName LIKE @{name}")
  List<Name> selectVelocityWithMapper(Parameter p);

  @Lang(XMLLanguageDriver.class)
  @Select("SELECT firstName, lastName FROM names WHERE lastName LIKE #{name} and 0 < 1")
  List<Name> selectXmlWithMapperAndSqlSymbols(Parameter p);

}
