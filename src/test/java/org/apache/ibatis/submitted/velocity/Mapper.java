package org.apache.ibatis.submitted.velocity;

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
  @Select("SELECT firstName <if test=\"includeLastName != null\">, lastName</if> FROM names WHERE lastName LIKE #{name}")
  List<Name> selectXmlWithMapper(Parameter p);

  @Select("SELECT firstName <if test=\"includeLastName != null\">, lastName</if> FROM names WHERE lastName LIKE #{name}")
  List<Name> selectVelocityWithMapper(Parameter p);

}
