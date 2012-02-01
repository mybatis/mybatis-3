package org.apache.ibatis.submitted.generictypes;

import org.apache.ibatis.annotations.Select;

public interface Mapper {

  @Select("select id, owner, members from groups where id=1")
  Group getGroup();

}
