package domain.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RichType {

  private RichType richType;

  private String richField;

  private String richProperty;

  private Map richMap = new HashMap();

  private List richList = new ArrayList() {
    {
      add("bar");
    }
  };

  public RichType getRichType() {
    return richType;
  }

  public void setRichType(RichType richType) {
    this.richType = richType;
  }

  public String getRichProperty() {
    return richProperty;
  }

  public void setRichProperty(String richProperty) {
    this.richProperty = richProperty;
  }

  public List getRichList() {
    return richList;
  }

  public void setRichList(List richList) {
    this.richList = richList;
  }

  public Map getRichMap() {
    return richMap;
  }

  public void setRichMap(Map richMap) {
    this.richMap = richMap;
  }
}
