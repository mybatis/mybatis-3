package org.apache.ibatis.submitted.annotion_many_one_add_columnprefix;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.submitted.annotion_many_one_add_resultmapid.Role;

import java.util.List;

/**
 * @author lvyang
 */
public interface RoleDao {
  @Select("select * from role")
  @Results(id = "roleMap1", value = {
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "name", property = "roleName")
  })
  public List<Role> findAll();
}
