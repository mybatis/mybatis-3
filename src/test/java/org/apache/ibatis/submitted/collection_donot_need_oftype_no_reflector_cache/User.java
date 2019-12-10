package org.apache.ibatis.submitted.collection_donot_need_oftype_no_reflector_cache;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = new ArrayList<>(roles);
  }

  public void setRoles(String roles) {
    this.roles = new ArrayList<>();
    Role role = new Role();
    role.setId(1);
    role.setRoleName("asd");
    this.roles.add(role);
  }
}
