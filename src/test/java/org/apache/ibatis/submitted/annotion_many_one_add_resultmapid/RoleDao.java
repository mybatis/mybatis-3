/**
 * Copyright (c) 2019 ucsmy.com, All rights reserved.
 */
package org.apache.ibatis.submitted.annotion_many_one_add_resultmapid;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description:
 * @Author: lvyang
 * @Created Date: 2019年12月10日
 * @LastModifyDate:
 * @LastModifyBy:
 * @Version:
 */
public interface RoleDao {
  @Select("select * from role")
  @Results(
    id="roleMap1",
    value = {
      @Result(id = true,column="role_id",property = "id"),
      @Result(column="role_name",property = "roleName")
    }
  )
  public List<Role> findAll();
}
