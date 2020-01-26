package org.apache.ibatis.scripting.xmltags;

import java.util.List;

public class TextNodeParserResult {
  public final List<SqlNode> nodes;
  public final boolean isDynamic;

  public TextNodeParserResult(List<SqlNode> nodes, boolean isDynamic) {
    this.nodes = nodes;
    this.isDynamic = isDynamic;
  }

  public SqlNode toSingleSqlNode() {
    if (nodes.size() == 1) {
      return nodes.get(0);
    } else {
      return new MixedSqlNode(nodes);
    }
  }
}
