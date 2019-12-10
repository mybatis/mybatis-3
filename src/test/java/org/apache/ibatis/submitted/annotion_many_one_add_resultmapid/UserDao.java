/**
 * Copyright (c) 2019 ucsmy.com, All rights reserved.
 */
package org.apache.ibatis.submitted.annotion_many_one_add_resultmapid;

import org.apache.ibatis.annotations.Many;
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
public interface UserDao {
    @Select("select\n" +
      "     u.id,u.username,r.id role_id,r.role_name\n" +
      "    from user u\n" +
      "    left join user_role ur on u.id =ur.user_id\n" +
      "    left join role r on ur.role_id = r.id")
    @Results({
      @Result(id = true,column="id",property = "id"),
      @Result(column="username",property = "username"),
      @Result(property = "roles",many = @Many(resultMapId = "org.apache.ibatis.submitted.annotion_many_one_add_resultmapid.RoleDao.roleMap1"))
    })
    public List<User> flindAll();

  @Select("select\n" +
    "     u.id,u.username,r.id role_id,r.role_name\n" +
    "    from user u\n" +
    "    left join user_role ur on u.id =ur.user_id\n" +
    "    left join role r on ur.role_id = r.id")
  @Results({
    @Result(id = true,column="id",property = "id"),
    @Result(column="username",property = "username"),
    @Result(property = "roles",many = @Many(resultMapId = "org.apache.ibatis.submitted.annotion_many_one_add_resultmapid.RoleDao.roleMap2"))
  })
  public List<User> flindAll2();
}
