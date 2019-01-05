package org.apache.ibatis.plugin.encryption;


import java.util.Date;
import org.apache.ibatis.annotations.Encryption;

/**
 * @author SoungRyoul Kim Thank my mentor Ikchan Sim who taught me.
 */
public class Customer {

  private String id;

  @Encryption(type = CryptogramType.SHA256)
  private String name;

  @Encryption(type = CryptogramType.AES256)
  private String email;

  private String description;

  private Date regDate;

  private int mileage;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getRegDate() {
    return regDate;
  }

  public void setRegDate(Date regDate) {
    this.regDate = regDate;
  }

  public int getMileage() {
    return mileage;
  }

  public void setMileage(int mileage) {
    this.mileage = mileage;
  }

  @Override
  public String toString() {
    return "Customer{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", email='" + email + '\'' +
        ", description='" + description + '\'' +
        ", regDate=" + regDate +
        ", mileage=" + mileage +
        '}';
  }
}
