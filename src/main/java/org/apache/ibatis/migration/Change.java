package org.apache.ibatis.migration;

import java.math.BigDecimal;

public class Change implements Comparable<Change> {

  private BigDecimal id;
  private String description;
  private String appliedTimestamp;
  private String filename;

  public Change() {
  }

  public Change(BigDecimal id) {
    this.id = id;
  }

  public Change(BigDecimal id, String appliedTimestamp, String description) {
    this.id = id;
    this.appliedTimestamp = appliedTimestamp;
    this.description = description;
  }

  public BigDecimal getId() {
    return id;
  }

  public void setId(BigDecimal id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAppliedTimestamp() {
    return appliedTimestamp;
  }

  public void setAppliedTimestamp(String appliedTimestamp) {
    this.appliedTimestamp = appliedTimestamp;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String toString() {
    return id + " " + (appliedTimestamp == null ? "   ...pending...   " : appliedTimestamp) + " " + description;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Change change = (Change) o;

    return (id.equals(change.getId()));
  }

  public int hashCode() {
    return id.hashCode();
  }

  public int compareTo(Change change) {
    return id.compareTo(change.getId());
  }
}
