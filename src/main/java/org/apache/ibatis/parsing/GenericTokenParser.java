/*
 *    Copyright 2009-2012 the original author or authors.
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
package org.apache.ibatis.parsing;

/**
 * @author Clinton Begin
 */
public class GenericTokenParser {

  private final String openToken;
  private final String closeToken;
  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) {
    StringBuilder builder = new StringBuilder();
    if (text != null && text.length() > 0) {
      char[] src = text.toCharArray();
      int offset = 0;
      int start = text.indexOf(openToken, offset);
      while (start > -1) {
        if (start > 0 && src[start - 1] == '\\') {
          // the variable is escaped. remove the backslash.
          builder.append(src, offset, start - offset - 1).append(openToken);
          offset = start + openToken.length();
        } else {
          int end = text.indexOf(closeToken, start);
          if (end == -1) {
            builder.append(src, offset, src.length - offset);
            offset = src.length;
          } else {
            builder.append(src, offset, start - offset);
            offset = start + openToken.length();
            String content = new String(src, offset, end - offset);
            builder.append(handler.handleToken(content));
            offset = end + closeToken.length();
          }
        }
        start = text.indexOf(openToken, offset);
      }
      if (offset < src.length) {
        builder.append(src, offset, src.length - offset);
      }
    }
    return builder.toString();
  }

}
