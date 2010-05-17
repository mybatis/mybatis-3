package org.apache.ibatis.submitted.nested;

import java.util.ArrayList;
import java.util.List;

public class Name {
  private String lastName;
  private List<String> firstNames;

  public Name() {
    firstNames = new ArrayList<String>();
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public List<String> getFirstNames() {
    return firstNames;
  }

  public void addFirstName(String firstName) {
    firstNames.add(firstName);
  }
}
