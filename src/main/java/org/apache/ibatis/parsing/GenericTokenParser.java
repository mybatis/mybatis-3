/**
 *    Copyright 2009-2019 the original author or authors.
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
 * 通用的 Token 解析器
 * @author Clinton Begin
 */
public class GenericTokenParser {

  // 开始的 Token 字符串
  private final String openToken;
  // 结束的 Token 字符串
  private final String closeToken;
  private final TokenHandler handler;

  public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
    this.openToken = openToken;
    this.closeToken = closeToken;
    this.handler = handler;
  }

  public String parse(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    // search open token
    // 寻找开始的 openToken 的位置
    int start = text.indexOf(openToken);
    if (start == -1) { // 找不到，直接返回
      return text;
    }
    char[] src = text.toCharArray();
    int offset = 0; // 起始查找位置
    // 结果
    final StringBuilder builder = new StringBuilder();
    StringBuilder expression = null; // 匹配到 openToken 和 closeToken 之间的表达式
    // 循环匹配
    while (start > -1) {
      // 转义字符
      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. remove the backslash and continue.
        // 因为 openToken 前面一个位置是 \ 转义字符，所以忽略 \
        // 添加 [offset, start - offset - 1] 和 openToken 的内容，添加到 builder 中
        builder.append(src, offset, start - offset - 1).append(openToken);
        // 修改 offset
        offset = start + openToken.length();
      } else { // 非转义
        // found open token. let's search close token.
        // 创建或重置 expression 对象
        if (expression == null) {
          expression = new StringBuilder();
        } else {
          expression.setLength(0);
        }
        // 添加 offset 和 openToken 之间的内容到 builder 中
        builder.append(src, offset, start - offset);
        // 修改 offset
        offset = start + openToken.length();
        // 寻找结束的 closeToken 的位置
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          // 转义字符
          if (end > offset && src[end - 1] == '\\') {
            // this close token is escaped. remove the backslash and continue.
            // 因为 closeToken 前面一个位置是 \ 转义字符，所以忽略 \
            // 添加 [offset, end - offset - 1] 和 closeToken 的内容，添加到 expression 中
            expression.append(src, offset, end - offset - 1).append(closeToken);
            // 修改 offset
            offset = end + closeToken.length();
            // 寻找结束的 closeToken 的位置
            end = text.indexOf(closeToken, offset);
          } else { // 非转义
            // 添加 offset 和 end-offset 的内容到 expression 中
            expression.append(src, offset, end - offset);
            // 修改 offset
            offset = end + closeToken.length();
            break;
          }
        }
        // 拼接内容
        if (end == -1) {
          // close token was not found.
          // closeToken 未找到
          builder.append(src, start, src.length - start);
          // 修改 offset
          offset = src.length;
        } else {
          // <x> closeToken 找到，将 expression 提交给 handler 处理，将处理结果添加到 builder 中
          builder.append(handler.handleToken(expression.toString()));
          // 修改 offset
          offset = end + closeToken.length();
        }
      }
      // 修改开始的 openToken 的位置
      start = text.indexOf(openToken, offset);
    }
    // 拼接剩余的部分
    if (offset < src.length) {
      builder.append(src, offset, src.length - offset);
    }
    return builder.toString();
  }
}
