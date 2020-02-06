package org.apache.ibatis.submitted.annotion_many_one_add_columnprefix;

public class Role {
  private Integer id;

  @Override
  public String toString() {
    return "Role{" +
        "id=" + id +
        ", roleName='" + roleName + '\'' +
        '}';
  }

  private String roleName;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }
}
