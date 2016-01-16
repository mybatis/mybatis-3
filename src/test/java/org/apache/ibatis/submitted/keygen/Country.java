package org.apache.ibatis.submitted.keygen;

/**
 * @author liuzh
 */
public class Country {
  private Integer id;
  private String countryname;
  private String countrycode;

  public Country() {
  }

  public Country(String countryname, String countrycode) {
    this.countryname = countryname;
    this.countrycode = countrycode;
  }

  public Country(Integer id, String countryname, String countrycode) {
    this.id = id;
    this.countryname = countryname;
    this.countrycode = countrycode;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getCountryname() {
    return countryname;
  }

  public void setCountryname(String countryname) {
    this.countryname = countryname;
  }

  public String getCountrycode() {
    return countrycode;
  }

  public void setCountrycode(String countrycode) {
    this.countrycode = countrycode;
  }
}
