/**
 * Copyright (c) 2019 ucsmy.com, All rights reserved.
 */
package org.apache.ibatis.submitted.annotion_many_one_add_resultmapid;

import org.apache.ibatis.annotations.*;

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

  @Select("select\n" +
    "     u.id,u.username,r.id role_id,r.role_name\n" +
    "    from user u\n" +
    "    left join user_role ur on u.id =ur.user_id\n" +
    "    left join role r on ur.role_id = r.id where u.id in (2,3)")
  @Results({
    @Result(id = true,column="id",property = "id"),
    @Result(column="username",property = "username"),
    @Result(property = "roles",one = @One(resultMapId = "org.apache.ibatis.submitted.annotion_many_one_add_resultmapid.RoleDao.roleMap2"))
  })
  public List<User> flindAll3();
}
