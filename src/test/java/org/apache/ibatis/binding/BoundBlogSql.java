package org.apache.ibatis.binding;

import static org.apache.ibatis.jdbc.SelectBuilder.*;

public class BoundBlogSql {

  public String selectBlogsSql() {
    BEGIN();
    SELECT("*");
    FROM("BLOG");
    return SQL();
  }

}
