package org.apache.ibatis.submitted.annotion_many_one_add_resultmapid;


import java.util.List;

public class User {
  private Integer id;	//int
  private String username;	//varchar
  private List<User> teachers;
  private Role role;
  private List<Role> roles;

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }


  @Override
  public String toString() {
    return "User{" +
      "id=" + id +
      ", username='" + username + '\'' +
      ", roles=" + roles +
      '}';
  }

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
  public List<User> getTeachers() {
    return teachers;
  }

  public void setTeachers(List<User> teachers) {
    this.teachers = teachers;
  }
}
