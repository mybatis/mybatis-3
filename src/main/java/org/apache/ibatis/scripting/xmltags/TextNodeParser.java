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

import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextNodeParser {

  private final static String COLLECTION_OPEN_TOKEN = "#{";
  private final static String COLLECTION_CLOSE_TOKEN = "...}";
  private final static Pattern COLLECTION_PATTERN;

  static {
    String quotedOpenToken = Pattern.quote(COLLECTION_OPEN_TOKEN);
    String quotedCloseToken = Pattern.quote(COLLECTION_CLOSE_TOKEN);
    COLLECTION_PATTERN = Pattern.compile(quotedOpenToken + "([^}]*?)" + quotedCloseToken);
  }

  public static TextNodeParserResult parse(Configuration configuration, String text) {
    List<SqlNode> nodes = new ArrayList<>();
    boolean isDynamic = false;

    int index = 0;
    Matcher matcher = COLLECTION_PATTERN.matcher(text);
    while (matcher.find()) {
      isDynamic = true;

      //process text node before any collection operator
      if (matcher.start() > index) {
        String textPart = text.substring(index, matcher.start());
        TextSqlNode textSqlNode = new TextSqlNode(textPart);
        if (textSqlNode.isDynamic()) {
          nodes.add(textSqlNode);
        } else {
          nodes.add(new StaticTextSqlNode(textPart));
        }
      }

      //process collection operator
      String collectionExp = matcher.group();
      collectionExp = collectionExp.substring(COLLECTION_OPEN_TOKEN.length(), collectionExp.length() - COLLECTION_CLOSE_TOKEN.length());

      MixedSqlNode contents = new MixedSqlNode(Arrays.asList(new TextSqlNode("#{item}")));
      nodes.add(new ForEachSqlNode(configuration, contents, collectionExp, null, "item", null, null, ","));

      index = matcher.end();
    }

    //process remaining text
    if (index < text.length()) {
      String textPart = text.substring(index, text.length());
      TextSqlNode textSqlNode = new TextSqlNode(textPart);
      if (textSqlNode.isDynamic()) {
        nodes.add(textSqlNode);
        isDynamic = true;
      } else {
        nodes.add(new StaticTextSqlNode(textPart));
      }
    }

    return new TextNodeParserResult(nodes, isDynamic);
  }
}
