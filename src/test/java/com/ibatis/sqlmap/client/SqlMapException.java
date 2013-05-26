/*
 *    Copyright 2009-2012 the original author or authors.
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
package com.ibatis.sqlmap.client;


/*
 * Thrown to indicate a problem with SQL Map configuration or state.  Generally
 * if an SqlMapException is thrown, something is critically wronge and cannot
 * be corrected until a change to the configuration or the environment is made.
 * <p/>
 * Note: Generally this wouldn't be used to indicate that an SQL execution error
 * occurred (that's what SQLException is for).
 */
public class SqlMapException extends RuntimeException {

  /*
   * Simple constructor
   */
  public SqlMapException() {
  }

  /*
   * Constructor to create exception with a message
   *
   * @param msg A message to associate with the exception
   */
  public SqlMapException(String msg) {
    super(msg);
  }

  /*
   * Constructor to create exception to wrap another exception
   *
   * @param cause The real cause of the exception
   */
  public SqlMapException(Throwable cause) {
    super(cause);
  }

  /*
   * Constructor to create exception to wrap another exception and pass a message
   *
   * @param msg   The message
   * @param cause The real cause of the exception
   */
  public SqlMapException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
