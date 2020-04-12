package org.apache.ibatis.jdbc;

import org.apache.ibatis.jdbc.handler.DelimiterHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author James
 */
public class NoCommentDelimiterHandler implements DelimiterHandler {
  private static final Pattern NOCOMMENT_DELIMITER = Pattern.compile("^DELIMITER\\s*(\\S+)", Pattern.CASE_INSENSITIVE);

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
      Matcher matcher = NOCOMMENT_DELIMITER.matcher(trimmedLine);
      if (matcher.find()) {
        scriptRunner.setDelimiter(matcher.group(1));
        return true;
      }
    }
    return false;
  }
}
