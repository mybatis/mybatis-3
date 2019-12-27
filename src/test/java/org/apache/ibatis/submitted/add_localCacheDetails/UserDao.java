package org.apache.ibatis.submitted.add_localCacheDetails;

import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.LocalCacheScope;

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
  List<User> findAll();

  List<User> findAll2();

  List<User> findAll3();

  @Options()
  @Select("select * from user")
  List<User> findAll4();

  @Options(localCacheScope = LocalCacheScope.NOUSE)
  @Select("select * from user")
  List<User> findAll5();
}
