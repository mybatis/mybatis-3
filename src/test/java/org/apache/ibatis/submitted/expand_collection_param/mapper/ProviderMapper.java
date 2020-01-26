package org.apache.ibatis.submitted.expand_collection_param.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.submitted.expand_collection_param.model.User;
import org.apache.ibatis.submitted.expand_collection_param.model.UserRole;

import java.util.List;

public interface ProviderMapper {

  @SelectProvider(type = StatementProvider.class, method = "getUsers")
  List<User> getUsers(@Param("id") Integer id, @Param("roles") List<UserRole> roles);


}
