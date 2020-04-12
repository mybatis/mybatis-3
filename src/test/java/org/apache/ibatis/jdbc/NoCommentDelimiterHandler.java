package org.apache.ibatis.jdbc;

import org.apache.ibatis.jdbc.handler.DelimiterHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author James
 */
public class NoCommentDelimiterHandler implements DelimiterHandler {

  private static final String DELIMITER_NAME = "DELIMITER";

  private boolean lineStartWithDelimiter(String trimmedLine) {
    if (trimmedLine.length() <= DELIMITER_NAME.length()) {
      return false;
    }
    return DELIMITER_NAME.equalsIgnoreCase(trimmedLine.substring(0, DELIMITER_NAME.length()));
  }

  @Override
  public boolean resetDelimiter(ScriptRunner scriptRunner, String trimmedLine) {
    if (lineStartWithDelimiter(trimmedLine)) {
        String delimiter = trimmedLine.substring(DELIMITER_NAME.length()).trim();
        if (delimiter.length() > 0) {
          scriptRunner.setDelimiter(delimiter);
          return true;
        }
    }
    return false;
  }
}
