package org.apache.ibatis.submitted.collection_donot_need_oftype_no_reflector_cache;


import org.testcontainers.shaded.com.fasterxml.jackson.databind.util.BeanUtil;

import java.util.*;
import java.util.stream.Collectors;

public class User {
  private Integer id;	//int
  private String username;	//varchar
  private List<Role> roles;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Map<String,Object>> roles) {
    List<Role> roles2 = roles.stream().map(map -> {
      return BeanUtils.mapToBean(map, Role.class);
    }).collect(Collectors.toList());
    this.roles=roles2;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = new ArrayList<>(roles);
  }


}
