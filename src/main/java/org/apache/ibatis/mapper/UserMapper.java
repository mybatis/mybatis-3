package org.apache.ibatis.mapper;

import org.apache.ibatis.entity.User;

public interface UserMapper {

    User selectById(Integer id);
}
