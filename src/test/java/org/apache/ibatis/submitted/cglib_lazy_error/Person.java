package org.apache.ibatis.submitted.cglib_lazy_error;

public class Person {

  private Long id;
  private String firstName;
  private String lastName;
  private Person parent;

  public Person getAncestor() {
    if (getParent() == null) {
      return this;
    } else {
      return getParent().getAncestor();
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Person)) return false;

    Person person = (Person) o;
    
    if (id != null ? !id.equals(person.id) : person.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

  @Override
  public String toString() {
    return id + ": " + firstName + " " + lastName + " ("+parent+")";
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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Person getParent() {
    return parent;
  }

  public void setParent(Person parent) {
    this.parent = parent;
  }
}
