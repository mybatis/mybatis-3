package org.apache.ibatis.submitted.batch_keys;

public interface Mapper {
  
  void insert(User user);
  void insertIdentity(User user);

}
