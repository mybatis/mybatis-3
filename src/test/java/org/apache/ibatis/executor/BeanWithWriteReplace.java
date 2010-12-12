package org.apache.ibatis.executor;

import java.io.ObjectStreamException;
import java.io.Serializable;

public class BeanWithWriteReplace implements Serializable {
  
  protected int id;
  protected String value;

  public BeanWithWriteReplace() {  
  }
  
  public BeanWithWriteReplace(int id, String value) {
    super();
    this.id = id;
    this.value = value;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + id;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BeanWithWriteReplace other = (BeanWithWriteReplace) obj;
    if (id != other.id)
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "BeanWithWriteReplace [id=" + id + ", value=" + value + "]";
  }

  protected Object writeReplace() throws ObjectStreamException {
    return this;
  }
}