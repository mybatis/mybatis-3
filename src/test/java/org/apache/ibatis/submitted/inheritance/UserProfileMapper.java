package org.apache.ibatis.submitted.inheritance;

import org.apache.ibatis.annotations.Select;

public interface UserProfileMapper extends BaseMapper<UserProfile> {
  @Override
  @Select("select * from user_profile")
  UserProfile retrieveById(Integer id);
}
