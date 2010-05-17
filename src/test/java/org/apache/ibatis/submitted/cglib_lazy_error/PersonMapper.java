package org.apache.ibatis.submitted.cglib_lazy_error;

public interface PersonMapper {

  public Person selectById(int id);

  public Person selectByStringId(String id);

  public int insertPerson(Person person);

}
