package com.testdomain;

/**
 * Used in testing the ResultObjectFactory
 *
 * @author Jeff Butler
 */
public interface ISupplier extends ISupplierKey {

  String getAddressLine1();

  void setAddressLine1(String addressLine1);

  String getAddressLine2();

  void setAddressLine2(String addressLine2);

  String getCity();

  void setCity(String city);

  String getName();

  void setName(String name);

  String getPhone();

  void setPhone(String phone);

  String getState();

  void setState(String state);

  String getStatus();

  void setStatus(String status);

  String getZip();

  void setZip(String zip);
}
