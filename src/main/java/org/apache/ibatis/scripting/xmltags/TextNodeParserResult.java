/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
