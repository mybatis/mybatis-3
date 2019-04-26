package org.apache.ibatis.builder.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.model.UserDTO;

import java.util.List;

public interface UserMapper {

    List<UserDTO> findUserListByName(@Param("name") String name);

    int updateByName(@Param("name") String name ,@Param("age") int age);
}

