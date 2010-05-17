package org.apache.ibatis.submitted.nested;

import java.util.ArrayList;
import java.util.List;

public class Parameter {
  private List<Name> names;

  public Parameter() {
    names = new ArrayList<Name>();
  }

  public List<Name> getNames() {
    return names;
  }

  public void addName(Name name) {
    names.add(name);
  }
}
