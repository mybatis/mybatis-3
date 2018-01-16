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

import org.apache.ibatis.jdbc.RuntimeSqlException;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MybatisGenericSqlParser extends SqlParser {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
  private static final String DEFAULT_DELIMITER = ";";

  private static final Pattern DELIMITER_PATTERN = Pattern.compile("^\\s*((--)|(//))?\\s*(//)?\\s*@DELIMITER\\s+([^\\s]+)", Pattern.CASE_INSENSITIVE);

  private String delimiter = DEFAULT_DELIMITER;
  private boolean fullLineDelimiter = false;

  @Override
  public SqlParser withProperties(Map<Object, Object> additionalProps) {
    String delimFromProps = (String) additionalProps.get(SqlParserFactory.DELIMITER_PROP);
    if (delimFromProps != null) {
      this.delimiter = delimFromProps;
    }
    Boolean fullLineFromProps = (Boolean) additionalProps.get(SqlParserFactory.FULLLINE_DELIMITER_PROP);
    if (fullLineFromProps != null) {
      this.fullLineDelimiter = fullLineFromProps;
    }
    return this;
  }

  @Override
  public Integer suitabilityFor(Connection c, Map<Object, Object> additionalProps) {
    return Integer.MAX_VALUE;
  }

  @Override
  protected String readNextStatement() {
    StringBuilder command = new StringBuilder();

    String line;
    try {
      while ((line = lineReader.readLine()) != null) {
        if (handleLine(command, line)) {
          return command.toString();
        }
      }
    } catch (IOException e) {
      throw new RuntimeSqlException("Error reading SQL from source file", e);
    }
    checkForMissingLineTerminator(command);
    String stmt = command.toString();
    return stmt.trim().isEmpty() ? null : stmt;
  }

  private void checkForMissingLineTerminator(StringBuilder command) {
    if (command != null && command.toString().trim().length() > 0) {
      throw new RuntimeSqlException("Line missing end-of-line terminator (" + delimiter + ") => " + command);
    }
  }

  private boolean handleLine(StringBuilder command, String line) {
    String trimmedLine = line.trim();
    if (lineIsComment(trimmedLine)) {
      Matcher matcher = DELIMITER_PATTERN.matcher(trimmedLine);
      if (matcher.find()) {
        delimiter = matcher.group(5);
      }
    } else if (commandReadyToExecute(trimmedLine)) {
      command.append(line.substring(0, line.lastIndexOf(delimiter)));
      command.append(LINE_SEPARATOR);
      return true;
    } else if (trimmedLine.length() > 0) {
      command.append(line);
      command.append(LINE_SEPARATOR);
    }
    return false;
  }

  private boolean lineIsComment(String trimmedLine) {
    return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
  }

  private boolean commandReadyToExecute(String trimmedLine) {
    // issue #561 remove anything after the delimiter
    return !fullLineDelimiter && trimmedLine.contains(delimiter) || fullLineDelimiter && trimmedLine.equals(delimiter);
  }
}
