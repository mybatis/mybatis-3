package org.apache.ibatis.builder.xml.dynamic;

import org.apache.ibatis.session.Configuration;

public class WhereSqlNode extends TrimSqlNode {

  public WhereSqlNode(Configuration configuration, SqlNode contents) {
    super(configuration, contents, "WHERE", "AND |OR ", null, null);
  }


}
