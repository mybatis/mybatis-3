package com.ibatis.jpetstore.persistence.iface;

public interface SequenceDao {

  int getNextId(String name);

}
