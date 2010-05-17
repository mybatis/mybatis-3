package org.apache.ibatis.submitted.complex_property;

public class User {
  private Long id;

  /**
   * user specified user ID
   */
  private String username;

  /**
   * encrypted password
   */
  private EncryptedString password;

  boolean administrator;

  public User() {
    setUsername(new String());
    setPassword(new EncryptedString());
    setAdministrator(false);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String arg) {
    this.username = arg;
  }

  public EncryptedString getPassword() {
    return password;
  }

  public void setPassword(EncryptedString arg) {
    this.password = arg;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long oid) {
    this.id = oid;
  }

  public boolean isAdministrator() {
    return administrator;
  }

  public void setAdministrator(boolean arg) {
    this.administrator = arg;
  }
}
