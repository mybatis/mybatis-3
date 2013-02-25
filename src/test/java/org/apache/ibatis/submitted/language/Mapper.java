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
