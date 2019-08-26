/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.parsing;

/**
 * 通用的 token 解析器
 * @author Clinton Begin
 */
public class GenericTokenParser {

    /**
     * 开始的 token 字符
     */
    private final String openToken;
    /**
     * 结束的 token 字符
     */
    private final String closeToken;
    private final TokenHandler handler;

    public GenericTokenParser(String openToken, String closeToken, TokenHandler handler) {
        this.openToken = openToken;
        this.closeToken = closeToken;
        this.handler = handler;
    }

    /**
     * token 解析
     * @param text
     * @return
     */
    public String parse(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // search open token
        // 定位 openToken
        int start = text.indexOf(openToken);
        if (start == -1) { // 找不到,直接返回
            return text;
        }
        char[] src = text.toCharArray();

        int offset = 0;  // 起始查找位置
        // 结果
        final StringBuilder builder = new StringBuilder();
        // 匹配到 openToken 和 closeToken 之间的表达式
        StringBuilder expression = null;
        // 循环匹配
        while (start > -1) {
            // 转义字符
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                // 因为 openToken 前面一个位置是 \ 转义字符,所以忽略 \
                // 将 src 中 [offset,start - offset - 1] 段 和openToken 中的内容,添加到 builder 中
                builder.append(src, offset, start - offset - 1).append(openToken);
                // 修改 offset
                offset = start + openToken.length();
            } else {
                // 非转义字符
                // found open token. let's search close token.
                // 创建或重置 expression
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                // 将 offset 和 openToken 之间的内容,添加到 builder中
                builder.append(src, offset, start - offset);
                // 修改 offset
                offset = start + openToken.length();
                // 寻找 closeToken 位置
                int end = text.indexOf(closeToken, offset);
                // 循环匹配,提取待处理的 expression
                while (end > -1) {
                    // 转义字符
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    // 交由 handler 处理 expression 并将结果拼接到 builder 中
                    builder.append(handler.handleToken(expression.toString()));
                    offset = end + closeToken.length();
                }
            }
            start = text.indexOf(openToken, offset);
        }
        // 拼接剩余部分
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
    }
}
