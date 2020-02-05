package org.apache.ibatis.submitted.annotion_many_one_add_columnprefix;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author lvyang
 */
public interface UserDao {
  @Select({ "select",
    "     u.id, u.username, r.id role_id, r.role_name",
    "    from user u",
    "    left join user_role ur on u.id = ur.user_id",
    "    left join role r on ur.role_id = r.id" })
  @Results({
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(property = "roles", many = @Many(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.RoleDao.roleMap1",columnPrefix = "role_"))
  })
  public List<User> findAll();

  @Select({ "select",
    "     u.id, u.username, r.id role_id, r.role_name",
    "    from user u",
    "    left join user_role ur on u.id = ur.user_id",
    "    left join role r on ur.role_id = r.id" })
  @Results({
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(property = "roles", many = @Many(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.RoleDao.roleMap2",columnPrefix = "role_"))
  })
  public List<User> findAll2();

  @Select({ "select",
    "     u.id, u.username, r.id role_id, r.role_name",
    "    from user u",
    "    left join user_role ur on u.id = ur.user_id",
    "    left join role r on ur.role_id = r.id where u.id in (2, 3)" })
  @Results({
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(property = "role", one = @One(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.RoleDao.roleMap2",columnPrefix = "role_"))
  })
  public List<User> findAll3();

  @Select("select id teacher_id, username teacher_name from user")
  @Results(id = "userMap", value = {
    @Result(id = true, column = "teacher_id", property = "id"),
    @Result(column = "teacher_name", property = "username")
  })
  public List<User> justUseResult();

  @Select({ "select",
    "u.id, u.username, r.id role_id, r.role_name, ut.id teacher_id, ut.username teacher_name",
    "from user u",
    "left join user_role ur on u.id = ur.user_id",
    "left join role r on ur.role_id = r.id",
    "left join user ut on ut.id != u.id",
    "where role_id = 3" })
  @Results({
    @Result(id = true, column = "id", property = "id"),
    @Result(column = "username", property = "username"),
    @Result(property = "role", one = @One(resultMap = "org.apache.ibatis.submitted.annotion_many_one_add_columnprefix.RoleDao.roleMap2",columnPrefix = "role_")),
    @Result(property = "teachers", many = @Many(resultMap = "userMap"))
  })
  public User findHeadmaster();
}
