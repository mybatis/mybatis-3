package org.apache.ibatis.parsing;

import org.apache.ibatis.exceptions.PersistenceException;

public class ParsingException extends PersistenceException {
  public ParsingException() {
    super();
  }

  public ParsingException(String message) {
    super(message);
  }

  public ParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParsingException(Throwable cause) {
    super(cause);
  }
}
