package domain.misc;

import java.util.Date;

public class Employee {

  private int id;
  private String firstName;
  private String lastName;
  private Date birthDate;
  private double height;
  private double weight;
  private String heightUnits;
  private String weightUnits;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public String getHeightUnits() {
    return heightUnits;
  }

  public void setHeightUnits(String heightUnits) {
    this.heightUnits = heightUnits;
  }

  public String getWeightUnits() {
    return weightUnits;
  }

  public void setWeightUnits(String weightUnits) {
    this.weightUnits = weightUnits;
  }
}
