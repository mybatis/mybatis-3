package org.apache.ibatis.submitted.parent_child_circular;

public class Node {

  private int id;
  private Node parent;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return id + " (" + parent + ")";
  }
}
