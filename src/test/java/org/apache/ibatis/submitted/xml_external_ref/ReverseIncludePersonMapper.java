package org.apache.ibatis.submitted.xml_external_ref;

public interface ReverseIncludePersonMapper {
  Person select(Integer id);

  Pet selectPet(Integer id);
}
