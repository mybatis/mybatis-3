package org.apache.ibatis.submitted.mapperparams;

import org.apache.ibatis.annotations.Param;

public interface Mapper {

  int countFail(int id, String name);
  int countOrdinalPositions(int id, String name);
  int countNew31Names(int id, String name);
  int countWithValue(@Param("table") String table, @Param("id") int id, @Param("value") String value);
  
}
