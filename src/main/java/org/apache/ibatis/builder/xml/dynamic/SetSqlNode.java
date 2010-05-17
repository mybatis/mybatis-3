package org.apache.ibatis.builder.xml.dynamic;

import org.apache.ibatis.session.Configuration;

public class SetSqlNode extends TrimSqlNode {

  public SetSqlNode(Configuration configuration,SqlNode contents) {
    super(configuration, contents, "SET", null, null, ",");
  }

}
