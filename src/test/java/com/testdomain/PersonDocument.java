package com.testdomain;

/**
 * @author Jeff Butler
 */
public class PersonDocument {

  private Integer id;
  private String name;
  private Document favoriteDocument;

  /**
   *
   */
  public PersonDocument() {
    super();
  }

  public Document getFavoriteDocument() {
    return favoriteDocument;
  }

  public void setFavoriteDocument(Document favoriteDocument) {
    this.favoriteDocument = favoriteDocument;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
