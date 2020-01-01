package org.apache.ibatis.submitted.collection_donot_need_oftype_no_reflector_cache;


import org.testcontainers.shaded.com.fasterxml.jackson.databind.util.BeanUtil;

import java.util.*;
import java.util.stream.Collectors;

public class User {
  private Integer id;	//int
  private String username;	//varchar
  private List<Map<String,Object>> roles = new ArrayList<>();

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

  public List<Map<String,Object>> getRoles() {
    return roles;
  }
}
