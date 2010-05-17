package com.testdomain;

/**
 * Used in testing the ResultObjectFactory
 *
 * @author Jeff Butler
 */
public class ISupplierImpl extends ISupplierKeyImpl implements ISupplier {
  private String name;
  private String status;
  private String addressLine1;
  private String addressLine2;
  private String city;
  private String state;
  private String zip;
  private String phone;

  /**
   *
   */
  public ISupplierImpl() {
    super();
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public void setAddressLine1(String addressLine1) {
    this.addressLine1 = addressLine1;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public void setAddressLine2(String addressLine2) {
    this.addressLine2 = addressLine2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

}
