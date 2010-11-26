package org.apache.ibatis.submitted.xml_external_ref;

public interface MultipleCrossIncludePersonMapper {
  Person select(Integer id);

  Pet selectPet(Integer id);
}
