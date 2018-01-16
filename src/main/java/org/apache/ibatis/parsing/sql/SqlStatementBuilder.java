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
package org.apache.ibatis.parsing.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a SQL statement, one line at a time.
 */
public class SqlStatementBuilder {
    /**
     * The current statement, as it is being built.
     */
    protected StringBuilder statement = new StringBuilder();

    /**
     * Flag indicating whether the current statement is still empty.
     */
    private boolean empty = true;

    /**
     * Flag indicating whether the current statement is properly terminated.
     */
    private boolean terminated;

    /**
     * Are we currently inside a ' multi-line string literal.
     */
    private boolean insideQuoteStringLiteral = false;

    /**
     * Are we currently inside an alternate multi-line string literal.
     */
    private boolean insideAlternateQuoteStringLiteral = false;

    /**
     * The alternate quote that is expected to close the string literal.
     */
    private String alternateQuote;

    /**
     * Whether the last processed line ended with a single line -- comment.
     */
    private boolean lineEndsWithSingleLineComment = false;

    /**
     * Are we inside a multi-line /*  *&#47; comment.
     */
    private boolean insideMultiLineComment = false;

    /**
     * Whether a non-comment part of a statement has already been seen.
     */
    private boolean nonCommentStatementPartSeen = false;

    /**
     * The current delimiter to look for to terminate the statement.
     */
    protected Delimiter delimiter;

    /**
     * Creates a new SqlStatementBuilder.
     *
     * @param defaultDelimiter The delimiter for this database.
     */
    public SqlStatementBuilder(Delimiter defaultDelimiter) {
        this.delimiter = defaultDelimiter;
    }

