package org.apache.ibatis.builder.xml.dynamic;

import java.util.List;

public class ChooseSqlNode implements SqlNode {
  private SqlNode defaultSqlNode;
  private List<IfSqlNode> ifSqlNodes;

  public ChooseSqlNode(List<IfSqlNode> ifSqlNodes, SqlNode defaultSqlNode) {
    this.ifSqlNodes = ifSqlNodes;
    this.defaultSqlNode = defaultSqlNode;
  }

  public boolean apply(DynamicContext context) {
    for (SqlNode sqlNode : ifSqlNodes) {
      if (sqlNode.apply(context)) {
        return true;
      }
    }
    if (defaultSqlNode != null) {
      defaultSqlNode.apply(context);
      return true;
    }
    return false;
  }
}
