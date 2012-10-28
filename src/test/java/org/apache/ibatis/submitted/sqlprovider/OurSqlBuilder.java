package org.apache.ibatis.submitted.sqlprovider;

import java.util.List;
import java.util.Map;

public class OurSqlBuilder {

  public String buildSqlQuery(Map allFilterIds) {
    return "select * from users";
  }
}
