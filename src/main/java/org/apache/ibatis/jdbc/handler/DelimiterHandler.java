package org.apache.ibatis.jdbc.handler;

import org.apache.ibatis.jdbc.ScriptRunner;

/**
 * @author James
 */
public interface DelimiterHandler {
  boolean resetDelimiter(ScriptRunner scriptRunner, String line);
}
