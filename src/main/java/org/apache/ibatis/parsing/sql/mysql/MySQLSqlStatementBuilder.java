/**
 *    Copyright 2009-2018 the original author or authors.
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
package org.apache.ibatis.parsing.sql.mysql;

import org.apache.ibatis.parsing.sql.Delimiter;
import org.apache.ibatis.parsing.sql.SqlStatementBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlStatementBuilder supporting MySQL-specific delimiter changes.
 */
public class MySQLSqlStatementBuilder extends SqlStatementBuilder {

    private final String[] charSets = {
            "ARMSCII8", "ASCII", "BIG5", "BINARY", "CP1250", "CP1251", "CP1256", "CP1257", "CP850", "CP852", "CP866", "CP932",
            "DEC8", "EUCJPMS", "EUCKR", "GB2312", "GBK", "GEOSTD8", "GREEK", "HEBREW", "HP8", "KEYBCS2", "KOI8R", "KOI8U", "LATIN1",
            "LATIN2", "LATIN5", "LATIN7", "MACCE", "MACROMAN", "SJIS", "SWE7", "TIS620", "UCS2", "UJIS", "UTF8"
    };

    /*private -> testing*/ boolean isInMultiLineCommentDirective = false;

    public MySQLSqlStatementBuilder(Delimiter defaultDelimiter) {
        super(defaultDelimiter);
    }

    @Override
    public boolean isCommentDirective(String line) {
        // single-line comment directive
        if (line.matches("^" + Pattern.quote("/*!") + "\\d{5} .*" + Pattern.quote("*/") + "\\s*;?")) {
            return true;
        }
        // start of multi-line comment directive
        if (line.matches("^" + Pattern.quote("/*!") + "\\d{5} .*")) {
            isInMultiLineCommentDirective = true;
            return true;
        }
        // last line of multi-line comment directive
        if (isInMultiLineCommentDirective && line.matches(".*" + Pattern.quote("*/") + "\\s*;?")) {
            isInMultiLineCommentDirective = false;
            return true;
        }
        return isInMultiLineCommentDirective;
    }

    @Override
    protected boolean isSingleLineComment(String token) {
        return token.startsWith("--") || (token.startsWith("#") && !("#".equals(delimiter.getDelimiter()) && "#".equals(token)));
    }

    @Override
    protected String removeEscapedQuotes(String token) {
        String noEscapedBackslashes = replaceAll(token, "\\\\", "");
        String noBackslashEscapes = replaceAll(replaceAll(noEscapedBackslashes, "\\'", ""), "\\\"", "");
        return replaceAll(noBackslashEscapes, "''", "").replace("'", " ' ");
    }

    @Override
    protected String cleanToken(String token) {
        if (token.startsWith("B'") || token.startsWith("X'")) {
            return token.substring(token.indexOf("'"));
        }

        if (token.startsWith("_")) {
            for (String charSet : charSets) {
                String cast = "_" + charSet;
                if (token.startsWith(cast)) {
                    return token.substring(cast.length());
                }
            }
        }

        // If no matches are found for charset casting then return token
        return token;
    }

    @Override
    protected String extractAlternateOpenQuote(String token) {
        if (token.startsWith("\"")) {
            return "\"";
        }
        return null;
    }
}