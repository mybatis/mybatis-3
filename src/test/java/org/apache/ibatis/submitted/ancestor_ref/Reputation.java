package org.apache.ibatis.submitted.ancestor_ref;

public class Reputation {

  private int value;

  private Author author;

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public Author getAuthor() {
    return author;
  }

  public void setAuthor(Author author) {
    this.author = author;
  }

}
