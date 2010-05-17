package org.apache.ibatis.submitted.initialized_collection_property.test;

import java.util.ArrayList;
import java.util.List;

public class Author {
  public long id;

  public List<Post> posts = new ArrayList<Post>();

  public String name;
}