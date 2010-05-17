package org.apache.ibatis.builder.xml.dynamic;

public interface SqlNode {
  boolean apply(DynamicContext context);
}
