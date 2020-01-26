package org.apache.ibatis.submitted.expand_collection_param.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.submitted.expand_collection_param.model.User;
import org.apache.ibatis.submitted.expand_collection_param.model.UserRole;
import org.apache.ibatis.submitted.expand_collection_param.model.UserStatus;

import java.util.List;

public interface AnnotationMapper {

  @Select("select * from users WHERE id in (#{ids...}) order by id")
  List<User> getUsersByIds(@Param("ids") List<Integer> ids);

  @Select("select * from users WHERE id in (#{ids...}) order by id")
  List<User> getUsersByArrayIds(@Param("ids") int[] ids);

  @Select("select * from users WHERE first_name in (#{names...}) order by id")
  List<User> getUsersByNames(@Param("names") List<String> names);

  @Select("select * from users WHERE role in (#{roles...}) order by id")
  List<User> getUsersByRoles(@Param("roles") List<UserRole> roles);

  @Select("select * from users WHERE status in (#{status...}) order by id")
  List<User> getUsersByStatus(@Param("status") List<UserStatus> status);

  @Select("select * from users WHERE status in (#{status...}) and role in (#{roles...}) order by id")
  List<User> getUsersByStatusAndRoles(@Param("status") List<UserStatus> status, @Param("roles") List<UserRole> roles);


}
