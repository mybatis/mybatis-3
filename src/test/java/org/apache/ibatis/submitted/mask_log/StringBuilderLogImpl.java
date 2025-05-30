/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.submitted.mask_log;

import org.apache.ibatis.logging.Log;

public class StringBuilderLogImpl implements Log {

  static StringBuilder LOG_CONTENT = new StringBuilder();

  public StringBuilderLogImpl(String clazz) {
    // Do Nothing
  }

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public boolean isTraceEnabled() {
    return true;
  }

  @Override
  public void error(String s, Throwable e) {
  }

  @Override
  public void error(String s) {
  }

  @Override
  public void debug(String s) {
    if (s.startsWith("==> Parameters:") || s.startsWith("<==        Row:")) {
      LOG_CONTENT.append(s);
    }
  }

  @Override
  public void trace(String s) {
    if (s.startsWith("==> Parameters:") || s.startsWith("<==        Row:")) {
      LOG_CONTENT.append(s);
    }
  }

  @Override
  public void warn(String s) {
  }
}
