package org.apache.ibatis.submitted.expand_collection_param.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.submitted.expand_collection_param.model.UserRole;

import java.util.List;

class StatementProvider {
  public String getUsers(@Param("id") Integer id, @Param("roles") List<UserRole> roles) {
    SQL sql = new SQL();
    sql.SELECT("*");
    sql.FROM("users");
    if (!roles.isEmpty()) {
      sql.WHERE("role in (#{roles...})");
    }
    sql.WHERE("id != #{id}");
    sql.ORDER_BY("id");
    return sql.toString();
  }
}
