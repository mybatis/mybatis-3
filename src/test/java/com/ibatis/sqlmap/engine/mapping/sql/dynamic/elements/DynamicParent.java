package com.ibatis.sqlmap.engine.mapping.sql.dynamic.elements;

import com.ibatis.sqlmap.engine.mapping.sql.SqlChild;

public interface DynamicParent {

  public void addChild(SqlChild child);

}
