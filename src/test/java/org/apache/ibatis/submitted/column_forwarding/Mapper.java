package org.apache.ibatis.submitted.column_forwarding;

import org.apache.ibatis.annotations.Param;

public interface Mapper {
  public User getUser(@Param("id") int id);
}
