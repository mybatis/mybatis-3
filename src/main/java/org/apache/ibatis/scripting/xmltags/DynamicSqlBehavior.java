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

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.scripting.ScriptingException;

/**
 * Specify the behavior when detects a dynamic sql.
 *
 * @since 3.5.6
 * @author Kazuki Shimizu
 */
public enum DynamicSqlBehavior {

  /**
   * Allow the dynamic sql (Default).
   */
  ALLOW {
    @Override
    public void doAction(String content) {
      // do nothing
    }
  },

  /**
   * Allow the dynamic sql and output warning log.
   * Note: The log level of {@code 'org.apache.ibatis.scripting.xmltags.DynamicSqlBehavior'} must be set to {@code WARN}.
   */
  WARNING {
    @Override
    public void doAction(String content) {
      DynamicSqlBehavior.LogHolder.log.warn(buildMessage(content));
    }
  },

  /**
   * Deny the dynamic sql.
   * Note: throw {@link ScriptingException}.
   */
  DENY {
    @Override
    public void doAction(String content) {
      throw new ScriptingException(buildMessage(content));
    }
  };

  /**
   * Perform the action when detects a dynamic sql.
   * @param content a dynamic content
   */
  public abstract void doAction(String content);

  /**
   * build error message.
   */
  private static String buildMessage(String content) {
      return "Dynamic sql is detected. dynamic content : " + content;
  }

  private static class LogHolder {
    private static final Log log = LogFactory.getLog(DynamicSqlBehavior.class);
  }

}
