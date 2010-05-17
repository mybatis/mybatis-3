package org.apache.ibatis.submitted.map_class_name_conflict.mapper;

import org.apache.ibatis.submitted.map_class_name_conflict.business.Person;

public interface PersonMapper {

  public Person get(Long id);

  public void insert(Person person);
}