    /**
     * @param delimiter The current delimiter to look for to terminate the statement.
     */
    public void setDelimiter(Delimiter delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Checks whether the statement is still empty.
     *
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Checks whether the statement being built is now properly terminated.
     *
     * @return {@code true} if it is, {@code false} if it isn't.
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @return The assembled statement, with the delimiter stripped off.
     */
    public String getSqlStatement() {
        return statement.toString();
    }

    /**
     * Checks whether this line is in fact a directive disguised as a comment.
     *
     * @param line The line to analyse.
     * @return {@code true} if it is a directive that should be processed by the database, {@code false} if not.
     */
    public boolean isCommentDirective(String line) {
        return false;
    }

    /**
     * Checks whether this line is just a single-line comment outside a statement or not.
     *
     * @param line The line to analyse.
     * @return {@code true} if it is, {@code false} if not.
     */
    protected boolean isSingleLineComment(String line) {
        return line.startsWith("--");
    }

    /**
     * Adds this line to the current statement being built.
     *
     * @param line The line to add.
     */
    public void addLine(String line) {
        if (isEmpty()) {
            empty = false;
        } else {
            statement.append("\n");
        }

        if (isCommentDirective(line.trim())) {
            nonCommentStatementPartSeen = true;
        }

        String lineSimplified = simplifyLine(line);

        applyStateChanges(lineSimplified);
        if (endWithOpenMultilineStringLiteral() || insideMultiLineComment || isSingleLineComment(lineSimplified)) {
            statement.append(line);
            return;
        }

        delimiter = changeDelimiterIfNecessary(lineSimplified, delimiter);

        statement.append(line);

        if (!lineEndsWithSingleLineComment && lineTerminatesStatement(lineSimplified, delimiter)) {
            stripDelimiter(statement, delimiter);
            terminated = true;
        }
    }

    /**
     * Checks whether the statement currently ends with an open multiline string literal.
     *
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    /* protected -> for testing */ boolean endWithOpenMultilineStringLiteral() {
        return insideQuoteStringLiteral || insideAlternateQuoteStringLiteral;
    }

    /**
     * @return Whether the current statement is only closed comments so far and can be discarded.
     */
    public boolean canDiscard() {
        return !insideAlternateQuoteStringLiteral && !insideQuoteStringLiteral && !insideMultiLineComment && !nonCommentStatementPartSeen;
    }

    /**
     * Simplifies this line to make it easier to parse.
     *
     * @param line The line to simplify.
     * @return The simplified line.
     */
    protected String simplifyLine(String line) {
        return removeEscapedQuotes(line)
                .replace("--", " -- ")
                .replace("/*", " /* ")
                .replace("*/", " */ ")
                .replaceAll("\\s+", " ").trim().toUpperCase();
    }

    /**
     * Checks whether this line in the sql script indicates that the statement delimiter will be different from the
     * current one. Useful for database-specific stored procedures and block constructs.
     *
     * @param line      The simplified line to analyse.
     * @param delimiter The current delimiter.
     * @return The new delimiter to use (can be the same as the current one) or {@code null} for no delimiter.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter) {
        return delimiter;
    }

    /**
     * Checks whether this line terminates the current statement.
     *
     * @param line      The line to check.
     * @param delimiter The current delimiter.
     * @return {@code true} if it does, {@code false} if it doesn't.
     */
    private boolean lineTerminatesStatement(String line, Delimiter delimiter) {
        if (delimiter == null) {
            return false;
        }

        String upperCaseDelimiter = delimiter.getDelimiter().toUpperCase();

        if (delimiter.isAloneOnLine()) {
            return line.equals(upperCaseDelimiter);
        }

        return line.endsWith(upperCaseDelimiter);
    }

    /**
     * Strips this delimiter from this sql statement.
     *
     * @param sql       The statement to parse.
     * @param delimiter The delimiter to strip.
     */
    private static void stripDelimiter(StringBuilder sql, Delimiter delimiter) {
        int last;

        for (last = sql.length(); last > 0; last--) {
            if (!Character.isWhitespace(sql.charAt(last - 1))) {
                break;
            }
        }

        sql.delete(last - delimiter.getDelimiter().length(), sql.length());
    }

    /**
     * Extracts the alternate open quote from this token (if any).
     *
     * @param token The token to check.
     * @return The alternate open quote. {@code null} if none.
     */
    protected String extractAlternateOpenQuote(String token) {
        return null;
    }

    /**
     * Computes the alternate closing quote for this open quote.
     *
     * @param openQuote The alternate open quote.
     * @return The close quote.
     */
    protected String computeAlternateCloseQuote(String openQuote) {
        return openQuote;
    }

    /**
     * Applies any state changes resulting from this line being added.
     *
     * @param line The line that was just added to the statement.
     */
    protected void applyStateChanges(String line) {
        String[] tokens = tokenizeLine(line);

        List<TokenType> delimitingTokens = extractStringLiteralDelimitingTokens(tokens);

        lineEndsWithSingleLineComment = false;
        for (TokenType delimitingToken : delimitingTokens) {
            if (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral
                    && TokenType.MULTI_LINE_COMMENT_OPEN.equals(delimitingToken)) {
                insideMultiLineComment = true;
            }
            if (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral
                    && TokenType.MULTI_LINE_COMMENT_CLOSE.equals(delimitingToken)) {
                insideMultiLineComment = false;
            }

            if (!insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral && !insideMultiLineComment
                    && TokenType.SINGLE_LINE_COMMENT.equals(delimitingToken)) {
                lineEndsWithSingleLineComment = true;
                return;
            }

            if (!insideMultiLineComment && !insideQuoteStringLiteral &&
                    TokenType.ALTERNATE_QUOTE.equals(delimitingToken)) {
                insideAlternateQuoteStringLiteral = !insideAlternateQuoteStringLiteral;
            }

            if (!insideMultiLineComment && !insideAlternateQuoteStringLiteral &&
                    TokenType.QUOTE.equals(delimitingToken)) {
                insideQuoteStringLiteral = !insideQuoteStringLiteral;
            }

            if (!insideMultiLineComment && !insideQuoteStringLiteral && !insideAlternateQuoteStringLiteral &&
                    TokenType.OTHER.equals(delimitingToken)) {
                nonCommentStatementPartSeen = true;
            }
        }
    }

    /**
     * Ignore all special characters that naturally occur in SQL, but are not opening or closing string literals.
     *
     * @param line The line to tokenize.
     * @return The tokens.
     */
    protected String[] tokenizeLine(String line) {
        return tokenizeToStringArray(line, " @<>;:=|(),+{}");
    }

    /**
     * Extract the type of all tokens that potentially delimit string literals.
     *
     * @param tokens The tokens to analyse.
     * @return The list of potentially delimiting string literals token types per token. Tokens that do not have any
     * impact on string delimiting are discarded.
     */
    private List<TokenType> extractStringLiteralDelimitingTokens(String[] tokens) {
        List<TokenType> delimitingTokens = new ArrayList<TokenType>();
        for (String token : tokens) {
            String cleanToken = cleanToken(token);
            boolean handled = false;

            if (alternateQuote == null) {
                String alternateQuoteFromToken = extractAlternateOpenQuote(cleanToken);
                if (alternateQuoteFromToken != null) {
                    String closeQuote = computeAlternateCloseQuote(alternateQuoteFromToken);
                    if (cleanToken.length() >= (alternateQuoteFromToken.length() + closeQuote.length())
                            && cleanToken.startsWith(alternateQuoteFromToken) && cleanToken.endsWith(closeQuote)) {
                        //Skip $$abc$$, ...
                        continue;
                    }

                    alternateQuote = closeQuote;
                    delimitingTokens.add(TokenType.ALTERNATE_QUOTE);

                    continue;
                }
            }
            if ((alternateQuote != null) && cleanToken.endsWith(alternateQuote)) {
                alternateQuote = null;
                delimitingTokens.add(TokenType.ALTERNATE_QUOTE);
                continue;
            }

            if ((cleanToken.length() >= 2) && cleanToken.startsWith("'") && cleanToken.endsWith("'")) {
                //Skip '', 'abc', ...
                continue;
            }
            if ((cleanToken.length() >= 4)) {
                int numberOfOpeningMultiLineComments = countOccurrencesOf(cleanToken, "/*");
                int numberOfClosingMultiLineComments = countOccurrencesOf(cleanToken, "*/");
                if (numberOfOpeningMultiLineComments > 0 && numberOfOpeningMultiLineComments == numberOfClosingMultiLineComments) {
                    //Skip /**/, /*comment*/, ...
                    continue;
                }
            }

            if (isSingleLineComment(cleanToken)) {
                delimitingTokens.add(TokenType.SINGLE_LINE_COMMENT);
                handled = true;
            }

            if (cleanToken.contains("/*")) {
                delimitingTokens.add(TokenType.MULTI_LINE_COMMENT_OPEN);
                handled = true;
            } else if (cleanToken.startsWith("'")) {
                delimitingTokens.add(TokenType.QUOTE);
                handled = true;
            }

            if (!cleanToken.contains("/*") && cleanToken.contains("*/")) {
                delimitingTokens.add(TokenType.MULTI_LINE_COMMENT_CLOSE);
                handled = true;
            } else if (!cleanToken.startsWith("'") && cleanToken.endsWith("'")) {
                delimitingTokens.add(TokenType.QUOTE);
                handled = true;
            }

            if (!handled) {
                delimitingTokens.add(TokenType.OTHER);
            }
        }

        return delimitingTokens;
    }

    /**
     * Removes escaped quotes from this token.
     *
     * @param token The token to parse.
     * @return The cleaned token.
     */
    protected String removeEscapedQuotes(String token) {
        return replaceAll(token, "''", "");
    }

    /**
     * Performs additional cleanup on this token, such as removing charset casting that prefixes string literals.
     * Must be implemented in dialect specific sub classes.
     *
     * @param token The token to clean.
     * @return The cleaned token.
     */
    protected String cleanToken(String token) {
        return token;
    }

    /**
     * Replaces all occurrances of this originalToken in this string with this replacementToken.
     *
     * @param str              The string to process.
     * @param originalToken    The token to replace.
     * @param replacementToken The replacement.
     * @return The transformed str.
     */
    protected static String replaceAll(String str, String originalToken, String replacementToken) {
        return str.replaceAll(Pattern.quote(originalToken), Matcher.quoteReplacement(replacementToken));
    }

    /**
     * Splits this string into an array using these delimiters.
     *
     * @param str        The string to split.
     * @param delimiters The delimiters to use.
     * @return The resulting array.
     */
    protected static String[] tokenizeToStringArray(String str, String delimiters) {
        if (str == null) {
            return null;
        }
        String[] tokens = str.split("[" + delimiters + "]");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        return tokens;
    }

    /**
     * Counts the number of occurrences of this token in this string.
     *
     * @param str   The string to analyse.
     * @param token The token to look for.
     * @return The number of occurrences.
     */
    protected static int countOccurrencesOf(String str, String token) {
        if (str == null || token == null || str.length() == 0 || token.length() == 0) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(token, pos)) != -1) {
            ++count;
            pos = idx + token.length();
        }
        return count;
    }
    
    /**
     * The types of tokens relevant for string delimiter related parsing.
     */
    private enum TokenType {
        /**
         * Some other token.
         */
        OTHER,

        /**
         * Token opens or closes a ' string literal
         */
        QUOTE,

        /**
         * Token opens or closes an alternate string literal
         */
        ALTERNATE_QUOTE,

        /**
         * Token starts end of line comment
         */
        SINGLE_LINE_COMMENT,

        /**
         * Token opens a multi-line comment
         */
        MULTI_LINE_COMMENT_OPEN,

        /**
         * Token closes a multi-line comment
         */
        MULTI_LINE_COMMENT_CLOSE
    }
}