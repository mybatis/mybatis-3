/**
 *    Copyright 2009-2017 the original author or authors.
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
package org.apache.ibatis.exceptions;

/**
 * @author Clinton Begin
 */
public class TooManyResultsException extends PersistenceException {

  private static final long serialVersionUID = 8935197089745865786L;

  public TooManyResultsException() {
    super();
  }

  public TooManyResultsException(String message) {
    super(message);
  }

  public TooManyResultsException(String message, Throwable cause) {
    super(message, cause);
  }

  public TooManyResultsException(Throwable cause) {
    super(cause);
  }
}
